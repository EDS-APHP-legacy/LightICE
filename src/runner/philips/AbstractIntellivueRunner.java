package runner.philips;

import common.time.*;
import common.DeviceIdentity;
import common.io.util.StateMachine;
import common.net.TaskQueue;
import drivers.philips.intellivue.Intellivue;
import drivers.philips.intellivue.Message;
import drivers.philips.intellivue.Network;
import drivers.philips.intellivue.action.ExtendedPollDataResult;
import drivers.philips.intellivue.action.ObservationPoll;
import drivers.philips.intellivue.action.SingleContextPoll;
import drivers.philips.intellivue.action.SinglePollDataResult;
import drivers.philips.intellivue.action.impl.ExtendedPollDataResultImpl;
import drivers.philips.intellivue.association.*;
import drivers.philips.intellivue.association.impl.AssociationFinishImpl;
import drivers.philips.intellivue.attribute.Attribute;
import drivers.philips.intellivue.attribute.AttributeFactory;
import drivers.philips.intellivue.connectindication.ConnectIndication;
import drivers.philips.intellivue.data.*;
import drivers.philips.intellivue.dataexport.DataExportError;
import drivers.philips.intellivue.dataexport.DataExportMessage;
import drivers.philips.intellivue.dataexport.DataExportResultInterface;
import drivers.philips.intellivue.dataexport.command.EventReportInterface;
import drivers.philips.intellivue.dataexport.command.GetInterface;
import drivers.philips.intellivue.dataexport.command.SetResultInterface;
import drivers.philips.intellivue.dataexport.event.MdsCreateEvent;
import datatypes.SampleArray;
import ice.ConnectionState;
import datatypes.Numeric;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import runner.AbstractDeviceRunner;
import runner.InstanceHolder;
import runner.philips.time.IntellivueRelativeClock;

import java.io.*;
import java.lang.Float;
import java.lang.String;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractIntellivueRunner extends AbstractDeviceRunner {

    // Connection timeouts and stuff
    protected final static long WATCHDOG_INTERVAL = 200L;
    protected final static long IN_CONNECTION_TIMEOUT = 5000L; // Maximum time between message receipt
    protected final static long IN_CONNECTION_ASSERT = 4000L;  // Assert a keepalive if no message received for this long
    protected static long OUT_CONNECTION_ASSERT = 8000L;
    protected final static long CONTINUOUS_POLL_INTERVAL = 10 * 60000L;
    protected final static long CONTINUOUS_POLL_ASSERT = 9 * 60000L;
    protected final static long ASSOCIATION_REQUEST_INTERVAL = 2000L;
    protected final static long FINISH_REQUEST_INTERVAL = 500L;
    private long lastAssociationRequest = 0L;
    private long lastFinishRequest = 0L;
    private long lastDataPoll = 0L;
    private long lastMessageReceived = 0L;
    private long lastKeepAlive = 0L;
    private long lastMessageSentTime = 0L;
    private static final long PERIOD = 1000L;

    protected final Intellivue intellivue;

    //protected final EventLoop eventLoop;
    protected ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    private ScheduledFuture<?> emitFastData;

    // Terminology mapping
    protected final Map<ObservedValue, String> numericRosettaMetrics = new HashMap<>();
    protected final Map<ObservedValue, String> sampleArrayRosettaMetrics = new HashMap<>();
    protected final Map<ObservedValue, Label> numericLabels = new HashMap<>();
    protected final Map<ObservedValue, Label> sampleArrayLabels = new HashMap<>();

    // SampleArrays and Numerics cache
    protected final Map<ObservedValue, Map<Integer, InstanceHolder<Numeric>>> numericUpdates = new HashMap<>();
    protected final Map<ObservedValue, Map<Integer, InstanceHolder<SampleArray>>> sampleArrayUpdates = new HashMap<>();
    protected final Map<ObservedValue, Map<Integer, SampleCache>> sampleArrayCache = Collections.synchronizedMap(new HashMap<>());


    // A set containing all the SelectionKey corresponding to the networkloop (SelectionKey=channel+selector)
    protected Set<SelectionKey> registrationKeys = new HashSet<>();

    private enum DisconnectState {
        Initial,
        Disconnecting,
        Disconnected
    }

    private static final DisconnectState[][] legalTransitions = new DisconnectState[][]{
            {DisconnectState.Initial, DisconnectState.Disconnecting},
            {DisconnectState.Disconnecting, DisconnectState.Disconnected}
    };

    private final StateMachine<DisconnectState> disconnectState = new StateMachine<>(legalTransitions, DisconnectState.Initial, null);

    public AbstractIntellivueRunner(DeviceIdentity deviceIdentity) throws IOException {
        super(deviceIdentity); // initializes the networkloop
        loadMap(numericRosettaMetrics, numericLabels, sampleArrayRosettaMetrics, sampleArrayLabels);

        this.intellivue = new IntellivueExt(new WallClock());

        TaskQueue.Task<?> watchdogTask = new TaskQueue.TaskImpl<Object>() {
            @Override
            public Object doExecute(TaskQueue queue) {
                watchdog();
                return null;
            }
        };
        watchdogTask.setInterval(WATCHDOG_INTERVAL);
        this.networkLoop.add(watchdogTask);

    }

    protected void watchdog() {
        long now = System.currentTimeMillis();
        log.debug("Watchdog: statemachine = " + stateMachine.getState());
        switch (stateMachine.getState().ordinal()) {
            case ConnectionState._Negotiating:
                // In the negotiating state we are emitting association requests
                if (now - lastAssociationRequest >= ASSOCIATION_REQUEST_INTERVAL) {
                    try {
                        log.debug("Requesting association");
                        intellivue.requestAssociation();
                        lastAssociationRequest = now;
                    } catch (IOException e1) {
                        log.error("requesting association", e1);
                    }
                }

            case ConnectionState._Terminal:
                // In the disconnecting state we are emitting association finish
                // requests
                if (DisconnectState.Disconnecting.equals(disconnectState.getState())) {
                    if (now - lastFinishRequest >= FINISH_REQUEST_INTERVAL) {
                        try {
                            intellivue.send(new AssociationFinishImpl());
                            lastFinishRequest = now;
                        } catch (IOException e1) {
                            log.error("sending association finish", e1);
                        }
                    }
                }
                break;
            case ConnectionState._Connected:
                if (now - lastMessageReceived >= IN_CONNECTION_TIMEOUT) {
                    // Been too long since the last message was received, revert to
                    // new association requests (Negotiation)
                    state(ConnectionState.Negotiating, "timeout receiving  messages");
                    return;
                } else if ((now - lastMessageReceived >= IN_CONNECTION_ASSERT || now - lastMessageSentTime >= OUT_CONNECTION_ASSERT)
                        && now - lastKeepAlive >= Math.min(OUT_CONNECTION_ASSERT, IN_CONNECTION_ASSERT)) {
                    // Either side (or both) has not asserted themselves in the time
                    // required AND we haven't recently sent a keep alive message
                    try {
                        // NOM_MOC_VMO_AL_MON == ALARMS
                        intellivue.requestSinglePoll(ObjectClass.NOM_MOC_VMO_AL_MON, AttributeId.NOM_ATTR_GRP_VMO_STATIC);

                        lastKeepAlive = now;
                    } catch (IOException e) {
                        state(ConnectionState.Negotiating, "failure to send a keepalive");
                        log.error("requesting a keep alive (static attributes of the alarm monitor object)", e);
                    }
                } else if (now - lastDataPoll >= CONTINUOUS_POLL_ASSERT) {
                    // Time to request a new data poll
                    try {
                        // NOM_MOC_VMO_METRIC_NU == NUMERICS
                        intellivue.requestExtendedPoll(ObjectClass.NOM_MOC_VMO_METRIC_NU, CONTINUOUS_POLL_INTERVAL);

                        // NOM_MOC_VMO_METRIC_SA_RT == WAVEFORMS
                        intellivue.requestExtendedPoll(ObjectClass.NOM_MOC_VMO_METRIC_SA_RT, CONTINUOUS_POLL_INTERVAL);

                        // NOM_MOC_VMO_AL_MON == ALARMS
                        intellivue.requestExtendedPoll(ObjectClass.NOM_MOC_VMO_AL_MON, CONTINUOUS_POLL_INTERVAL, AttributeId.NOM_ATTR_GRP_AL_MON);

                        // NOM_MOC_PT_DEMOG == PATIENT DEMOGRAPHICS
                        intellivue.requestSinglePoll(ObjectClass.NOM_MOC_PT_DEMOG, AttributeId.NOM_ATTR_GRP_PT_DEMOG);
                        lastDataPoll = now;
                    } catch (IOException e) {
                        log.error("requesting data polls", e);
                    }
                }
                break;
        }
    }

    protected final void state(ConnectionState state, String connectionInfo) {
        // So actually the state transition will emit the connection info
        if (!stateMachine.transitionWhenLegal(state, 5000L, connectionInfo)) {
            throw new RuntimeException("timed out changing state");
        }

        // If we didn't actually transition state then this will fire the info
        // change
        // If we already did fire it this will be a no op
        setConnectionInfo(connectionInfo);
    }


    protected void unregisterAll() {
        for (SelectionKey key : registrationKeys) {
            networkLoop.unregister(key, intellivue);
        }
        registrationKeys.clear();
    }

    public void connect(InetAddress remote) throws IOException {
        connect(remote, -1, Intellivue.DEFAULT_UNICAST_PORT);
    }

    public void connect(InetAddress remote, int prefixLength, int port) throws IOException {
        InetSocketAddress local = null;

        if (prefixLength >= 0) {
            InetAddress l = Network.getLocalAddresses(remote, (short) prefixLength, true).get(0);
            local = new InetSocketAddress(l, 0);
        } else {
            local = new InetSocketAddress(0);
        }

        InetSocketAddress remoteInetSocketAddress = new InetSocketAddress(remote, port);
        connect(remoteInetSocketAddress, local);
    }

    public void connect(InetSocketAddress remote, InetSocketAddress local) throws IOException {
        switch (stateMachine.getState().ordinal()) {
            case ConnectionState._Initial:
                state(ConnectionState.Connecting, "trying " + remote.getAddress().getHostAddress() + ":" + remote.getPort());
                break;
            case ConnectionState._Connecting:
                setConnectionInfo("trying " + remote.getAddress().getHostAddress() + ":" + remote.getPort());
                break;
            default:
                return;
        }

        unregisterAll();

        final DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.socket().setReuseAddress(true);
        channel.bind(local);
        channel.connect(remote);

        registrationKeys.add(networkLoop.register(intellivue, channel));

        state(ConnectionState.Negotiating, "Requesting Association");
    }

    @Override
    public void disconnect() {
        synchronized (stateMachine) {
            if (ConnectionState.Terminal.equals(stateMachine.getState())) {
                return;
            }
            stateMachine.transitionIfLegal(ConnectionState.Terminal, "disconnect()");
        }
        if (!ConnectionState.Connected.equals(stateMachine.getState())) {
            disconnectState.transitionIfLegal(DisconnectState.Disconnecting, "");
            disconnectState.transitionIfLegal(DisconnectState.Disconnected, "");
            return;
        } else {
            disconnectState.transitionIfLegal(DisconnectState.Disconnecting, "");
        }
        if (!disconnectState.wait(DisconnectState.Disconnected, 5000L)) {
            log.trace("No disconnect received in response to finish");
        }

    }

    @Override
    public void shutdown() {
        networkLoop.clearTasks();
        networkLoop.cancelThread();
        if (null != networkLoopThread) {
            try {
                networkLoopThread.join();
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
        }


        super.shutdown();
    }


    protected final InstanceHolder<SampleArray> getSampleArrayUpdate(ObservedValue ov, int handle) {
        Map<Integer, InstanceHolder<SampleArray>> forObservedValue = sampleArrayUpdates.get(ov);
        if (null == forObservedValue) {
            return null;
        } else {
            return forObservedValue.get(handle);
        }
    }
    protected final void putSampleArrayUpdate(ObservedValue ov, int handle, InstanceHolder<SampleArray> value) {
        Map<Integer, InstanceHolder<SampleArray>> forObservedValue = sampleArrayUpdates.get(ov);
        if (null == forObservedValue) {
            forObservedValue = new HashMap<>();
            sampleArrayUpdates.put(ov, forObservedValue);
        }
        if(null == value) {
            forObservedValue.remove(handle);
        } else {
            forObservedValue.put(handle, value);
        }
    }

    @Override
    protected void stateChanged(ConnectionState newState, ConnectionState oldState, String transitionNote) {
        super.stateChanged(newState, oldState, transitionNote);
        if (ConnectionState.Connected.equals(oldState) && !ConnectionState.Connected.equals(newState)) {
            lastDataPoll = 0L;
            lastMessageReceived = 0L;
            lastMessageSentTime = 0L;
            lastKeepAlive = 0L;
            stopEmitFastData();
        } else if(ConnectionState.Connected.equals(newState) && !ConnectionState.Connected.equals(oldState)) {
            startEmitFastData();
        }
    }
    private synchronized void startEmitFastData() {
        // for the 62.5Hz case we use two seconds (125 samples)
        log.info("Start emit fast data for period " + PERIOD + "ms");
        //TODO: Configurable Emit Period
        emitFastData = executor.scheduleAtFixedRate(new EmitData(),5 * PERIOD - System.currentTimeMillis() % PERIOD, PERIOD, TimeUnit.MILLISECONDS);
    }
    private synchronized void stopEmitFastData() {
        emitFastData.cancel(false);
        emitFastData = null;
        log.info("stop emit fast data");
    }


    private class EmitData implements Runnable {

        public EmitData() {
        }

        @Override
        public void run() {
            try {
                IceInstant fakeSampleTime = IceInstant.now(); // new DomainClock().instant();

                // For k, v in sampleArrayCache...
                synchronized (sampleArrayCache) {
                    for (Map.Entry<ObservedValue, Map<Integer, SampleCache>> entry : sampleArrayCache.entrySet()) {
                        ObservedValue observedValue = entry.getKey();
                        Map<Integer, SampleCache> sampleCacheByHandle = entry.getValue();

                        // For kk, vv in sampleCacheByHandle...
                        for (Map.Entry<Integer, SampleCache> subentry : sampleCacheByHandle.entrySet()) {
                            Integer handleId = subentry.getKey();
                            SampleCache sampleCache = subentry.getValue();

                            InstanceHolder<SampleArray> holder = getSampleArrayUpdate(observedValue, handleId);

                            // Get the time period over which values are sampled for this handle
                            RelativeTime rt = handleToUpdatePeriod.get(handleId);

                            if (null == rt || null == sampleCache) {
                                log.warn("No RelativeTime for handle=" + handleId + " rt=" + rt + " sampleCache=" + sampleCache + " unitCode=" + unitCode);
                                continue;
                            }
//                        int samples = (int) (PERIOD / rt.toMilliseconds());

                            if (null != holder) {
                                synchronized (sampleCache) {
                                    log.debug("Getting samples from sampleCache... (holder not null)");
                                    Pair<IceInstant, List<Number>> c = sampleCache.emitSamples(holder.data.getRosettaMetric() + " " + holder.data.instanceId);
                                    if (null == c) {
                                        putSampleArrayUpdate(observedValue, handleId, null);
                                    } else {
                                        sampleArraySample(holder.data, c.getValue(), c.getKey(), fakeSampleTime);
                                    }
                                }
                            } else {
                                String rosettaMetric = sampleArrayRosettaMetrics.get(observedValue);
                                if (rosettaMetric == null) {
                                    rosettaMetric = "";
                                    log.warn("Unknown waveform:" + observedValue);
                                }
                                UnitCode unitCode = handleToUnitCode.get(handleId);
                                synchronized (sampleCache) {
                                    System.out.println("Getting samples from sampleCache... (holder not null)");
                                    putSampleArrayUpdate(
                                            observedValue, handleId,
                                            sampleArraySample(getSampleArrayUpdate(observedValue, handleId), sampleCache.emitSamples(rosettaMetric + " " + handleId),
                                                    rosettaMetric, observedValue.toString(), handleId,
                                                    PhilipsToRosettaMapping.units(unitCode),
                                                    (int) (1000L / rt.toMilliseconds()), fakeSampleTime));
                                }
                            }
                        }
                    }
                }

            } catch (Throwable t) {
                log.error("error emitting fast data", t);
            }
        }
    }



    private class IntellivueExt extends Intellivue {

        private final Logger log = LoggerFactory.getLogger(IntellivueExt.class);

        private final Clock referenceClock;
        private IntellivueRelativeClock intellivueRelativeClock = null;

        public IntellivueExt(Clock referenceClock) {
            super();
            this.referenceClock = referenceClock;
//            intellivueRelativeClock = new IntellivueRelativeClock();
        }

        @Override
        protected void handleDataExportMessage(DataExportResultInterface message) {
            // if we were checking for confirmation of outgoing confirmed
            // messages this would be the place to find confirmations
            super.handleDataExportMessage(message);
        }

        @Override
        public synchronized boolean send(Message message) throws IOException {
            lastMessageSentTime = System.currentTimeMillis();
            return super.send(message);
        }

        @Override
        protected void handleRawMessage(SocketAddress sockaddr, Message message, SelectionKey sk) throws IOException {
            // This will capture DataExport, Association, and ConnectIndication
            // messages...
            // Opting not to update lastMessageREceived for ConnectIndications
            // .. since they are beacons and not part of the session
            super.handleRawMessage(sockaddr, message, sk);
        }

        @Override
        protected void handleAssociationMessage(SocketAddress sockaddr, AssociationMessage message) throws IOException {
            lastMessageReceived = System.currentTimeMillis();
            super.handleAssociationMessage(sockaddr, message);
        }

        @Override
        protected void handleDataExportMessage(DataExportError error) throws IOException {
            // Could do something context-sensitive here when a confirmed action
            // fails
            // Such as when setting the priority list returns "access denied"
            // for waveforms because another client is already receiving waves
            super.handleDataExportMessage(error);
        }

        @Override
        protected void handleDataExportMessage(DataExportMessage message) throws IOException {
            lastMessageReceived = System.currentTimeMillis();
            super.handleDataExportMessage(message);
        }

        @Override
        protected void handler(GetInterface get) {
            // TODO: what is this for?
            super.handler(get);
        }

        @Override
        protected void handler(SetResultInterface result, boolean confirmed) {
            // This is where we receive the response for the SET PRIORITY LIST REQUEST
            // The response informs us about the numerics and waveforms available among what
            // we asked to receive.

            super.handler(result, confirmed);
            AttributeValueList attrs = result.getAttributes();
            Attribute<TextIdList> ati = attrs.getAttribute(AttributeId.NOM_ATTR_POLL_NU_PRIO_LIST, TextIdList.class);

            if (null != ati && ati.getValue().containsAll(numericLabels.values().toArray(new Label[0]))) {
            } else {
                log.warn("Numerics priority list does not contain all of our requested labels:" + ati);
            }

            ati = attrs.getAttribute(AttributeId.NOM_ATTR_POLL_RTSA_PRIO_LIST, TextIdList.class);
            if (null != ati && ati.getValue().containsAll(sampleArrayLabels.values().toArray(new Label[0]))) {
            } else {
                log.warn("SampleArray priority list does not contain all requested labels:" + ati);
            }
        }

        @Override
        protected void handler(EventReportInterface eventReport, boolean confirm) throws IOException {
            // The super sends confirmations where appropriate by default
            super.handler(eventReport, confirm);
            switch (ObjectClass.valueOf(eventReport.getEventType().getType())) {
                case NOM_NOTI_MDS_CREAT:
                    MdsCreateEvent createEvent = (MdsCreateEvent) eventReport.getEvent();
                    AttributeValueList attrs = createEvent.getAttributes();
                    Attribute<SystemModel> asm = attrs.getAttribute(AttributeId.NOM_ATTR_ID_MODEL, SystemModel.class);
                    Attribute<drivers.philips.intellivue.data.String> as = attrs.getAttribute(AttributeId.NOM_ATTR_ID_BED_LABEL, drivers.philips.intellivue.data.String.class);

                    intellivueRelativeClock = new IntellivueRelativeClock();
                    intellivueRelativeClock.setDeviceStartTime(attrs);

                    if (null != asm) {
                        deviceIdentity.setManufacturer(asm.getValue().getManufacturer().getString());
                    }
                    switch (stateMachine.getState().ordinal()) {
                        case ConnectionState._Negotiating:
                            state(ConnectionState.Connected, "Received MDS Create Event");
                            break;
                    }

                    requestSinglePoll(ObjectClass.NOM_MOC_VMS_MDS, AttributeId.NOM_ATTR_GRP_SYS_PROD);

                    requestSet(numericLabels.values().toArray(new Label[0]), sampleArrayLabels.values().toArray(new Label[0]));

                    break;
                default:
                    break;
            }

        }

        @Override
        protected void handleAssociationMessage(SocketAddress sockaddr, AssociationRefuse message) {
            switch (stateMachine.getState().ordinal()) {
                case ConnectionState._Connected:
                    state(ConnectionState.Negotiating, "reconnecting after active association is later refused");
                    break;
                case ConnectionState._Terminal:
                    if(DisconnectState.Disconnecting.equals(disconnectState.getState())) {
                        disconnectState.transitionIfLegal(DisconnectState.Disconnected, "associating refused when disconnecting");
                    }
                    break;
                case ConnectionState._Negotiating:
                    setConnectionInfo("association refused, retrying...");
                    break;
            }

            super.handleAssociationMessage(sockaddr, message);
        }

        @Override
        protected void handleAssociationMessage(SocketAddress sockaddr, AssociationAbort message) {
            switch (stateMachine.getState().ordinal()) {
                case ConnectionState._Connected:
                    state(ConnectionState.Negotiating, "reconnecting after active association is later aborted");
                    break;
                case ConnectionState._Terminal:
                    if(DisconnectState.Disconnecting.equals(disconnectState.getState())) {
                        disconnectState.transitionIfLegal(DisconnectState.Disconnected, "association aborted!");
                    }
                    break;
            }
            super.handleAssociationMessage(sockaddr, message);
        }

        @Override
        protected void handleAssociationMessage(SocketAddress sockaddr, AssociationDisconnect message) {
            switch (stateMachine.getState().ordinal()) {
                case ConnectionState._Connected:
                    state(ConnectionState.Negotiating, "unexpected disconnect message");
                    break;
                case ConnectionState._Terminal:
                    if(DisconnectState.Disconnecting.equals(disconnectState.getState())) {
                        disconnectState.transitionIfLegal(DisconnectState.Disconnected, "association disconnected");
                    }
                    break;
            }
            super.handleAssociationMessage(sockaddr, message);
        }

        @Override
        protected void handleAssociationMessage(SocketAddress sockaddr, AssociationFinish message) throws IOException {
            super.handleAssociationMessage(sockaddr, message);
            switch (stateMachine.getState().ordinal()) {
                case ConnectionState._Connected:
                    state(ConnectionState.Negotiating, "unexpected disconnect message");
                    break;
                case ConnectionState._Terminal:
                    if(DisconnectState.Disconnecting.equals(disconnectState.getState())) {
                        disconnectState.transitionIfLegal(DisconnectState.Disconnected, "association disconnected unexpectedly");
                    }
                    break;
            }
        }

        @Override
        protected void handler(ConnectIndication connectIndication, SelectionKey sk) {
            log.trace("Received a connectindication:" + connectIndication);
            IPAddressInformation ipinfo = connectIndication.getIpAddressInformation();
            ProtocolSupport.ProtocolSupportEntry pse = acceptable(connectIndication);
            if (null != ipinfo && null != pse) {
                try {
                    InetAddress remote = ipinfo.getInetAddress();
                    int prefixLength = Network.prefixCount(connectIndication.getIpAddressInformation().getSubnetMask());

                    connect(remote, prefixLength, pse.getPortNumber());
                } catch (IOException e) {
                    log.error("Trying to connect to " + ipinfo + " after receiving a beacon", e);
                }

            }
        }

        protected long minPollPeriodToTimeout(long minPollPeriod) {
            if (minPollPeriod <= 3300L) {
                return 10000L;
            } else if (minPollPeriod <= 43000L) {
                return 3 * minPollPeriod;
            } else {
                return 130000L;
            }
        }

        @Override
        protected void handleAssociationMessage(SocketAddress sockaddr, AssociationAccept message) {
            PollProfileSupport pps = message.getUserInfo().getPollProfileSupport();
            long timeout = minPollPeriodToTimeout(pps.getMinPollPeriod().toMilliseconds());
            OUT_CONNECTION_ASSERT = Math.max(200L, timeout - 1000L);
            log.debug("Negotiated " + pps.getMinPollPeriod().toMilliseconds() + "ms min poll period, timeout=" + timeout);
            log.debug("Negotiated " + pps.getMaxMtuTx() + " " + pps.getMaxMtuRx() + " " + pps.getMaxBwTx());
            super.handleAssociationMessage(sockaddr, message);
        }

        @Override
        protected void handler(SinglePollDataResult result) {
            switch (ObjectClass.valueOf(result.getPolledObjType().getOidType().getType())) {
                case NOM_MOC_VMO_AL_MON:
                    switch (AttributeId.valueOf(result.getPolledAttributeGroup().getType())) {
                        case NOM_ATTR_GRP_VMO_STATIC:
                            // Responses to our "keep alive" messages occur here
                            // Currently we track any incoming message as proof of life
                            // ... so nothing special to do here
                            break;
                        case NOM_ATTR_GRP_AL_MON:
                            log.debug("Alert Monitor Information has arrived:" + result);
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }

            for (SingleContextPoll sop : result.getPollInfoList()) {
                for (ObservationPoll op : sop.getPollInfo()) {
                    AttributeValueList attrs = op.getAttributes();
                    Attribute<ProductionSpecification> prodSpec = attrs.getAttribute(AttributeId.NOM_ATTR_ID_PROD_SPECN, ProductionSpecification.class);
                    if (null != prodSpec) {
                        log.info("ProductionSpecification");
                        log.info("" + prodSpec.getValue());
                        VariableLabel serial = prodSpec.getValue().getByComponentId(ProductionSpecificationType.SERIAL_NUMBER, ComponentId.ID_COMP_PRODUCT);
                        VariableLabel part = prodSpec.getValue().getByComponentId(ProductionSpecificationType.PART_NUMBER, ComponentId.ID_COMP_PRODUCT);
                        if (null != serial) {
                            deviceIdentity.setSerialNumber(serial.getString());
                        } else {
                            log.warn("No serial number found in the ProductionSpecification");
                        }
                        if(null != part) {
                            deviceIdentity.setPartNumber(part.getString());
/*                            if("865240".equals(part.getString())) {
                                //iconOrBlank("MX800", "mx800.png");
                            } else {
                                //iconOrBlank("MP70", "mp70.png");
                            }*/
                        } else {
                            log.warn("No PART NUMBER for ID COMP PRODUCT");
                            //writeDeviceIdentity();
                            deviceIdentity.writeDI();
                        }

                    } else {
                        deviceIdentity.writeDI();
                        //writeDeviceIdentity();
                    }

                    Attribute<drivers.philips.intellivue.data.String> firstName = attrs.getAttribute(AttributeId.NOM_ATTR_PT_NAME_GIVEN, drivers.philips.intellivue.data.String.class);
                    Attribute<drivers.philips.intellivue.data.String> lastName = attrs.getAttribute(AttributeId.NOM_ATTR_PT_NAME_FAMILY, drivers.philips.intellivue.data.String.class);
                    Attribute<drivers.philips.intellivue.data.String> patientId = attrs.getAttribute(AttributeId.NOM_ATTR_PT_ID, drivers.philips.intellivue.data.String.class);

                    if (null != firstName) {

                    }
                    if (null != lastName) {

                    }
                    if (null != patientId) {

                    }
                }
            }
            super.handler(result);
        }

        protected void handlerAlert(AttributeValueList attrs) {
            Attribute<DeviceAlertCondition> deviceAlertCondition = attrs.getAttribute(AbstractIntellivueRunner.this.deviceAlertCondition);
            Attribute<DevAlarmList> patientAlertList = attrs.getAttribute(AbstractIntellivueRunner.this.patientAlertList);
            Attribute<DevAlarmList> technicalAlertList = attrs.getAttribute(AbstractIntellivueRunner.this.technicalAlertList);

            if (null != deviceAlertCondition) {
                log.debug("writeDeviceAlert(deviceAlertCondition.getValue().getDeviceAlertState().toString());");
                //writeDeviceAlert(deviceAlertCondition.getValue().getDeviceAlertState().toString());
            }

            if (null != patientAlertList) {
                log.debug("markOldPatientAlertInstances();");
                //markOldPatientAlertInstances();
                for (DevAlarmEntry dae : patientAlertList.getValue().getValue()) {
                    if (dae.getAlMonInfo() instanceof StrAlMonInfo) {
                        StrAlMonInfo info = (StrAlMonInfo) dae.getAlMonInfo();
                        String key = dae.getAlCode().getType() + "-" + dae.getAlSource().getType() + "-"
                                + dae.getObject().getGlobalHandleId().getMdsContext() + "-" + dae.getObject().getGlobalHandleId().getHandleId()
                                + "-" + dae.getObject().getOidType().getType();
                        log.debug("writePatientAlert(key, Normalizer.normalize(info.getString().getString(), Normalizer.Form.NFD).replaceAll(\"[^\\\\p{ASCII}]\", \"\"));");
                        //writePatientAlert(key, Normalizer.normalize(info.getString().getString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    }
                }
                log.debug("clearOldPatientAlertInstances();");
                //clearOldPatientAlertInstances();
            }
            if (null != technicalAlertList) {
                log.debug("markOldTechnicalAlertInstances();");
                //markOldTechnicalAlertInstances();
                for (DevAlarmEntry dae : technicalAlertList.getValue().getValue()) {
                    if (dae.getAlMonInfo() instanceof StrAlMonInfo) {
                        StrAlMonInfo info = (StrAlMonInfo) dae.getAlMonInfo();
                        String key = dae.getAlCode().getType() + "-" + dae.getAlSource().getType() + "-"
                                + dae.getObject().getGlobalHandleId().getMdsContext() + "-" + dae.getObject().getGlobalHandleId().getHandleId()
                                + "-" + dae.getObject().getOidType().getType();
                        log.debug("writeTechnicalAlert(key, Normalizer.normalize(info.getString().getString(), Normalizer.Form.NFD).replaceAll(\"[^\\\\p{ASCII}]\", \"\"));");
                        //writeTechnicalAlert(key, Normalizer.normalize(info.getString().getString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    }
                }
                log.debug("clearOldTechnicalAlertInstances();");
                //clearOldTechnicalAlertInstances();
            }
        }

        @Override
        protected void handler(ExtendedPollDataResult r) {
            ExtendedPollDataResultImpl result = (ExtendedPollDataResultImpl) r;
            log.debug(String.valueOf(result));

            if (intellivueRelativeClock == null) {
                log.error("intellivueRelativeClock not initialized with setDeviceStartTime!");
                return;
            }
            IceInstant deviceTime = intellivueRelativeClock.instantFromRelative(result.getRelativeTime());
            IceInstant referenceTime = this.referenceClock.instant();

            // we could track gaps in poll sequence numbers but instead we're
            // relying on consumers of the data
            // to observe a gap in the data timestamps
            if (result.getPolledObjType().getNomPartition().equals(NomPartition.Object)
                    && result.getPolledObjType().getOidType().getType() == ObjectClass.NOM_MOC_VMO_AL_MON.asInt()) {
                for (SingleContextPoll sop : result.getPollInfoList()) {
                    for (ObservationPoll op : sop.getPollInfo()) {
//                        int handlerPeriod = op.getHandleId().getHandleId();
                        handlerAlert(op.getAttributes());
                    }
                }
            } else {
                log.debug("Here begins a pollInfoList at time " + result.getRelativeTime() + " " + result.getAbsoluteTime());
                for (SingleContextPoll sop : result.getPollInfoList()) {
                    for (ObservationPoll op : sop.getPollInfo()) {
                        int handle = op.getHandleId().getHandleId();
                        AttributeValueList attrs = op.getAttributes();

                        // Here, depending on the type of data we received, we can call sub-handlerSampleArray functions:


                        // Unit-Code (from the dimension nomenclature partition) of the metric value.
                        Attribute<EnumValue<UnitCode>> unitCode = attrs.getAttribute(AbstractIntellivueRunner.this.unitCode);
                        if(null != unitCode) {
                            handlerUnitCode(handle, unitCode.getValue().getEnum());
                        }

                        // Numeric data
                        Attribute<NumericObservedValue> observed = attrs.getAttribute(AbstractIntellivueRunner.this.observed);
                        if (null != observed) {
                            handlerNumeric(handle, deviceTime, referenceTime, observed.getValue());
                        }

                        // Multiple Numeric data
                        Attribute<CompoundNumericObservedValue> compoundObserved = attrs.getAttribute(AbstractIntellivueRunner.this.compoundObserved);
                        if (null != compoundObserved) {
                            for (NumericObservedValue nov : compoundObserved.getValue().getList())
                                handlerNumeric(handle, deviceTime, referenceTime, nov);
                        }

                        // Specifies the time period over which values are sampled; only applicable if values are sampled periodically
                        Attribute<RelativeTime> period = attrs.getAttribute(AbstractIntellivueRunner.this.period);
                        if (null != period) {
                            handlerPeriod(handle, period.getValue());
                        }

                        Attribute<ScaleAndRangeSpecification> sar = attrs.getAttribute(AbstractIntellivueRunner.this.sar);
                        if (null != sar) {
                            handlerScaleAndRange(handle, sar.getValue());
                        }

                        // Sample Array Specification
                        Attribute<SampleArraySpecification> spec = attrs.getAttribute(AbstractIntellivueRunner.this.spec);
                        if (null != spec) {
                            handlerSampleArraySpec(handle, spec.getValue());
                        }

                        // Multiple Sample Array data
                        Attribute<SampleArrayCompoundObservedValue> cov = attrs.getAttribute(AbstractIntellivueRunner.this.cov);
                        if (null != cov) {
                            for (SampleArrayObservedValue saov : cov.getValue().getList())
                                handlerSampleArray(handle, deviceTime, saov);
                        }

                        Attribute<SampleArrayObservedValue> v = attrs.getAttribute(AbstractIntellivueRunner.this.v);
                        if (null != v) {
                            handlerSampleArray(handle, deviceTime, v.getValue());
                        }
                    }
                }
            }
            super.handler(result);
        }

        private final void handlerNumeric(int handle, IceInstant deviceTime, IceInstant referenceTime, NumericObservedValue observed) {
            // log.debug(observed.toString());
            ObservedValue observedValue = ObservedValue.valueOf(observed.getPhysioId().getType());
            if (null != observedValue) {
                String rosettaMetric = numericRosettaMetrics.get(observedValue);

                // Here rosettaMetric is null if no mapping has already been done
                if (rosettaMetric == null) {
                    log.warn("Numeric metric not mapped to rosetta metric:" + observed);
                }

                UnitCode unit = UnitCode.valueOf(observed.getUnitCode().getType());

                if (observed.getMsmtState().isUnavailable())
                    putNumericUpdate(observedValue, handle, numericSample(getNumericUpdate(observedValue, handle), (Float) null, rosettaMetric, observedValue.toString(), handle, PhilipsToRosettaMapping.units(unit), deviceTime, referenceTime));
                else
                    putNumericUpdate(observedValue, handle, numericSample(getNumericUpdate(observedValue, handle), observed.getValue().floatValue(), rosettaMetric, observedValue.toString(), handle, PhilipsToRosettaMapping.units(unit), deviceTime, referenceTime));
            }
            else {
                log.warn("Unknown Observed Value: PhysioIdType=" + observed.getPhysioId().getType() + ", observed=" + observed);
            }

        }



        private final InstanceHolder<Numeric> getNumericUpdate(ObservedValue ov, int handle) {
            Map<Integer, InstanceHolder<Numeric>> forObservedValue = numericUpdates.get(ov);
            if (null == forObservedValue) {
                return null;
            } else {
                return forObservedValue.get(handle);
            }
        }



        private final void putNumericUpdate(ObservedValue ov, int handle, InstanceHolder<Numeric> value) {
            Map<Integer, InstanceHolder<Numeric>> forObservedValue = numericUpdates.get(ov);
            if (null == forObservedValue) {
                forObservedValue = new HashMap<>();
                numericUpdates.put(ov, forObservedValue);
            }
            forObservedValue.put(handle, value);
        }



        protected void handlerSampleArray(int handle, IceInstant deviceTime, SampleArrayObservedValue saov) {
            short[] bytes = saov.getValue();
            ObservedValue ov = ObservedValue.valueOf(saov.getPhysioId().getType());
            if (null == ov) {
                log.warn("No ObservedValue for " + saov.getPhysioId().getType());
            } else {
                String rosettaMetric = sampleArrayRosettaMetrics.get(ov);
                if (null == rosettaMetric) {
                    log.warn("No rosettaMetric for " + ov);
                } else {
                    SampleArraySpecification sampleArraySpecification = handleToSampleArraySpecification.get(handle);
                    ScaleAndRangeSpecification scaleAndRangeSpecification = handleToScaleAndRangeSpecification.get(handle);
                    UnitCode unitCode = handleToUnitCode.get(handle);
                    RelativeTime relativeTime = handleToUpdatePeriod.get(handle);
                    if (null == sampleArraySpecification || null == relativeTime || null == scaleAndRangeSpecification || null == unitCode) {
                        log.warn("No SampleArraySpecification or RelativeTime for handlerPeriod=" + handle + " rt=" + relativeTime + " sas=" + sampleArraySpecification + " sar=" + scaleAndRangeSpecification + " unitCode=" + unitCode);
                    } else {
                        int cnt = sampleArraySpecification.getArraySize();
                        // TODO these were once cached, no?
                        MySampleArray w = new MySampleArray(sampleArraySpecification, scaleAndRangeSpecification);

                        int cnt_sa = saov.getLength() / (sampleArraySpecification.getSampleSize() / Byte.SIZE);

                        if (cnt_sa < cnt) {
                            log.warn("Ignoring insufficient data (" + cnt_sa + ") in the samplearray observation when " + cnt + " expected for " + ov + " " + handle + " v.getLength()=" + saov.getLength() + " sampleSize=" + sampleArraySpecification.getSampleSize());
                            return;
                        } else {
                            if (cnt < cnt_sa) {
                                log.debug("Expanding to accomodate " + cnt_sa + " samples where only " + cnt + " were expected");
                                w.setArraySize(cnt_sa);
                                cnt = cnt_sa;
                            }
                            if (w.getArraySize() < cnt) {
                                log.debug("Expanding to accomodate " + cnt + " samples where " + w.getArraySize() + " were expected");
                                w.setArraySize(cnt);
                            }

                            //
                            for (int i = 0; i < cnt; i++) {
                                w.applyValue(i, bytes);
                            }

                            Map<Integer, SampleCache> handleToSampleCache = sampleArrayCache.computeIfAbsent(ov, k -> Collections.synchronizedMap(new HashMap<>()));
                            SampleCache sampleCache = handleToSampleCache.get(handle);
                            if(null == sampleCache) {
                                sampleCache = new SampleCache();
                                handleToSampleCache.put(handle, sampleCache);
                            }

                            sampleCache.addNewSamples(w.getNumbers(), deviceTime);
                        }
                    }
                }
            }
        }


        protected void handlerScaleAndRange(int handle, ScaleAndRangeSpecification sar) {
            handleToScaleAndRangeSpecification.put(handle, sar.clone());
            if(log.isTraceEnabled()) {
                log.trace("Received a ScaleAndRangeSpecification for " + handle + " " + sar);
            }
        }

        protected void handlerUnitCode(int handle, UnitCode unitCode) {
            handleToUnitCode.put(handle, unitCode);
            if(log.isTraceEnabled()) {
                log.trace("Received a unitCode for " + handle + " " + unitCode);
            }
        }

        protected void handlerSampleArraySpec(int handle, SampleArraySpecification spec) {
            handleToSampleArraySpecification.put(handle, spec.clone());
            if(log.isTraceEnabled()) {
                log.trace("Received a SampleArraySpecification for " + handle + " " + spec);
            }
        }

        protected void handlerPeriod(int handle, RelativeTime period) {
            RelativeTime newPeriod = handleToUpdatePeriod.get(handle);
            if (null == newPeriod) {
                newPeriod = new RelativeTime();
                handleToUpdatePeriod.put(handle, newPeriod);
            }
            newPeriod.fromOther(period);
            System.out.println(Integer.toString(handle) + newPeriod);
        }
    }


    static void loadMap(Map<ObservedValue, String> numericrosettaMetrics,
                        Map<ObservedValue, Label> numericLabels,
                        Map<ObservedValue, String> sampleArrayrosettaMetrics,
                        Map<ObservedValue, Label> sampleArrayLabels) {
        try {
            String map =
                    "NOM_VOL_MINUTE_AWAY\tMDC_VOL_MINUTE_AWAY\tNLS_NOM_VOL_MINUTE_AWAY\tN\n" +
                    "NOM_VOL_AWAY_TIDAL\tMDC_VOL_AWAY_TIDAL\tNLS_NOM_VOL_AWAY_TIDAL\tN\n" +
                    "NOM_VENT_RESP_RATE\tMDC_VENT_RESP_RATE\tNLS_NOM_VENT_RESP_RATE\tN\n" +
                    "NOM_AWAY_RESP_RATE\tMDC_AWAY_RESP_RATE\tNLS_NOM_AWAY_RESP_RATE\tN\n" +
                    "NOM_RESP_RATE_SPONT\tMDC_RESP_RATE_SPONT\tNLS_NOM_RESP_RATE_SPONT\tN\n" +
                    "NOM_VENT_RESP_RATE_MAND\tMDC_VENT_RESP_RATE_MAND\tNLS_NOM_VENT_RESP_RATE_MAND\tN\n" +
                    "NOM_RESP_RATE\tMDC_RESP_RATE\tNLS_NOM_RESP_RATE\tN\n" +
                    "NOM_AWAY_CO2_ET\tMDC_AWAY_CO2_ET\tNLS_NOM_AWAY_CO2\tN\n" +
                    "NOM_PULS_OXIM_SAT_O2\tMDC_PULS_OXIM_SAT_O2\tNLS_NOM_PULS_OXIM_SAT_O2\tN\n" +
                    "NOM_PLETH_PULS_RATE\tMDC_PULS_OXIM_PULS_RATE\tNLS_NOM_PULS_OXIM_PULS_RATE\tN\n" +
                    "NOM_PULS_RATE\tMDC_PULS_RATE\tNLS_NOM_PULS_RATE\tN\n" +
                    "NOM_ECG_CARD_BEAT_RATE\tMDC_ECG_HEART_RATE\tNLS_NOM_ECG_CARD_BEAT_RATE\tN\n" +
                    "# This is the named numeric we want in the priority list\n" +
                    "NOM_PRESS_BLD_NONINV\tMDC_PRESS_BLD_NONINV\tNLS_NOM_PRESS_BLD_NONINV\tN\n" +
                    "# These will be elements of the compound observed value for that numeric\n" +
                    "NOM_PRESS_BLD_NONINV_DIA\tMDC_PRESS_CUFF_DIA\tNLS_NOM_PRESS_BLD_NONINV\tN\n" +
                    "NOM_PRESS_BLD_NONINV_SYS\tMDC_PRESS_CUFF_SYS\tNLS_NOM_PRESS_BLD_NONINV\tN\n" +
                    "NOM_PRESS_BLD_NONINV_PULS_RATE\tMDC_PULS_RATE_NON_INV\tNLS_NOM_PRESS_BLD_NONINV_PULS_RATE\tN\n" +
                    "NOM_PRESS_BLD_NONINV_MEAN\tMDC_PRESS_BLD_NONINV_MEAN\tNLS_NOM_PRESS_BLD_NONINV\tN\n" +
                    "NOM_RESP\tMDC_IMPED_TTHOR\tNLS_NOM_RESP\tW\n" +
                    "NOM_AWAY_CO2\tMDC_AWAY_CO2\tNLS_NOM_AWAY_CO2\tW\n" +
                    "NOM_PLETH\tMDC_PULS_OXIM_PLETH\tNLS_NOM_PULS_OXIM_PLETH\tW\n" +
                    "NOM_ECG_ELEC_POTL_I\tMDC_ECG_LEAD_I\tNLS_NOM_ECG_ELEC_POTL_I\tW\n" +
                    "NOM_ECG_ELEC_POTL_II\tMDC_ECG_LEAD_II\tNLS_NOM_ECG_ELEC_POTL_II\tW\n" +
                    "NOM_ECG_ELEC_POTL_III\tMDC_ECG_LEAD_III\tNLS_NOM_ECG_ELEC_POTL_III\tW\n" +
                    "NOM_ECG_ELEC_POTL_AVF\tMDC_ECG_LEAD_AVF\tNLS_NOM_ECG_ELEC_POTL_AVF\tW\n" +
                    "NOM_ECG_ELEC_POTL_AVL\tMDC_ECG_LEAD_AVL\tNLS_NOM_ECG_ELEC_POTL_AVL\tW\n" +
                    "NOM_ECG_ELEC_POTL_AVR\tMDC_ECG_LEAD_AVR\tNLS_NOM_ECG_ELEC_POTL_AVR\tW\n" +
                    "NOM_ECG_ELEC_POTL_V2\tMDC_ECG_LEAD_V2\tNLS_NOM_ECG_ELEC_POTL_V2\tW\n" +
                    "NOM_ECG_ELEC_POTL_V5\tMDC_ECG_LEAD_V5\tNLS_NOM_ECG_ELEC_POTL_V5\tW\n" +
                    "NOM_PRESS_BLD_ART_ABP\tMDC_PRESS_BLD_ART_ABP\tNLS_NOM_PRESS_BLD_ART_ABP\tW\n" +
                    "NOM_PRESS_BLD_ART\tMDC_PRESS_BLD_ART\tNLS_NOM_PRESS_BLD_ART\tW\n" +
                    "\n" + "\n";
            BufferedReader br = new BufferedReader(new StringReader(map));
            String line = null;

            while (null != (line = br.readLine())) {
                line = line.trim();
                if (line.length() > 0 && '#' != line.charAt(0)) {
                    String v[] = line.split("\t");

                    if (v.length < 4) {
                        log.debug("Bad line:" + line);
                    } else {
                        ObservedValue ov = ObservedValue.valueOf(v[0]);
                        String rosettaMetric = v[1];
                        Label label = Label.valueOf(v[2]);

                        log.trace("Adding " + ov + " mapped to " + rosettaMetric + " with label " + label);
                        v[3] = v[3].trim();
                        if ("W".equals(v[3])) {
                            sampleArrayLabels.put(ov, label);
                            sampleArrayrosettaMetrics.put(ov, rosettaMetric);
                        } else if ("N".equals(v[3])) {
                            numericLabels.put(ov, label);
                            numericrosettaMetrics.put(ov, rosettaMetric);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Maps from a handle_id (the type of data received)
    protected final Map<Integer, RelativeTime> handleToUpdatePeriod = new HashMap<>();
    protected final Map<Integer, SampleArraySpecification> handleToSampleArraySpecification = new HashMap<>();
    protected final Map<Integer, ScaleAndRangeSpecification> handleToScaleAndRangeSpecification = new HashMap<>();
    protected final Map<Integer, UnitCode> handleToUnitCode = new HashMap<>();


    // ALERTS
    protected final Attribute<DeviceAlertCondition> deviceAlertCondition            = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_DEV_AL_COND, DeviceAlertCondition.class);
    protected final Attribute<DevAlarmList> patientAlertList                        = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_AL_MON_P_AL_LIST, DevAlarmList.class);
    protected final Attribute<DevAlarmList> technicalAlertList                      = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_AL_MON_T_AL_LIST, DevAlarmList.class);

    // OBSERVATIONS
    protected final Attribute<CompoundNumericObservedValue> compoundObserved        = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_NU_CMPD_VAL_OBS, CompoundNumericObservedValue.class);
    protected final Attribute<NumericObservedValue> observed                        = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_NU_VAL_OBS, NumericObservedValue.class);
    protected final Attribute<SampleArrayObservedValue> v                           = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_SA_VAL_OBS, SampleArrayObservedValue.class);
    protected final Attribute<SampleArrayCompoundObservedValue> cov                 = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_SA_CMPD_VAL_OBS, SampleArrayCompoundObservedValue.class);

    //
    protected final Attribute<Type> type                                            = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_TYPE, Type.class);
    protected final Attribute<RelativeTime> period                                  = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_TIME_PD_SAMP, RelativeTime.class);
    protected final Attribute<SampleArraySpecification> spec                        = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_SA_SPECN, SampleArraySpecification.class);
    protected final Attribute<ScaleAndRangeSpecification> sar                       = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_SCALE_SPECN_I16, ScaleAndRangeSpecification.class);
    protected final Attribute<EnumValue<UnitCode>> unitCode                         = AttributeFactory.getEnumAttribute(AttributeId.NOM_ATTR_UNIT_CODE.asOid(), UnitCode.class);

    // UNUSED
    protected final Attribute<MetricSpecification> metricSpecification              = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_METRIC_SPECN, MetricSpecification.class);
    protected final Attribute<TextId> idLabel                                       = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_LABEL, TextId.class);
    protected final Attribute<drivers.philips.intellivue.data.String> idLabelString = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_LABEL_STRING, drivers.philips.intellivue.data.String.class);
    protected final Attribute<DisplayResolution> displayResolution                  = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_DISP_RES, DisplayResolution.class);
     protected final Attribute<EnumValue<SimpleColor>> color                         = AttributeFactory.getEnumAttribute(AttributeId.NOM_ATTR_COLOR.asOid(), SimpleColor.class);
    protected final Attribute<HandleId> handle                                      = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_HANDLE, HandleId.class);
}

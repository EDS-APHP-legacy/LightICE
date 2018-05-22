package runner;

import common.DeviceIdentity;
import common.io.util.StateMachine;
import common.net.NetworkLoop;
import common.time.IceInstant;
import datatypes.Data;
import datatypes.Numeric;
import export.writers.Writer;
import fakedds.DataWriter;
import fakedds.InstanceHandle_t;
import datatypes.SampleArray;
import ice.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.philips.SerialIntellivueRunner;
import runner.philips.EthernetIntellivueRunner;
import utils.Device;

import java.io.IOException;
import java.util.*;

public abstract class AbstractDeviceRunner {
    protected static final Logger log = LoggerFactory.getLogger(AbstractDeviceRunner.class);
    protected NetworkLoop networkLoop;
    protected Thread networkLoopThread;
    protected final DeviceConnectivity deviceConnectivity = new DeviceConnectivity();
    private InstanceHandle_t deviceConnectivityHandle;
    protected DeviceIdentity deviceIdentity;
    protected Device device;
    private List<Writer> listeners;

    public static ThreadGroup threadGroup = new ThreadGroup(Thread.currentThread().getThreadGroup(), "AbstractDeviceRunner") {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("Thrown by " + t.toString(), e);
            super.uncaughtException(t, e);
        }
    };

    protected final StateMachine<ConnectionState> stateMachine = new StateMachine<>(legalTransitions, ConnectionState.Initial,
            (ConnectionState newState, ConnectionState oldState, String transitionNote) -> {
                //stateChanging(newState, oldState, transitionNote);
                log.debug(oldState + "==>" + newState + " (" + transitionNote + ")");
                deviceConnectivity.state = newState;
                deviceConnectivity.info = transitionNote;
                InstanceHandle_t handle = deviceConnectivityHandle;
                if (handle != null) {
                    writeDeviceConnectivity();
                }
                stateChanged(newState, oldState, transitionNote);
            });



    public AbstractDeviceRunner(DeviceIdentity deviceIdentity) throws IOException {
        this.deviceIdentity = deviceIdentity;
        listeners = new ArrayList<>();

        deviceConnectivity.type = getConnectionType();
        deviceConnectivity.state = getState();

        networkLoop = new NetworkLoop();
        networkLoopThread = new Thread(AbstractDeviceRunner.threadGroup, () -> {
            try {
                networkLoop.runLoop();
            } finally {
                log.info("NetworkLoop.runLoop ended");
            }
        }, "Network Loop");
        networkLoopThread.setDaemon(true);
        networkLoopThread.start();
    }



    // Disconnected -> Connecting -> Negotiating -> Connected -> Disconnecting -> Disconnected
    private static final ConnectionState[][] legalTransitions = new ConnectionState[][] {
            // Normal "flow"
            // A "connect" was requested, from this transition on the device adapter will
            // attempt to maintain / re-establish connectivity
            { ConnectionState.Initial, ConnectionState.Connecting },
            // Connection was established
            { ConnectionState.Connecting, ConnectionState.Negotiating },
            // Connection still open but no active session (silence on the
            // RS-232 line for example)
            { ConnectionState.Connected, ConnectionState.Negotiating },
            // A fatal error in data processing that has caused us to close the connection
            // and to attempt to reopen it
            { ConnectionState.Connected, ConnectionState.Connecting },
            // Negotiation was successful
            { ConnectionState.Negotiating, ConnectionState.Connected },
            // A lack of an open connection while trying to attempt to negotiate
            { ConnectionState.Negotiating, ConnectionState.Connecting },
            // Explicit disconnect has been invoked, the Terminal state is Terminal
            // A fatal error occurred in the Negotiating state
            { ConnectionState.Negotiating, ConnectionState.Terminal },
            // A fatal error occurred in the Connecting state
            { ConnectionState.Connecting, ConnectionState.Terminal },
            // A fatal error occurred in the Connected state
            { ConnectionState.Connected, ConnectionState.Terminal },
    };

    protected void stateChanged(ConnectionState newState, ConnectionState oldState, String transitionNote) {
        if (ConnectionState.Connected.equals(oldState) && !ConnectionState.Connected.equals(newState)) {
            //eventLoop.doLater(() -> unregisterAllInstances());
        }
    }

    protected void writeDeviceConnectivity() {
        deviceConnectivity.unique_device_identifier = deviceIdentity.getUniqueDeviceIdentifier();
        if (null == deviceConnectivity.unique_device_identifier || "".equals(deviceConnectivity.unique_device_identifier)) {
            throw new IllegalStateException("No UDI");
        }
        if (null == deviceConnectivityHandle) {
            deviceConnectivityHandle = DataWriter.register_instance(deviceConnectivity);
        }
        DataWriter.write(deviceConnectivity, deviceConnectivityHandle);
    }

    protected void setConnectionInfo(String connectionInfo) {
        if (null == connectionInfo) {
            // TODO work on nullity semantics
            log.warn("Attempt to set connectionInfo null");
            connectionInfo = "";
        }
        if (!connectionInfo.equals(deviceConnectivity.info)) {
            deviceConnectivity.info = connectionInfo;
            writeDeviceConnectivity();
        }
    }

    public abstract boolean connect();
    public abstract void disconnect();

    protected abstract ConnectionType getConnectionType();
    public ConnectionState getState() {
        return stateMachine.getState();
    }

    public void shutdown() {

        if (null != deviceConnectivityHandle) {
            InstanceHandle_t handle = deviceConnectivityHandle;
            deviceConnectivityHandle = null;
            //deviceConnectivityWriter.dispose(deviceConnectivity, handlerPeriod);

        }
        //publisher.delete_datawriter(deviceConnectivityWriter);
        //domainParticipant.delete_topic(deviceConnectivityTopic);

        log.info("AbstractDeviceRunner.shutdown");
    }
    private final List<InstanceHolder<SampleArray>> registeredSampleArrayInstances = new ArrayList<InstanceHolder<SampleArray>>();
    private final List<InstanceHolder<Numeric>> registeredNumericInstances = new ArrayList<InstanceHolder<Numeric>>();
/*    private final List<InstanceHolder<AlarmLimit>> registeredAlarmLimitInstances = new ArrayList<InstanceHolder<AlarmLimit>>();
    private final List<InstanceHolder<LocalAlarmLimitObjective>> registeredAlarmLimitObjectiveInstances = new ArrayList<InstanceHolder<LocalAlarmLimitObjective>>();
    private final Map<String, InstanceHolder<Alert>> patientAlertInstances = new HashMap<String, InstanceHolder<Alert>>();
    private final Map<String, InstanceHolder<Alert>> technicalAlertInstances = new HashMap<String, InstanceHolder<Alert>>();
    private final Set<String> oldPatientAlertInstances = new HashSet<String>();
    private final Set<String> oldTechnicalAlertInstances = new HashSet<String>();*/

    protected InstanceHolder<SampleArray> createSampleArrayInstance(String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit, int frequency) {
        if (deviceIdentity == null || deviceIdentity.getUniqueDeviceIdentifier() == null || "".equals(deviceIdentity.getUniqueDeviceIdentifier())) {
            throw new IllegalStateException("Please populate deviceIdentity.unique_device_identifier before calling createSampleArrayInstance");
        }

        InstanceHolder<SampleArray> holder = new InstanceHolder<>();
        holder.data = new SampleArray();
        holder.data.deviceIdentity = deviceIdentity;
        holder.data.setRosettaMetric(rosettaMetric);
        holder.data.setRosettaUnit(rosettaUnit);
        holder.data.vendorMetric = vendor_rosettaMetric;
        holder.data.instanceId = instance_id;
        holder.data.frequency = frequency;

        holder.handle = null;
        //holder.handlerPeriod = sampleArrayDataWriter.register_instance(holder.data);

/*        if(holder.handlerPeriod.is_nil()) {
            log.warn("Unable to register instance " + holder.data);
            holder.handlerPeriod = null;
        } else {
            registeredSampleArrayInstances.add(holder);
        }*/
        return holder;
    }

    protected void unregisterSampleArrayInstance(InstanceHolder<SampleArray> holder) {

        registeredSampleArrayInstances.remove(holder);

        //sampleArrayDataWriter.unregister_instance(holder.data, holder.handlerPeriod);
    }

    private InstanceHolder<SampleArray> ensureHolderConsistency(InstanceHolder<SampleArray> holder,
                                                                String rosettaMetric, String vendor_rosettaMetric, int instance_id,
                                                                String rosettaUnit, int frequency) {

        if (null != holder && (!holder.data.getRosettaMetric().equals(rosettaMetric) ||
                !holder.data.vendorMetric.equals(vendor_rosettaMetric) ||
                holder.data.instanceId != instance_id ||
                holder.data.frequency != frequency ||
                !holder.data.getRosettaUnit().equals(rosettaUnit))) {

            unregisterSampleArrayInstance(holder);
            holder = null;
        }
        return holder;
    }
//
//    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
//                                                            Number[] newValues,
//                                                            String rosettaMetric, String vendor_rosettaMetric, String rosettaUnit, int frequency,
//                                                            IceInstant timestamp) {
//        return sampleArraySample(holder, newValues, rosettaMetric, vendor_rosettaMetric, 0, rosettaUnit, frequency, timestamp);
//    }
//
//
//    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
//                                                                Number[] newValues, int len,
//                                                                String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit, int frequency,
//                                                                IceInstant timestamp) {
//        return sampleArraySample(holder, new ArrayContainer<>(newValues, len), rosettaMetric, vendor_rosettaMetric, instance_id, rosettaUnit, frequency, timestamp);
//    }
//
//    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
//                                                                Number[] newValues,
//                                                                String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit, int frequency,
//                                                                IceInstant timestamp) {
//        return sampleArraySample(holder, new ArrayContainer<>(newValues), rosettaMetric, vendor_rosettaMetric, instance_id, rosettaUnit, frequency, timestamp);
//    }

    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
                                                          Pair<IceInstant, List<Number>> newValues,
                                                          String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit, int frequency,
                                                          IceInstant referenceTime) {

        holder = ensureHolderConsistency(holder, rosettaMetric, vendor_rosettaMetric, instance_id, rosettaUnit, frequency);

        if (newValues != null) {
            // Call this now so that resolution of instance registration timestamp
            // is reduced
            referenceTime = referenceTime.refineResolutionForFrequency(frequency, newValues.getValue().size());
            if (null == holder) {
                holder = createSampleArrayInstance(rosettaMetric, vendor_rosettaMetric, instance_id, rosettaUnit, frequency);
            }
            sampleArraySample(holder.data, newValues.getValue(), newValues.getKey(), referenceTime);
        } else {
            if (holder != null) {
                unregisterSampleArrayInstance(holder);
                holder = null;
            }
        }
        return holder;
    }
    protected void sampleArraySample(SampleArray sampleArray, Number[] newValues, IceInstant deviceTime, IceInstant referenceTime) {
        sampleArraySample(sampleArray, new ArrayContainer<>(newValues), deviceTime, referenceTime);
    }

    protected void sampleArraySample(SampleArray sampleArray, Collection<Number> newValues, IceInstant deviceTime, IceInstant referenceTime) {
        sampleArraySample(sampleArray, new CollectionContainer<>(newValues), deviceTime, referenceTime);
    }

    private void sampleArraySample(SampleArray data, NullSaveContainer<Number> newValues, IceInstant deviceTime, IceInstant referenceTime) {
        data.setTime(deviceTime, referenceTime);
        fill(data, newValues);
        publish(data);
    }

    protected InstanceHolder<Numeric> createNumericInstance(String rosettaMetric, String vendor_rosettaMetric) {
        return createNumericInstance(rosettaMetric, vendor_rosettaMetric, 0);
    }

    protected InstanceHolder<Numeric> createNumericInstance(String rosettaMetric, String vendor_rosettaMetric, int instance_id) {
        return createNumericInstance(rosettaMetric, vendor_rosettaMetric, instance_id, rosetta.MDC_DIM_DIMLESS.VALUE);
    }

    protected InstanceHolder<Numeric> createNumericInstance(String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit) {
        if (deviceIdentity == null || deviceIdentity.getUniqueDeviceIdentifier() == null || "".equals(deviceIdentity.getUniqueDeviceIdentifier())) {
            throw new IllegalStateException("Please populate deviceIdentity.unique_device_identifier before calling createNumericInstance");
        }

        InstanceHolder<Numeric> holder = new InstanceHolder<>();
        holder.data = new Numeric();
        holder.data.deviceIdentity = deviceIdentity;
        holder.data.setRosettaMetric(rosettaMetric);
        holder.data.vendorMetric = vendor_rosettaMetric;
        holder.data.instanceId = instance_id;
        holder.data.setRosettaUnit(rosettaUnit);

        holder.handle = null;
        //holder.handlerPeriod = numericDataWriter.register_instance(holder.data);

        registeredNumericInstances.add(holder);
        return holder;
    }
    protected void unregisterNumericInstance(InstanceHolder<Numeric> holder) {
        if (null != holder) {
            registeredNumericInstances.remove(holder);
            log.debug("numericDataWriter.unregister_instance(holder.data, holder.handlerPeriod);");
            //numericDataWriter.unregister_instance(holder.data, holder.handlerPeriod);
        }
    }

    // For convenience
    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Integer newValue, String rosettaMetric, String vendor_rosettaMetric, String rosettaUnit, IceInstant deviceTime, IceInstant referenceTime) {
        return numericSample(holder, null == newValue ? null : newValue.floatValue(), rosettaMetric, vendor_rosettaMetric, rosettaUnit, deviceTime, referenceTime);
    }

    // For convenience
    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Integer newValue, String rosettaMetric, String vendor_rosettaMetric, int instance_id, IceInstant deviceTime, IceInstant referenceTime) {
        return numericSample(holder, null == newValue ? null : newValue.floatValue(), rosettaMetric, vendor_rosettaMetric, instance_id, rosetta.MDC_DIM_DIMLESS.VALUE, deviceTime, referenceTime);
    }

    // For convenience
    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Integer newValue, String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit, IceInstant deviceTime, IceInstant referenceTime) {
        return numericSample(holder, null == newValue ? null : newValue.floatValue(), rosettaMetric, vendor_rosettaMetric, instance_id, rosettaUnit, deviceTime, referenceTime);
    }

    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Float newValue, String rosettaMetric, String vendor_rosettaMetric, String rosettaUnit, IceInstant deviceTime, IceInstant referenceTime) {
        return numericSample(holder, newValue, rosettaMetric, vendor_rosettaMetric, 0, rosettaUnit, deviceTime, referenceTime);
    }

    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Float newValue, String rosettaMetric, String vendor_rosettaMetric, int instance_id, IceInstant deviceTime, IceInstant referenceTime) {
        return numericSample(holder, newValue, rosettaMetric, vendor_rosettaMetric, instance_id, rosetta.MDC_DIM_DIMLESS.VALUE, deviceTime, referenceTime);
    }

    protected void numericSample(Numeric numeric, float newValue, IceInstant deviceTime, IceInstant referenceTime) {
        numeric.value = newValue;

        numeric.setTime(deviceTime, referenceTime);

        publish(numeric);
        //numericDataWriter.write(holder.data, holder.handlerPeriod);
    }

    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Float newValue, String rosettaMetric, String vendor_rosettaMetric, int instance_id, String rosettaUnit, IceInstant deviceTime, IceInstant referenceTime) {

        if (holder != null && (!holder.data.getRosettaMetric().equals(rosettaMetric) || !holder.data.vendorMetric.equals(vendor_rosettaMetric) || holder.data.instanceId != instance_id || !holder.data.getRosettaUnit().equals(rosettaUnit))) {
            unregisterNumericInstance(holder);
            holder = null;
        }
        if (null != newValue) {
            if (null == holder) {
                holder = createNumericInstance(rosettaMetric, vendor_rosettaMetric, instance_id, rosettaUnit);
            }
            numericSample(holder.data, newValue, deviceTime, referenceTime);
        } else {
            if (null != holder) {
                unregisterNumericInstance(holder);
                holder = null;
            }
        }
        return holder;
    }
    private void fill(SampleArray sampleArray, NullSaveContainer<Number> newValues) {
        sampleArray.values.userData.clear();
        if(!newValues.isNull()) {
            Iterator<Number> iter = newValues.iterator();
            while (iter.hasNext()) {
                Number n = iter.next();
                sampleArray.values.userData.addFloat(n.floatValue());
            }
        }
    }

    private void publish(Data data) {
        for (Writer p : this.listeners) {
            p.write(this.deviceIdentity, data);
        }

        log.debug("publish<" + data.getClass() + ">.data = " + data);
    }

    public static AbstractDeviceRunner resolveRunner(DeviceIdentity deviceIdentity) throws IOException, ClassNotFoundException {
        String driverName = deviceIdentity.getDriver();

        switch (driverName) {
            case "philips_rs232": {
                deviceIdentity.setManufacturer("Philips");
                deviceIdentity.setModel("Intellivue");
                return new SerialIntellivueRunner(deviceIdentity);
            }
            case "philips_ethernet": {
                deviceIdentity.setManufacturer("Philips");
                deviceIdentity.setModel("Intellivue");
                return new EthernetIntellivueRunner(deviceIdentity);
            }
            default: throw new ClassNotFoundException("Class for the driver " + driverName + " not available.");
        }
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void addListener(Writer listener) {
        this.listeners.add(listener);
    }
}

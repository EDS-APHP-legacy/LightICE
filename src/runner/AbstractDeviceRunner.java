package runner;

import common.DeviceClock;
import common.DeviceIdentity;
import common.io.util.StateMachine;
import common.net.NetworkLoop;
import datatypes.Data;
import datatypes.Numeric;
import export.writers.Writer;
import fakedds.DataWriter;
import fakedds.InstanceHandle_t;
import datatypes.SampleArray;
import ice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.philips.SerialIntellivueRunner;
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
            //deviceConnectivityWriter.dispose(deviceConnectivity, handler);

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

    protected InstanceHolder<SampleArray> createSampleArrayInstance(String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, int frequency) {
        if (deviceIdentity == null || deviceIdentity.getUniqueDeviceIdentifier() == null || "".equals(deviceIdentity.getUniqueDeviceIdentifier())) {
            throw new IllegalStateException("Please populate deviceIdentity.unique_device_identifier before calling createSampleArrayInstance");
        }

        InstanceHolder<SampleArray> holder = new InstanceHolder<SampleArray>();
        holder.data = new SampleArray();
        holder.data.deviceIdentity = deviceIdentity;
        holder.data.metricId = metric_id;
        holder.data.vendorMetricId = vendor_metric_id;
        holder.data.instanceId = instance_id;
        holder.data.rosettaCode = rosettaCode;
        holder.data.frequency = frequency;

        holder.handle = null;
        //holder.handler = sampleArrayDataWriter.register_instance(holder.data);

/*        if(holder.handler.is_nil()) {
            log.warn("Unable to register instance " + holder.data);
            holder.handler = null;
        } else {
            registeredSampleArrayInstances.add(holder);
        }*/
        return holder;
    }

    protected void unregisterSampleArrayInstance(InstanceHolder<SampleArray> holder) {

        registeredSampleArrayInstances.remove(holder);

        //sampleArrayDataWriter.unregister_instance(holder.data, holder.handler);
    }

    private InstanceHolder<SampleArray> ensureHolderConsistency(InstanceHolder<SampleArray> holder,
                                                                String metric_id, String vendor_metric_id, int instance_id,
                                                                String rosettaCode, int frequency) {

        if (null != holder && (!holder.data.metricId.equals(metric_id) ||
                !holder.data.vendorMetricId.equals(vendor_metric_id) ||
                holder.data.instanceId != instance_id ||
                holder.data.frequency != frequency ||
                !holder.data.rosettaCode.equals(rosettaCode))) {

            unregisterSampleArrayInstance(holder);
            holder = null;
        }
        return holder;
    }

    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
                                                            Number[] newValues,
                                                            String metric_id, String vendor_metric_id, String rosettaCode, int frequency,
                                                            DeviceClock.Reading timestamp) {
        return sampleArraySample(holder, newValues, metric_id, vendor_metric_id, 0, rosettaCode, frequency, timestamp);
    }


    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
                                                                Number[] newValues, int len,
                                                                String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, int frequency,
                                                                DeviceClock.Reading timestamp) {
        return sampleArraySample(holder, new ArrayContainer<>(newValues, len), metric_id, vendor_metric_id, instance_id, rosettaCode, frequency, timestamp);
    }

    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
                                                                Number[] newValues,
                                                                String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, int frequency,
                                                                DeviceClock.Reading timestamp) {
        return sampleArraySample(holder, new ArrayContainer<>(newValues), metric_id, vendor_metric_id, instance_id, rosettaCode, frequency, timestamp);
    }

    protected InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
                                                                Collection<Number> newValues,
                                                                String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, int frequency,
                                                                DeviceClock.Reading timestamp) {
        return sampleArraySample(holder, new CollectionContainer<>(newValues), metric_id, vendor_metric_id, instance_id, rosettaCode, frequency, timestamp);
    }

    private InstanceHolder<SampleArray> sampleArraySample(InstanceHolder<SampleArray> holder,
                                                          NullSaveContainer<Number> newValues,
                                                          String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, int frequency,
                                                          DeviceClock.Reading timestamp) {

        holder = ensureHolderConsistency(holder, metric_id, vendor_metric_id, instance_id, rosettaCode, frequency);

        if (!newValues.isNull()) {
            // Call this now so that resolution of instance registration timestamp
            // is reduced
            timestamp = timestamp.refineResolutionForFrequency(frequency, newValues.size());
            if (null == holder) {
                holder = createSampleArrayInstance(metric_id, vendor_metric_id, instance_id, rosettaCode, frequency);
            }
            sampleArraySample(holder.data, newValues, timestamp);
        } else {
            if (holder != null) {
                unregisterSampleArrayInstance(holder);
                holder = null;
            }
        }
        return holder;
    }
    protected void sampleArraySample(SampleArray sampleArray, Number[] newValues, DeviceClock.Reading timestamp) {
        sampleArraySample(sampleArray, new ArrayContainer<>(newValues), timestamp);
    }

    protected void sampleArraySample(SampleArray sampleArray, Collection<Number> newValues, DeviceClock.Reading timestamp) {
        sampleArraySample(sampleArray, new CollectionContainer<>(newValues), timestamp);
    }

    private void sampleArraySample(SampleArray data, NullSaveContainer<Number> newValues, DeviceClock.Reading deviceTimestamp) {
        fill(data, newValues);
        publish(data, deviceTimestamp);
    }

    protected InstanceHolder<Numeric> createNumericInstance(String metric_id, String vendor_metric_id) {
        return createNumericInstance(metric_id, vendor_metric_id, 0);
    }

    protected InstanceHolder<Numeric> createNumericInstance(String metric_id, String vendor_metric_id, int instance_id) {
        return createNumericInstance(metric_id, vendor_metric_id, instance_id, rosetta.MDC_DIM_DIMLESS.VALUE);
    }

    protected InstanceHolder<Numeric> createNumericInstance(String metric_id, String vendor_metric_id, int instance_id, String rosettaCode) {
        if (deviceIdentity == null || deviceIdentity.getUniqueDeviceIdentifier() == null || "".equals(deviceIdentity.getUniqueDeviceIdentifier())) {
            throw new IllegalStateException("Please populate deviceIdentity.unique_device_identifier before calling createNumericInstance");
        }

        InstanceHolder<Numeric> holder = new InstanceHolder<Numeric>();
        holder.data = new Numeric();
        holder.data.deviceIdentity = deviceIdentity;
        holder.data.metricId = metric_id;
        holder.data.vendorMetricId = vendor_metric_id;
        holder.data.instanceId = instance_id;
        holder.data.rosettaCode = rosettaCode;

        holder.handle = null;
        //holder.handler = numericDataWriter.register_instance(holder.data);

        registeredNumericInstances.add(holder);
        return holder;
    }
    protected void unregisterNumericInstance(InstanceHolder<Numeric> holder) {
        if (null != holder) {
            registeredNumericInstances.remove(holder);
            log.debug("numericDataWriter.unregister_instance(holder.data, holder.handler);");
            //numericDataWriter.unregister_instance(holder.data, holder.handler);
        }
    }

    // For convenience
    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Integer newValue, String metric_id, String vendor_metric_id, String rosettaCode, DeviceClock.Reading time) {
        return numericSample(holder, null == newValue ? null : newValue.floatValue(), metric_id, vendor_metric_id, rosettaCode, time);
    }

    // For convenience
    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Integer newValue, String metric_id, String vendor_metric_id, int instance_id, DeviceClock.Reading time) {
        return numericSample(holder, null == newValue ? null : newValue.floatValue(), metric_id, vendor_metric_id, instance_id, rosetta.MDC_DIM_DIMLESS.VALUE, time);
    }

    // For convenience
    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Integer newValue, String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, DeviceClock.Reading time) {
        return numericSample(holder, null == newValue ? null : newValue.floatValue(), metric_id, vendor_metric_id, instance_id, rosettaCode, time);
    }

    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Float newValue, String metric_id, String vendor_metric_id, String rosettaCode, DeviceClock.Reading time) {
        return numericSample(holder, newValue, metric_id, vendor_metric_id, 0, rosettaCode, time);
    }

    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Float newValue, String metric_id, String vendor_metric_id, int instance_id, DeviceClock.Reading time) {
        return numericSample(holder, newValue, metric_id, vendor_metric_id, instance_id, rosetta.MDC_DIM_DIMLESS.VALUE, time);
    }

    protected void numericSample(Numeric numeric, float newValue, DeviceClock.Reading deviceTimestamp) {
        numeric.value = newValue;
        if(deviceTimestamp.hasDeviceTime()) {
            numeric.deviceTime.sec = (int) deviceTimestamp.getTime().getEpochSecond();
            numeric.deviceTime.nanosec = deviceTimestamp.getTime().getNano();
        } else {
            numeric.deviceTime.sec = 0;
            numeric.deviceTime.nanosec = 0;
        }

        numeric.presentationTime.sec = (int) deviceTimestamp.getTime().getEpochSecond();
        numeric.presentationTime.nanosec = deviceTimestamp.getTime().getNano();

        publish(numeric, deviceTimestamp);
        //numericDataWriter.write(holder.data, holder.handler);
    }

    protected InstanceHolder<Numeric> numericSample(InstanceHolder<Numeric> holder, Float newValue, String metric_id, String vendor_metric_id, int instance_id, String rosettaCode, DeviceClock.Reading time) {

        if (holder != null && (!holder.data.metricId.equals(metric_id) || !holder.data.vendorMetricId.equals(vendor_metric_id) || holder.data.instanceId != instance_id || !holder.data.rosettaCode.equals(rosettaCode))) {
            unregisterNumericInstance(holder);
            holder = null;
        }
        if (null != newValue) {
            if (null == holder) {
                holder = createNumericInstance(metric_id, vendor_metric_id, instance_id, rosettaCode);
            }
            numericSample(holder.data, newValue, time);
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

    private void publish(Data data, DeviceClock.Reading deviceTimestamp) {
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

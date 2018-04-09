package fakedds;

import ice.DeviceConnectivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import runner.AbstractDeviceRunner;

public class DataWriter {
    private static final Logger log = LoggerFactory.getLogger(AbstractDeviceRunner.class);

    public static void write(DeviceConnectivity instance_data, InstanceHandle_t handle) {
        System.out.println("DataWriter.write(" + instance_data + ", " + handle + ")");
        log.debug("DataWriter.write(" + instance_data + ", " + handle + ")");
    }

    public static InstanceHandle_t register_instance(DeviceConnectivity deviceConnectivity) {
        System.out.println("DataWriter.register_instance(" + deviceConnectivity + ")");
        log.debug("DataWriter.register_instance(" + deviceConnectivity + ")");
        return new InstanceHandle_t();
    }
}

package utils;

import common.DeviceIdentity;
import export.writers.Writer;
import runner.AbstractDeviceRunner;

import java.io.IOException;

public class Device {
    public DeviceIdentity deviceIdentity;
    private AbstractDeviceRunner runner;

    public Device(String site, String service, String sector, String room, String alias, NetworkAddress addr, String driver) throws IOException, ClassNotFoundException {

        this.deviceIdentity = new DeviceIdentity(site, service, sector, room, alias, addr, driver);

        this.runner = AbstractDeviceRunner.resolveRunner(this.deviceIdentity);
    }

    public void run() {
        this.runner.connect();
    }

    public void addListener(Writer writer) {
        this.runner.addListener(writer);
    }
}
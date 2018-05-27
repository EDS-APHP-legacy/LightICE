import export.writers.Writer;
import org.slf4j.Logger;
import utils.Conf;
import utils.Device;

import java.io.IOException;

import static utils.Conf.parseConfig;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Conf conf = parseConfig("conf.json");

        for (Device device : conf.getDevices()) {
            System.out.println("Connecting to " + device.deviceIdentity.getAlias() + " (" + device.deviceIdentity.getAddrString() + ")...");
            device.run();
            for (Writer writer : conf.getWriters())
                device.addListener(writer);
        }

        while (true) {
            Thread.sleep(600000);
        }
    }
}

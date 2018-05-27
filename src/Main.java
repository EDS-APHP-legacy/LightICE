import export.writers.Writer;
import utils.Conf;
import utils.Device;

import java.io.IOException;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import static utils.Conf.parseConfig;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Conf conf = parseConfig("conf.json");

        // Without this code it defaults to Level.INFO
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(conf.getLogLevel());

        for (Device device : conf.getDevices()) {
            System.out.println("Connecting to " + device.deviceIdentity.getAlias() + " (" + device.deviceIdentity.getAddrString() + ")...");
            device.run();
            for (Writer writer : conf.getWriters())
                device.addListener(writer);
        }

        while (true) {
            Thread.sleep(60000);
        }
    }
}

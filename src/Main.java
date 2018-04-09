import export.writers.Writer;
import utils.Conf;
import utils.Device;

import java.io.IOException;

import static utils.Conf.parseConfig;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Conf conf = parseConfig("conf.json");

        for (Device device : conf.getDevices()) {
            device.run();
            for (Writer writer : conf.getWriters())
                device.addListener(writer);
        }

        while (true) {
            Thread.sleep(600000);
        }
    }
}

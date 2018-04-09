package utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import export.serializers.AvroSerializer;
import export.serializers.JsonSerializer;
import export.serializers.Serializer;
import export.writers.DruidWriter;
import export.writers.KafkaWriter;
import export.writers.StdoutWriter;
import export.writers.Writer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Conf {
    private List<Device> devices;
    private List<Writer> writers;

    public Conf() {
        this.devices = new ArrayList<>();
        this.writers = new ArrayList<>();
    }

    public void addDevice(Device device) {
        this.devices.add(device);
    }

    private void addWriter(Writer writer) {
        this.writers.add(writer);
    }

    public List<Device> getDevices() {
        return devices;
    }

    public List<Writer> getWriters() {
        return writers;
    }

    private static String readFileToString(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, "utf-8");
    }

    public static Conf parseConfig(String configurationPath) throws IOException, ClassNotFoundException {
        Conf conf = new Conf();

        for (JsonValue value : Json.parse(readFileToString(configurationPath)).asObject().get("writers").asArray()) {
            String type = value.asObject().get("type").asString();
            String serializerName = value.asObject().get("serializer").asString();

            Serializer serializer;

            if (Objects.equals(serializerName, "json")) {
                serializer = new JsonSerializer();
            }
            else if (Objects.equals(serializerName, "avro")) {
                serializer = new AvroSerializer();
            }
            else {
                throw new NotImplementedException();
            }


            Writer writer;
            if (Objects.equals(type, "kafka")) {
                String kafkaTopic = value.asObject().get("topic").asString();
                String jaasConfPath = value.asObject().get("jaasConfPath").asString();
                Properties props = new Properties();

                for (JsonObject.Member subValue : value.asObject().get("properties").asObject()) {
                    if (subValue.getValue().isNumber())
                        props.put(subValue.getName(), subValue.getValue().asInt());
                    else if (subValue.getValue().isString())
                        props.put(subValue.getName(), subValue.getValue().asString());
                    else
                        throw new NotImplementedException();
                }
                writer = new KafkaWriter(serializer, kafkaTopic, jaasConfPath, props);
            }
            else if (Objects.equals(type, "stdout")) {
                writer = new StdoutWriter(serializer);
            }
            else if (Objects.equals(type, "druid")) {
                writer = new DruidWriter(serializer);
            }
            else if (Objects.equals(type, "tcp")) {
                throw new NotImplementedException();
            }
            else {
                throw new NotImplementedException();
            }

            conf.addWriter(writer);
        }

        for (JsonValue value : Json.parse(readFileToString(configurationPath)).asObject().get("servers").asArray()) {
            Device device = new Device(
                    value.asObject().get("alias").asString(),
                    value.asObject().get("site").asString(),
                    value.asObject().get("service").asString(),
                    value.asObject().get("sector").asString(),
                    value.asObject().get("room").asString(),
                    value.asObject().get("serialPort").asString(),
                    value.asObject().get("driver").asString()
            );
            conf.addDevice(device);
        }

        return conf;
    }
}

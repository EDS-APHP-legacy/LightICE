package utils;

import ch.qos.logback.classic.Level;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import export.serializers.AvroSerializer;
import export.serializers.JsonSerializer;
import export.serializers.Serializer;
import export.writers.KafkaWriter;
import export.writers.StdoutWriter;
import export.writers.Writer;
import org.apache.commons.compress.archivers.dump.UnrecognizedFormatException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Conf {
    public Level getLogLevel() {
        return logLevel;
    }

    private Level logLevel;
    private List<Device> devices;
    private List<Writer> writers;

    public Conf() {
        this.devices = new ArrayList<>();
        this.writers = new ArrayList<>();
        this.logLevel = Level.INFO;
    }

    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
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

        JsonObject confObj = Json.parse(readFileToString(configurationPath)).asObject();

        switch (confObj.get("log_level").asString().toUpperCase()) {
            case "TRACE":
                conf.setLogLevel(Level.TRACE);
                break;
            case "DEBUG":
                conf.setLogLevel(Level.DEBUG);
                break;
            case "INFO":
                conf.setLogLevel(Level.INFO);
                break;
            case "WARN":
                conf.setLogLevel(Level.WARN);
                break;
            case "ERROR":
                conf.setLogLevel(Level.ERROR);
                break;
            case "OFF":
                conf.setLogLevel(Level.OFF);
                break;
            default:
                throw new UnexpectedException("log_level variable should be one of: TRACE, DEBUG, INFO, WARN, ERROR, OFF");

        }

        for (JsonValue value : confObj.get("writers").asArray()) {
            String type = value.asObject().get("type").asString();

            Serializer serializer = null;
            try {
                String serializerName = value.asObject().get("serializer").asString();
                boolean serializerFlat = value.asObject().get("serializer_flat").asBoolean();
                if (Objects.equals(serializerName, "json")) {
                    serializer = new JsonSerializer(serializerFlat);
                }
                else if (Objects.equals(serializerName, "avro")) {
                    serializer = new AvroSerializer(serializerFlat);
                }
                else {
                    throw new UnsupportedOperationException();
                }
            }
            catch (NullPointerException e) {
                System.err.println("[ERROR] Configuration incorrect, you probably forgot serializer_flat.");
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
                        throw new UnsupportedOperationException();
                }
                writer = new KafkaWriter(serializer, kafkaTopic, jaasConfPath, props);
            }
            else if (Objects.equals(type, "stdout")) {
                writer = new StdoutWriter(serializer);
            }
            else {
                throw new UnsupportedOperationException();
            }

            conf.addWriter(writer);
        }

        for (JsonValue value : confObj.get("servers").asArray()) {
            Device device = null;
            if (!(value.asObject().get("serialPort") == null)) {
                device = new Device(
                        value.asObject().get("alias").asString(),
                        value.asObject().get("site").asString(),
                        value.asObject().get("service").asString(),
                        value.asObject().get("sector").asString(),
                        value.asObject().get("room").asString(),
                        new NetworkAddress(value.asObject().get("serialPort").asString()),
                        value.asObject().get("driver").asString()
                );
            }
            else if (!(value.asObject().get("hostname") == null) && !(value.asObject().get("port") == null)) {
                device = new Device(
                    value.asObject().get("alias").asString(),
                    value.asObject().get("site").asString(),
                    value.asObject().get("service").asString(),
                    value.asObject().get("sector").asString(),
                    value.asObject().get("room").asString(),
                    new NetworkAddress(value.asObject().get("hostname").asString(), value.asObject().get("port").asInt()),
                    value.asObject().get("driver").asString()
                );
            }
            if (device == null)
                throw new UnsupportedEncodingException("Configuration incomplete, please verify it!");

            conf.addDevice(device);
        }

        return conf;
    }
}

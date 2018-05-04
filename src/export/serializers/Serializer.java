package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Serializer {
    public final Class outputType;
    protected final boolean flat;

    protected Serializer(Class outputType, boolean flat) {
        this.outputType = outputType;
        this.flat = flat;
    }

    public List<byte[]> serializeToBytes(DeviceIdentity deviceIdentity, Data data) {
        return serializeToString(deviceIdentity, data)
                .stream()
                .map(x -> x.getBytes(StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }

    public List<String> serializeToString(DeviceIdentity deviceIdentity, Data data) {
        throw new UnsupportedOperationException();
    }

    public List<Map<String, Object>> serializeToMap(DeviceIdentity deviceIdentity, Data data) {
        throw new UnsupportedOperationException();
    }
}

package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.charset.StandardCharsets;

public abstract class Serializer {
    public final Class outputType;

    protected Serializer(Class outputType) {
        this.outputType = outputType;
    }

    public byte[] serializeToBytes(DeviceIdentity deviceIdentity, Data data) {
        return serializeToString(deviceIdentity, data).getBytes(StandardCharsets.UTF_8);
    }

    public String serializeToString(DeviceIdentity deviceIdentity, Data data) {
        throw new NotImplementedException();
    }
}

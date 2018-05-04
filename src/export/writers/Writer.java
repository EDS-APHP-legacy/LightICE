package export.writers;

import common.DeviceIdentity;
import datatypes.Data;
import export.serializers.Serializer;


public abstract class Writer {
    protected final Serializer serializer;

    public Writer(Serializer serializer) {
        this.serializer = serializer;
    }

    public void write(DeviceIdentity deviceIdentity, Data data) {
        throw new UnsupportedOperationException();
    }
}

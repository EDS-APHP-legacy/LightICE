package export.writers;

import common.DeviceIdentity;
import datatypes.Data;
import export.serializers.Serializer;

public class DruidWriter extends Writer {
    public DruidWriter(Serializer serializer) {
        super(serializer);
    }

    @Override
    public void write(DeviceIdentity deviceIdentity, Data data) {
        this.serializer.serializeToBytes(deviceIdentity, data);
    }
}

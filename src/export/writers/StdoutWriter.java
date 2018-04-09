package export.writers;

import common.DeviceIdentity;
import datatypes.Data;
import export.serializers.Serializer;

public class StdoutWriter extends Writer {
    public StdoutWriter(Serializer serializer) {
        super(serializer);
    }

    @Override
    public void write(DeviceIdentity deviceIdentity, Data data) {
        System.out.println(this.serializer.serializeToString(deviceIdentity, data));
    }
}

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
        for (String s : this.serializer.serializeToString(deviceIdentity, data))
            System.out.println(s);
    }
}

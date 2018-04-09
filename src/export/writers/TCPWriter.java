package export.writers;

import common.DeviceIdentity;
import datatypes.Data;
import export.serializers.Serializer;

public class TCPWriter extends Writer {
    public TCPWriter(Serializer serializer) {
        super(serializer);
    }

    @Override
    public void write(DeviceIdentity deviceIdentity, Data data) {
        // TODO en s'inspirant de KafkaWriter
    }
}
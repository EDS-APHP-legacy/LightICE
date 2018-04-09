package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;
import datatypes.Numeric;
import datatypes.SampleArray;
import export.serializers.avro.*;
import org.apache.kafka.common.errors.SerializationException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AvroSerializer extends Serializer {
    public AvroSerializer() {
        super(ByteBuffer.class);
    }

    @Override
    public byte[] serializeToBytes(DeviceIdentity di, Data data) {

        DeviceInfo deviceInfo = new DeviceInfo(di.getSite(), di.getService(), di.getSector(), di.getRoom(), di.getAlias(), di.getSerialPort(), di.getDriver());

        if (data instanceof SampleArray) {
            SampleArray sa = (SampleArray) data;

            long[] tmpTimestamps = sa.getTimestampsDeviceTime(false);

            List<Float> values = new ArrayList<>(sa.getValues().length);
            List<Long> timestamps = new ArrayList<>(sa.getValues().length);
            for (int i = 0; i < sa.getValues().length; ++i) {
                values.add(i, sa.getValues()[i]);
                timestamps.add(i, tmpTimestamps[i]);
            }

            DataArray dataArray = new DataArray((float) sa.frequency, sa.rosettaCode, sa.metricId, sa.vendorMetricId, sa.instanceId, values, timestamps);

            ArrayValues arrayValues = new ArrayValues(sa.dataType, deviceInfo, dataArray);

            try {
                return arrayValues.toByteBuffer().array();
            } catch (IOException e) {
                e.printStackTrace();
                throw new SerializationException();
            }

        } else if(data instanceof Numeric) {
            Numeric nu = (Numeric) data;

            DataSingle dataSingle = new DataSingle(nu.rosettaCode, nu.metricId, nu.vendorMetricId, nu.instanceId, nu.value, nu.getTimestampDeviceTime());

            SingleValue singleValue = new SingleValue(nu.dataType, deviceInfo, dataSingle);

            try {
                return singleValue.toByteBuffer().array();
            } catch (IOException e) {
                e.printStackTrace();
                throw new SerializationException();
            }
        }
        throw new NotImplementedException();
    }
}

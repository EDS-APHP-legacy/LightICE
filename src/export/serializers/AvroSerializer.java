package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;
import datatypes.Numeric;
import datatypes.SampleArray;
import export.serializers.avro.*;
import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AvroSerializer extends Serializer {
    public AvroSerializer(boolean flat) {
        super(ByteBuffer.class, flat);
    }

    @Override
    public List<byte[]> serializeToBytes(DeviceIdentity di, Data data) {
        DeviceInfo deviceInfo = new DeviceInfo(di.getSite(), di.getService(), di.getSector(), di.getRoom(), di.getAlias(), di.getAddr().getSerialAddr(), di.getDriver());

        List<byte[]> result = new ArrayList<>();


        if (data instanceof SampleArray) {
            SampleArray sa = (SampleArray) data;

            long[] tmpTimestamps = sa.getTimestampsDeviceTime(false);

            if (this.flat) {
                DataSingle dataSingle;
                SingleValue singleValue;
                for (int i = 0; i < sa.getValues().length; ++i) {
                    dataSingle = new DataSingle(sa.getRosettaUnit(), sa.getRosettaMetric(), sa.vendorMetric, sa.instanceId, sa.getValues()[i], tmpTimestamps[i]);
                    singleValue = new SingleValue(sa.dataType, deviceInfo, dataSingle);

                    try {
                        result.add(singleValue.toByteBuffer().array());
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new SerializationException();
                    }
                }
            }
            else {

                List<Float> values = new ArrayList<>(sa.getValues().length);
                List<Long> timestamps = new ArrayList<>(sa.getValues().length);
                for (int i = 0; i < sa.getValues().length; ++i) {
                    values.add(i, sa.getValues()[i]);
                    timestamps.add(i, tmpTimestamps[i]);
                }

                DataArray dataArray = new DataArray((float) sa.frequency, sa.getRosettaUnit(), sa.getRosettaMetric(), sa.vendorMetric, sa.instanceId, values, timestamps);
                ArrayValues arrayValues = new ArrayValues(sa.dataType, deviceInfo, dataArray);

                try {
                    result.add(arrayValues.toByteBuffer().array());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new SerializationException();
                }
                return result;
            }

        } else if(data instanceof Numeric) {
            Numeric nu = (Numeric) data;

            DataSingle dataSingle = new DataSingle(nu.getRosettaUnit(), nu.getRosettaMetric(), nu.getVendorMetric(), nu.instanceId, nu.value, nu.getTimestampDeviceTime());
            SingleValue singleValue = new SingleValue(nu.dataType, deviceInfo, dataSingle);

            try {
                result.add(singleValue.toByteBuffer().array());
            } catch (IOException e) {
                e.printStackTrace();
                throw new SerializationException();
            }
            return result;
        }
        throw new UnsupportedOperationException();
    }
}

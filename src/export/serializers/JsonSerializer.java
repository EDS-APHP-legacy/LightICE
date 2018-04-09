package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;
import datatypes.Numeric;
import datatypes.SampleArray;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JsonSerializer extends Serializer {
    public JsonSerializer() {
        super(String.class);
    }

    @Override
    public String serializeToString(DeviceIdentity deviceIdentity, Data data) {
        if (data instanceof SampleArray) {
            SampleArray sa = (SampleArray) data;


            StringBuffer valuesStrBuffer = new StringBuffer();
            StringBuffer timestampsStrBuffer = new StringBuffer();

            float[] values = sa.getValues();
            long[] timestampsNano = sa.getTimestampsDeviceTime(false);

            for(int i = 0; i < values.length; ++i) {
                if (i != 0) {
                    valuesStrBuffer.append(",");
                    timestampsStrBuffer.append(",");
                }
                valuesStrBuffer.append(values[i]);
                timestampsStrBuffer.append(timestampsNano[i]);
            }

//            System.out.println(timestampsStrBuffer.toString());


            return "{" +
                      "\"device_info\": {" +
                        "\"site\": \"" + sa.deviceIdentity.getSite() + "\"," +
                        "\"service\": \"" + sa.deviceIdentity.getService() + "\"," +
                        "\"sector\": \"" + sa.deviceIdentity.getSector() + "\"," +
                        "\"room\": \"" + sa.deviceIdentity.getRoom() + "\"," +
                        "\"alias\": \"" + sa.deviceIdentity.getAlias() + "\"," +
                        "\"serialPort\": \"" + sa.deviceIdentity.getSerialPort() + "\"," +
                        "\"driver\": \"" + sa.deviceIdentity.getDriver() + "\"," +
                        "\"manufacturer\": \"" + sa.deviceIdentity.getManufacturer() + "\"," +
                        "\"model\": \"" + sa.deviceIdentity.getModel() + "\"," +
                        "\"serialNumber\": \"" + sa.deviceIdentity.getSerialNumber() + "\"," +
                        "\"operatingSystem\": \"" + sa.deviceIdentity.getOperatingSystem() + "\"" +
                      "}," +
                      "\"data_type\": \"sample\"," +
                      "\"data\": {" +
                        "\"presentationTime\": " + sa.presentationTime.timestampMilli() + "," +
                        "\"deviceTime\": " + sa.deviceTime.timestampMilli() + "," +
                        "\"frequency\": \"" + sa.frequency + "\", " +
                        "\"rosetta_code\": \"" + sa.rosettaCode + "\", " +
                        "\"metric_id\": \"" + sa.metricId + "\", " +
                        "\"vendor_metric_id\": \"" + sa.vendorMetricId + "\", " +
                        "\"instance_id\": \"" + sa.instanceId + "\", " +
                        "\"values\": [" + valuesStrBuffer.toString() + "]" + "\", " +
                        "\"timestampsNano\": [" + timestampsStrBuffer.toString() + "]" + "\", " +
                      "}" +
                    "}";
        } else if(data instanceof Numeric) {
            Numeric nu = (Numeric) data;
            return "{" +
                      "\"device_info\": {" +
                        "\"site\": \"" + nu.deviceIdentity.getSite() + "\"," +
                        "\"service\": \"" + nu.deviceIdentity.getService() + "\"," +
                        "\"sector\": \"" + nu.deviceIdentity.getSector() + "\"," +
                        "\"room\": \"" + nu.deviceIdentity.getRoom() + "\"," +
                        "\"alias\": \"" + nu.deviceIdentity.getAlias() + "\"," +
                        "\"serialPort\": \"" + nu.deviceIdentity.getSerialPort() + "\"," +
                        "\"driver\": \"" + nu.deviceIdentity.getDriver() + "\"," +
                        "\"manufacturer\": \"" + nu.deviceIdentity.getManufacturer() + "\"," +
                        "\"model\": \"" + nu.deviceIdentity.getModel() + "\"," +
                        "\"serialNumber\": \"" + nu.deviceIdentity.getSerialNumber() + "\"," +
                        "\"operatingSystem\": \"" + nu.deviceIdentity.getOperatingSystem() + "\"" +
                      "}," +
                      "\"data_type\": \"numeric\"," +
                      "\"data\": {" +
                        "\"presentationTime\": " + nu.presentationTime.timestampMilli() + "," +
                        "\"deviceTime\": " + nu.deviceTime.timestampMilli() + "," +
                        "\"rosetta_code\": \"" + nu.rosettaCode + "\"," +
                        "\"metric_id\": \"" + nu.metricId + "\"," +
                        "\"vendor_metric_id\": \"" + nu.vendorMetricId + "\"," +
                        "\"instance_id\": \"" + nu.instanceId + "\"," +
                        "\"value\": " + nu.value +
                      "}" +
                    "}";
        }
        throw new NotImplementedException();
    }
}

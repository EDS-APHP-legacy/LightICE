package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;
import datatypes.Numeric;
import datatypes.SampleArray;

import java.util.ArrayList;
import java.util.List;

public class JsonSerializer extends Serializer {
    public JsonSerializer(boolean flat) {
        super(String.class, flat);
    }

    @Override
    public List<String> serializeToString(DeviceIdentity deviceIdentity, Data data) {
        List<String> result = new ArrayList<>();

        if (data instanceof SampleArray) {
            SampleArray sa = (SampleArray) data;

            float[] values = sa.getValues();
            long[] timestampsNano = sa.getTimestampsDeviceTime(false);


            if (this.flat) {
                for(int i = 0; i < values.length; ++i) {
                    result.add("{" +
                            "\"deviceinfo_site\": \"" + sa.deviceIdentity.getSite() + "\"," +
                            "\"deviceinfo_service\": \"" + sa.deviceIdentity.getService() + "\"," +
                            "\"deviceinfo_sector\": \"" + sa.deviceIdentity.getSector() + "\"," +
                            "\"deviceinfo_room\": \"" + sa.deviceIdentity.getRoom() + "\"," +
                            "\"deviceinfo_alias\": \"" + sa.deviceIdentity.getAlias() + "\"," +
                            "\"deviceinfo_serial_port\": \"" + sa.deviceIdentity.getSerialPort() + "\"," +
                            "\"deviceinfo_driver\": \"" + sa.deviceIdentity.getDriver() + "\"," +
                            "\"deviceinfo_manufacturer\": \"" + sa.deviceIdentity.getManufacturer() + "\"," +
                            "\"deviceinfo_model\": \"" + sa.deviceIdentity.getModel() + "\"," +
                            "\"deviceinfo_serial_number\": \"" + sa.deviceIdentity.getSerialNumber() + "\"," +
                            "\"deviceinfo_operating_system\": \"" + sa.deviceIdentity.getOperatingSystem() + "\"," +
                            "\"data_type\": \"sample\"," +
                            "\"data_presentation_time\": " + sa.presentationTime.timestampMilli() + "," +
                            "\"data_device_time\": " + sa.deviceTime.timestampMilli() + "," +
                            "\"data_frequency\": \"" + sa.frequency + "\", " +
                            "\"data_rosetta_code\": \"" + sa.rosettaCode + "\", " +
                            "\"data_metric_id\": \"" + sa.metricId + "\", " +
                            "\"data_vendor_metric_id\": \"" + sa.vendorMetricId + "\", " +
                            "\"data_instance_id\": \"" + sa.instanceId + "\", " +
                            "\"data_value\": [" + values[i] + "]" + "\", " +
                            "\"data_timestamp_nano\": [" + timestampsNano[i] + "]" +
                            "}");
                }
            }
            else {
                StringBuilder valuesStrBuffer = new StringBuilder();
                StringBuilder timestampsStrBuffer = new StringBuilder();


                for(int i = 0; i < values.length; ++i) {
                    if (i != 0) {
                        valuesStrBuffer.append(",");
                        timestampsStrBuffer.append(",");
                    }
                    valuesStrBuffer.append(values[i]);
                    timestampsStrBuffer.append(timestampsNano[i]);
                }

                result.add("{" +
                        "\"deviceinfo_site\": \"" + sa.deviceIdentity.getSite() + "\"," +
                        "\"deviceinfo_service\": \"" + sa.deviceIdentity.getService() + "\"," +
                        "\"deviceinfo_sector\": \"" + sa.deviceIdentity.getSector() + "\"," +
                        "\"deviceinfo_room\": \"" + sa.deviceIdentity.getRoom() + "\"," +
                        "\"deviceinfo_alias\": \"" + sa.deviceIdentity.getAlias() + "\"," +
                        "\"deviceinfo_serial_port\": \"" + sa.deviceIdentity.getSerialPort() + "\"," +
                        "\"deviceinfo_driver\": \"" + sa.deviceIdentity.getDriver() + "\"," +
                        "\"deviceinfo_manufacturer\": \"" + sa.deviceIdentity.getManufacturer() + "\"," +
                        "\"deviceinfo_model\": \"" + sa.deviceIdentity.getModel() + "\"," +
                        "\"deviceinfo_serial_number\": \"" + sa.deviceIdentity.getSerialNumber() + "\"," +
                        "\"deviceinfo_operating_system\": \"" + sa.deviceIdentity.getOperatingSystem() + "\"," +
                        "\"data_type\": \"sample\"," +
                        "\"data_presentation_time\": " + sa.presentationTime.timestampMilli() + "," +
                        "\"data_device_time\": " + sa.deviceTime.timestampMilli() + "," +
                        "\"data_frequency\": \"" + sa.frequency + "\", " +
                        "\"data_rosetta_code\": \"" + sa.rosettaCode + "\", " +
                        "\"data_metric_id\": \"" + sa.metricId + "\", " +
                        "\"data_vendor_metric_id\": \"" + sa.vendorMetricId + "\", " +
                        "\"data_instance_id\": \"" + sa.instanceId + "\", " +
                        "\"data_values\": [" + valuesStrBuffer.toString() + "]" + "\", " +
                        "\"data_timestamps_nano\": [" + timestampsStrBuffer.toString() + "]" +
                        "}");
            }
            return result;
        } else if(data instanceof Numeric) {
            Numeric nu = (Numeric) data;
            result.add("{" +
                      "\"deviceinfo_site\": \"" + nu.deviceIdentity.getSite() + "\"," +
                      "\"deviceinfo_service\": \"" + nu.deviceIdentity.getService() + "\"," +
                      "\"deviceinfo_sector\": \"" + nu.deviceIdentity.getSector() + "\"," +
                      "\"deviceinfo_room\": \"" + nu.deviceIdentity.getRoom() + "\"," +
                      "\"deviceinfo_alias\": \"" + nu.deviceIdentity.getAlias() + "\"," +
                      "\"deviceinfo_serial_port\": \"" + nu.deviceIdentity.getSerialPort() + "\"," +
                      "\"deviceinfo_driver\": \"" + nu.deviceIdentity.getDriver() + "\"," +
                      "\"deviceinfo_manufacturer\": \"" + nu.deviceIdentity.getManufacturer() + "\"," +
                      "\"deviceinfo_model\": \"" + nu.deviceIdentity.getModel() + "\"," +
                      "\"deviceinfo_serial_number\": \"" + nu.deviceIdentity.getSerialNumber() + "\"," +
                      "\"deviceinfo_operating_system\": \"" + nu.deviceIdentity.getOperatingSystem() + "\"," +
                      "\"data_type\": \"numeric\"," +
                      "\"data_presentation_time\": " + nu.presentationTime.timestampMilli() + "," +
                      "\"data_device_time\": " + nu.deviceTime.timestampMilli() + "," +
                      "\"data_rosetta_code\": \"" + nu.rosettaCode + "\"," +
                      "\"data_metric_id\": \"" + nu.metricId + "\"," +
                      "\"data_vendor_metric_id\": \"" + nu.vendorMetricId + "\"," +
                      "\"data_instance_id\": \"" + nu.instanceId + "\"," +
                      "\"data_value\": " + nu.value +
                    "}");
            return result;
        }
        throw new UnsupportedOperationException();
    }
}

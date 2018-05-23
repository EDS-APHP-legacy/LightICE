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
                            "\"deviceinfo_addr\": {\"" + sa.deviceIdentity.getAddrString() + "\"," +
                            "\"deviceinfo_driver\": \"" + sa.deviceIdentity.getDriver() + "\"," +
                            "\"deviceinfo_manufacturer\": \"" + sa.deviceIdentity.getManufacturer() + "\"," +
                            "\"deviceinfo_model\": \"" + sa.deviceIdentity.getModel() + "\"," +
                            "\"deviceinfo_serial_number\": \"" + sa.deviceIdentity.getSerialNumber() + "\"," +
                            "\"deviceinfo_operating_system\": \"" + sa.deviceIdentity.getOperatingSystem() + "\"," +
                            "\"data_type\": \"sample\"," +
                            "\"data_presentation_time\": " + sa.getPresentationTime().timestampMilli() + "," +
                            "\"data_device_time\": " + sa.getDeviceTime().timestampMilli() + "," +
                            "\"data_frequency\": \"" + sa.frequency + "\", " +
                            "\"data_rosetta_metric\": \"" + sa.getRosettaMetric() + "\", " +
                            "\"data_rosetta_unit\": \"" + sa.getRosettaUnit() + "\", " +
                            "\"data_vendor_metric\": \"" + sa.vendorMetric + "\", " +
                            "\"data_instance\": \"" + sa.instanceId + "\", " +
                            "\"data_value\": " + values[i] +
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
                        "\"deviceinfo_serial_port\": \"" + sa.deviceIdentity.getAddrString() + "\"," +
                        "\"deviceinfo_driver\": \"" + sa.deviceIdentity.getDriver() + "\"," +
                        "\"deviceinfo_manufacturer\": \"" + sa.deviceIdentity.getManufacturer() + "\"," +
                        "\"deviceinfo_model\": \"" + sa.deviceIdentity.getModel() + "\"," +
                        "\"deviceinfo_serial_number\": \"" + sa.deviceIdentity.getSerialNumber() + "\"," +
                        "\"deviceinfo_operating_system\": \"" + sa.deviceIdentity.getOperatingSystem() + "\"," +
                        "\"data_type\": \"sample_array\"," +
                        "\"data_presentation_time\": " + sa.getPresentationTime().timestampMilli() + "," +
                        "\"data_device_time\": " + sa.getDeviceTime().timestampMilli() + "," +
                        "\"data_frequency\": \"" + sa.frequency + "\", " +
                        "\"data_rosetta_metric\": \"" + sa.getRosettaMetric() + "\", " +
                        "\"data_rosetta_unit\": \"" + sa.getRosettaUnit() + "\", " +
                        "\"data_vendor_metric\": \"" + sa.vendorMetric + "\", " +
                        "\"data_instance\": \"" + sa.instanceId + "\", " +
                        "\"data_values\": [" + valuesStrBuffer.toString() + "]" +
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
                      "\"deviceinfo_serial_port\": \"" + nu.deviceIdentity.getAddrString() + "\"," +
                      "\"deviceinfo_driver\": \"" + nu.deviceIdentity.getDriver() + "\"," +
                      "\"deviceinfo_manufacturer\": \"" + nu.deviceIdentity.getManufacturer() + "\"," +
                      "\"deviceinfo_model\": \"" + nu.deviceIdentity.getModel() + "\"," +
                      "\"deviceinfo_serial_number\": \"" + nu.deviceIdentity.getSerialNumber() + "\"," +
                      "\"deviceinfo_operating_system\": \"" + nu.deviceIdentity.getOperatingSystem() + "\"," +
                      "\"data_type\": \"numeric\"," +
                      "\"data_presentation_time\": " + nu.getPresentationTime().timestampMilli() + "," +
                      "\"data_device_time\": " + nu.getDeviceTime().timestampMilli() + "," +
                      "\"data_rosetta_metric\": \"" + nu.getRosettaMetric() + "\", " +
                      "\"data_rosetta_unit\": \"" + nu.getRosettaUnit() + "\", " +
                      "\"data_vendor_metric\": \"" + nu.getVendorMetric() + "\", " +
                      "\"data_instance\": \"" + nu.instanceId + "\", " +
                      "\"data_value\": " + nu.value +
                    "}");
            return result;
        }
        throw new UnsupportedOperationException();
    }
}

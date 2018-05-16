package export.serializers;

import common.DeviceIdentity;
import datatypes.Data;
import datatypes.Numeric;
import datatypes.SampleArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSerializer extends Serializer {
    public MapSerializer() {
        super(null, true);
    }

    @Override
    public List<Map<String, Object>> serializeToMap(DeviceIdentity deviceIdentity, Data data) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (data instanceof SampleArray) {
            SampleArray sa = (SampleArray) data;

            float[] values = sa.getValues();
            for (float value : values) {
                final Map<String, Object> obj = new HashMap<>();
                obj.put("deviceinfo_site", sa.deviceIdentity.getSite());
                obj.put("deviceinfo_sector", sa.deviceIdentity.getSector());
                obj.put("deviceinfo_room", sa.deviceIdentity.getRoom());
                obj.put("deviceinfo_alias", sa.deviceIdentity.getAlias());
                obj.put("deviceinfo_serial_port", sa.deviceIdentity.getSerialPort());
                obj.put("deviceinfo_driver", sa.deviceIdentity.getDriver());
                obj.put("deviceinfo_manufacturer", sa.deviceIdentity.getManufacturer());
                obj.put("deviceinfo_model", sa.deviceIdentity.getModel());
                obj.put("deviceinfo_serial_number", sa.deviceIdentity.getSerialNumber());
                obj.put("deviceinfo_operating_system", sa.deviceIdentity.getOperatingSystem());
                obj.put("data_type", "sample");
                obj.put("data_presentation_time", sa.presentationTime.timestampMilli());
                obj.put("data_device_time", sa.deviceTime.timestampMilli());
                obj.put("data_rosetta_unit", sa.getRosettaUnit());
                obj.put("data_rosetta_metric", sa.getRosettaMetric());
                obj.put("data_vendor_metric", sa.vendorMetric);
                obj.put("data_instance_id", sa.instanceId);
                obj.put("data_value", value);
                result.add(obj);
            }

            return result;
        } else if(data instanceof Numeric) {
            Numeric nu = (Numeric) data;

            final Map<String, Object> obj = new HashMap<>();
            obj.put("deviceinfo_site", nu.deviceIdentity.getSite());
            obj.put("deviceinfo_sector", nu.deviceIdentity.getSector());
            obj.put("deviceinfo_room", nu.deviceIdentity.getRoom());
            obj.put("deviceinfo_alias", nu.deviceIdentity.getAlias());
            obj.put("deviceinfo_serial_port", nu.deviceIdentity.getSerialPort());
            obj.put("deviceinfo_driver", nu.deviceIdentity.getDriver());
            obj.put("deviceinfo_manufacturer", nu.deviceIdentity.getManufacturer());
            obj.put("deviceinfo_model", nu.deviceIdentity.getModel());
            obj.put("deviceinfo_serial_number", nu.deviceIdentity.getSerialNumber());
            obj.put("deviceinfo_operating_system", nu.deviceIdentity.getOperatingSystem());
            obj.put("data_type", "sample");
            obj.put("data_presentation_time", nu.presentationTime.timestampMilli());
            obj.put("data_device_time", nu.deviceTime.timestampMilli());
            obj.put("data_rosetta_unit", nu.getRosettaUnit());
            obj.put("data_rosetta_metric", nu.getRosettaMetric());
            obj.put("data_vendor_metric", nu.vendorMetric);
            obj.put("data_instance_id", nu.instanceId);
            obj.put("data_value", nu.value);
            result.add(obj);

            return result;
        }
        throw new UnsupportedOperationException();
    }
}

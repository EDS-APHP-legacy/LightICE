package datatypes;

import common.DeviceIdentity;
import fakedds.Copyable;
import fakedds.Values;
import ice.CdrHelper;
import ice.Time_t;


public class SampleArray extends RosettaTimeAwareData
{
    public final String dataType = "waveform";

    public DeviceIdentity deviceIdentity;

    public String vendorMetric = "";
    public int instanceId = 0;
    public int frequency = 0;
    public Values values = (Values) Values.create();


    public SampleArray() {

    }

    public void setVendorMetric(String vendorMetric) {
        if (vendorMetric == null) {
            this.vendorMetric = "";
        }
        else if (vendorMetric.length() > 64) {
            System.err.println("Rosetta metric string too long (>64) - Taking only the first 64 characters, but you should fix this!");
            this.vendorMetric = vendorMetric.substring(0, 64);
        }
        else {
            this.vendorMetric = vendorMetric;
        }
    }

    public String getVendorMetric() {
        return this.vendorMetric;
    }
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }



        if(getClass() != o.getClass()) {
            return false;
        }

        SampleArray otherObj = (SampleArray)o;



        if(!deviceIdentity.getUniqueDeviceIdentifier().equals(otherObj.deviceIdentity.getUniqueDeviceIdentifier())) {
            return false;
        }

        if(!rosettaMetric.equals(otherObj.rosettaMetric)) {
            return false;
        }

        if(!vendorMetric.equals(otherObj.vendorMetric)) {
            return false;
        }

        if(instanceId != otherObj.instanceId) {
            return false;
        }

        if(!rosettaUnit.equals(otherObj.rosettaUnit)) {
            return false;
        }

        if(frequency != otherObj.frequency) {
            return false;
        }

        if(!values.equals(otherObj.values)) {
            return false;
        }

        if(!deviceTime.equals(otherObj.deviceTime)) {
            return false;
        }

        if(!presentationTime.equals(otherObj.presentationTime)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;

        __result += deviceIdentity.getUniqueDeviceIdentifier().hashCode();
        __result += rosettaMetric.hashCode();
        __result += vendorMetric.hashCode();
        __result += (int) instanceId;
        __result += rosettaUnit.hashCode();
        __result += (int)frequency;
        __result += values.hashCode();
        __result += deviceTime.hashCode();
        __result += presentationTime.hashCode();

        return __result;
    }


    /**
     * This is the implementation of the <code>Copyable</code> interface.
     * This method will perform a deep copy of <code>src</code>
     * This method could be placed into <code>SampleArrayTypeSupport</code>
     * rather than here by using the <code>-noCopyable</code> option
     * to rtiddsgen.
     *
     * @param src The Object which contains the data to be copied.
     * @return Returns <code>this</code>.
     * @exception NullPointerException If <code>src</code> is null.
     * @exception ClassCastException If <code>src</code> is not the
     * same type as <code>this</code>.
     * @see Copyable#copy_from(java.lang.Object)
     */
    public Object copy_from(Object src) {


        SampleArray typedSrc = (SampleArray) src;
        SampleArray typedDst = this;

        typedDst.deviceIdentity = DeviceIdentity.fromOther(typedSrc.deviceIdentity);
        typedDst.rosettaMetric = typedSrc.rosettaMetric;
        typedDst.vendorMetric = typedSrc.vendorMetric;
        typedDst.instanceId = typedSrc.instanceId;
        typedDst.rosettaUnit = typedSrc.rosettaUnit;
        typedDst.frequency = typedSrc.frequency;
        typedDst.values = (Values) typedDst.values.copy_from(typedSrc.values);
        typedDst.deviceTime = (Time_t) typedDst.deviceTime.copy_from(typedSrc.deviceTime);
        typedDst.presentationTime = (Time_t) typedDst.presentationTime.copy_from(typedSrc.presentationTime);

        return this;
    }

    public float[] getValues() {
        return this.values.getValuesAsArray();
    }

    public long[] getTimestamps(boolean fromStart, Time_t time) {
        int len = this.getValues().length;

        long gap = 1000000000L / this.frequency;

        long startNano;
        long endNano;
        if (fromStart) {
            startNano = time.timestampNano();
            endNano = startNano + ((len-1) * gap);
        }
        else {
            endNano = time.timestampNano();
            startNano = endNano - ((len-1) * gap);
        }

        long[] result = new long[len];
        for (int i = 0; i < len; ++i) {
            result[i] = startNano + i * gap;
        }
        return result;
    }

    public long[] getTimestampsDeviceTime(boolean fromStart) {
        return this.getTimestamps(fromStart, this.deviceTime);
    }

    public long[] getTimestampsPresentationTime(boolean fromStart) {
        return this.getTimestamps(fromStart, this.presentationTime);
    }


    public String toString(){
        return toString("", 0);
    }


    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();


        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }


        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("unique_device_identifier: ").append(deviceIdentity.getUniqueDeviceIdentifier()).append("\n");

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("rosettaMetric: ").append(rosettaMetric).append("\n");

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("vendorMetric: ").append(vendorMetric).append("\n");

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("instanceId: ").append(instanceId).append("\n");

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("rosettaUnit: ").append(rosettaUnit).append("\n");

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("frequency: ").append(frequency).append("\n");

        strBuffer.append(values.toString("values ", indent+1));

        strBuffer.append(deviceTime.toString("deviceTime ", indent+1));

        strBuffer.append(presentationTime.toString("presentationTime ", indent+1));

        return strBuffer.toString();
    }

}


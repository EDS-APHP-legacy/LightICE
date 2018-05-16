package datatypes;

import common.DeviceIdentity;
import ice.CdrHelper;


public class Numeric extends Data
{
    public final String dataType = "waveform";

    public DeviceIdentity deviceIdentity;

    public ice.Time_t deviceTime = (ice.Time_t) ice.Time_t.create();
    public ice.Time_t presentationTime = (ice.Time_t) ice.Time_t.create();

    public String vendorMetric = ""; /* maximum length = (64) */
    public String rosettaMetric = ""; /* maximum length = (64) */
    public String rosettaUnit = ""; /* maximum length = (64) */
    public int instanceId = 0;
    public float value = 0;


    public Numeric() {

    }


    public Numeric(Numeric other) {

        this();
        copy_from(other);
    }


    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }



        if(getClass() != o.getClass()) {
            return false;
        }

        Numeric otherObj = (Numeric)o;



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

        if(value != otherObj.value) {
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


    public long getTimestampDeviceTime() {
        return this.deviceTime.timestampNano();
    }

    public long getTimestampPresentationTime() {
        return this.presentationTime.timestampNano();
    }


    public int hashCode() {
        int __result = 0;

        __result += deviceIdentity.getUniqueDeviceIdentifier().hashCode();

        __result += rosettaMetric.hashCode();

        __result += vendorMetric.hashCode();

        __result += (int) instanceId;

        __result += rosettaUnit.hashCode();

        __result += (int)value;

        __result += deviceTime.hashCode();

        __result += presentationTime.hashCode();

        return __result;
    }


    /**
     * This is the implementation of the <code>Copyable</code> interface.
     * This method will perform a deep copy of <code>src</code>
     * This method could be placed into <code>NumericTypeSupport</code>
     * rather than here by using the <code>-noCopyable</code> option
     * to rtiddsgen.
     *
     * @param src The Object which contains the data to be copied.
     * @return Returns <code>this</code>.
     * @exception NullPointerException If <code>src</code> is null.
     * @exception ClassCastException If <code>src</code> is not the
     * same type as <code>this</code>.
     * @see fakedds.Copyable#copy_from(java.lang.Object)
     */
    public Object copy_from(Object src) {


        Numeric typedSrc = (Numeric) src;
        Numeric typedDst = this;

        typedDst.deviceIdentity.setUniqueDeviceIdentifier(typedSrc.deviceIdentity.getUniqueDeviceIdentifier());

        typedDst.rosettaMetric = typedSrc.rosettaMetric;

        typedDst.vendorMetric = typedSrc.vendorMetric;

        typedDst.instanceId = typedSrc.instanceId;

        typedDst.rosettaUnit = typedSrc.rosettaUnit;

        typedDst.value = typedSrc.value;

        typedDst.deviceTime = (ice.Time_t) typedDst.deviceTime.copy_from(typedSrc.deviceTime);

        typedDst.presentationTime = (ice.Time_t) typedDst.presentationTime.copy_from(typedSrc.presentationTime);

        return this;
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
        strBuffer.append("value: ").append(value).append("\n");

        strBuffer.append(deviceTime.toString("deviceTime ", indent+1));

        strBuffer.append(presentationTime.toString("presentationTime ", indent+1));

        return strBuffer.toString();
    }

}


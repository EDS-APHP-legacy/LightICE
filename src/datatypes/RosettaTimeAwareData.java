package datatypes;

import common.time.IceInstant;
import common.time.IceInstantInterface;
import ice.Time_t;

abstract class RosettaTimeAwareData extends Data {
    protected Time_t deviceTime = (Time_t) Time_t.create();
    protected Time_t presentationTime = (Time_t) Time_t.create();

    protected String rosettaMetric = ""; /* maximum length = (64) */
    protected String rosettaUnit = ""; /* maximum length = (64) */

    public void setRosettaMetric(String rosettaMetric) {
        if (rosettaMetric == null) {
            this.rosettaMetric = "";
        }
        else if (rosettaMetric.length() > 64) {
            System.err.println("Rosetta metric string too long (>64) - Taking only the first 64 characters, but you should fix this!");
            this.rosettaMetric = rosettaMetric.substring(0, 64);
        }
        else {
            this.rosettaMetric = rosettaMetric;
        }
    }

    public void setRosettaUnit(String rosettaUnit) {
        if (rosettaUnit == null) {
            this.rosettaUnit = "";
        }
        else if (rosettaUnit.length() > 64) {
            System.err.println("Rosetta unit string too long (>64) - Taking only the first 64 characters, but you should fix this!");
            this.rosettaUnit = rosettaUnit.substring(0, 64);
        }
        else {
            this.rosettaUnit = rosettaUnit;
        }
    }

    public String getRosettaMetric() {
        return this.rosettaMetric;
    }


    public String getRosettaUnit() {
        if (this.rosettaUnit == null)
            return "";
        return this.rosettaUnit;
    }

    public Time_t getDeviceTime() {
        return this.deviceTime;
    }

    public Time_t getPresentationTime() {
        return this.presentationTime;
    }

    public void setTime(IceInstant deviceTime, IceInstant referenceTime) {
        if (deviceTime == null) {
            this.deviceTime.sec = 0;
            this.deviceTime.nanosec = 0;
        }
        else {
            this.deviceTime.sec = (int) deviceTime.getTime().getEpochSecond();
            this.deviceTime.nanosec = deviceTime.getTime().getNano();
        }

        this.presentationTime.sec = (int) referenceTime.getTime().getEpochSecond();
        this.presentationTime.nanosec = referenceTime.getTime().getNano();

    }
}

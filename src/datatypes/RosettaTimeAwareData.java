package datatypes;

import ice.Time_t;

abstract class RosettaTimeAwareData extends Data {
    public Time_t deviceTime = (Time_t) Time_t.create();
    public Time_t presentationTime = (Time_t) Time_t.create();

    protected String rosettaMetric = ""; /* maximum length = (64) */
    protected String rosettaUnit = ""; /* maximum length = (64) */

    public void setRosettaMetric(String rosettaMetric) {
        if (rosettaMetric.length() > 64) {
            System.err.println("Rosetta metric string too long (>64) - Taking only the first 64 characters, but you should fix this!");
            this.rosettaMetric = rosettaMetric.substring(0, 64);
        }
        else {
            this.rosettaMetric = rosettaMetric;
        }
    }

    public void setRosettaUnit(String rosettaUnit) {
        if (rosettaUnit.length() > 64) {
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
        return this.rosettaUnit;
    }
}

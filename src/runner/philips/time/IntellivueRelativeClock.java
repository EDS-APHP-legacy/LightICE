package runner.philips.time;

import common.time.IceInstant;
import common.time.TimeUnit;
import drivers.philips.intellivue.attribute.Attribute;
import drivers.philips.intellivue.data.AbsoluteTime;
import drivers.philips.intellivue.data.AttributeId;
import drivers.philips.intellivue.data.AttributeValueList;
import drivers.philips.intellivue.data.RelativeTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntellivueRelativeClock {
    private final Logger log = LoggerFactory.getLogger(IntellivueRelativeClock.class);

    public IntellivueRelativeClock() {}

    // For a point in time this is currentTime-runTime
    // Or, in other words, the time when the device started
    // according to the device clock
    // In milliseconds
    private long deviceStartTimeMS;


    public IceInstant instantFromRelative(RelativeTime time) {
        return new IceInstant(this.deviceStartTimeMS + time.toMilliseconds(), TimeUnit.MS);
    }

    public void setDeviceStartTime(AttributeValueList attrs) {

        Attribute<AbsoluteTime> clockTime = attrs.getAttribute(AttributeId.NOM_ATTR_TIME_ABS, AbsoluteTime.class);
        Attribute<RelativeTime> offsetTime = attrs.getAttribute(AttributeId.NOM_ATTR_TIME_REL, RelativeTime.class);

        boolean ok = true;
        if (null == clockTime) {
            log.warn("No NOM_ATTR_TIME_ABS in MDS Create");
            ok = false;
        }
        if (null == offsetTime) {
            log.warn("No NOM_ATTR_TIME_REL in MDS Create");
            ok = false;
        }
        if (ok) {
            long currentTime = clockTime.getValue().getDate().getTime();
            long runTime = offsetTime.getValue().toMilliseconds();
            deviceStartTimeMS = currentTime - runTime;
        }
    }
}
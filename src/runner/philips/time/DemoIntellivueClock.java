package runner.philips.time;

import common.time.Clock;
import common.time.IceInstant;
import common.time.IceInstantInterface;
import common.time.IceInstantCombined;
import drivers.philips.intellivue.attribute.Attribute;
import drivers.philips.intellivue.data.AbsoluteTime;
import drivers.philips.intellivue.data.AttributeId;
import drivers.philips.intellivue.data.AttributeValueList;
import drivers.philips.intellivue.data.RelativeTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoIntellivueClock implements Clock {
    private final Logger log = LoggerFactory.getLogger(DemoIntellivueClock.class);

    private final Clock ref;

    public DemoIntellivueClock(Clock ref) {
        this.ref = ref;

    }
    // For a point in time this is currentTime-runTime
    // Or, in other words, the time when the device started
    // according to the device clock
    private long startTimeInDeviceTime;

    @Override
    public IceInstantInterface instant() {
        return ref.instant();
    }

    public IceInstantInterface instantFromRelative(RelativeTime time) {
        IceInstantInterface deviceTime = new IceInstant(receiveDateTime(time));
        return new IceInstantCombined(instant(), deviceTime);
    }

    public void receiveDateTime(AttributeValueList attrs) {

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
            startTimeInDeviceTime = currentTime - runTime;
        }
    }

    public long receiveDateTime(RelativeTime time) {
        // TBD - make it handler microseconds
        // long microseconds = time.toMicroseconds();
        long runningTime = time.toMilliseconds();
        long currentTimeInDeviceTime = startTimeInDeviceTime + runningTime;
        return currentTimeInDeviceTime;
    }
}
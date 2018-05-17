package common.time;

public interface Clock {

    /**
     * @return a IceInstant representing the current instantFromRelative as defined by the clock, not null
     */
    IceInstantInterface instant();

}

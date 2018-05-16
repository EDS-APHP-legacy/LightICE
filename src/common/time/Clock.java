package common.time;

public interface Clock {

    /**
     * @return a ReadOnlyClock representing the current instant as defined by the clock, not null
     */
    ReadOnlyClockInterface instant();

}

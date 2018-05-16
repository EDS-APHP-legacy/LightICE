package common.time;

public class WallClock implements Clock {

    @Override
    public ReadOnlyClockInterface instant() {
        return new ReadOnlyClock(getTimeInMillis());
    }

    protected long getTimeInMillis()
    {
        return System.currentTimeMillis();
    }

}
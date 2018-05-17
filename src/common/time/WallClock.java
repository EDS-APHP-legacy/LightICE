package common.time;

public class WallClock implements Clock {

    @Override
    public IceInstantInterface instant() {
        return new IceInstant(getTimeInMillis());
    }

    protected long getTimeInMillis()
    {
        return System.currentTimeMillis();
    }

}
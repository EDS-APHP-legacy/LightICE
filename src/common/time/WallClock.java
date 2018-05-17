package common.time;

public class WallClock implements Clock {

    @Override
    public IceInstant instant() {
        return IceInstant.now();
    }
}
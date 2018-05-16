package common.time;

import java.time.Instant;

public class ReadOnlyClock implements ReadOnlyClockInterface {
    private final Instant ms;

    public ReadOnlyClock(long time) {
        this(Instant.ofEpochMilli(time));
    }
    public ReadOnlyClock(Instant time) {
        ms = time;
    }

    @Override
    public String toString() {
        return ms.toString();
    }

    @Override
    public Instant getTime() {
        return ms;
    }

    @Override
    public Instant getDeviceTime() {
        return ms;
    }

    @Override
    public boolean hasDeviceTime() {
        return true;
    }

    @Override
    public ReadOnlyClockInterface refineResolutionForFrequency(int hertz, int size) {
        return this;
    }
}

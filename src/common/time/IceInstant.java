package common.time;

import java.time.Instant;

public class IceInstant implements IceInstantInterface {
    private final Instant ms;

    public IceInstant(long time) {
        this(Instant.ofEpochMilli(time));
    }
    public IceInstant(Instant time) {
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
    public IceInstantInterface refineResolutionForFrequency(int hertz, int size) {
        return this;
    }
}

package common.time;

import java.time.Instant;

public class ReadOnlyCombinedClock implements ReadOnlyClockInterface {

    final ReadOnlyClockInterface ref;
    final ReadOnlyClockInterface dev;

    public ReadOnlyCombinedClock(ReadOnlyClockInterface ref, ReadOnlyClockInterface dev) {
        this.ref = ref;
        this.dev = dev;
    }

    @Override
    public String toString() {
        return ref.toString() + " " + dev.toString();
    }

    @Override
    public Instant getTime() {
        return ref.getTime();
    }

    @Override
    public boolean hasDeviceTime() {
        return dev.hasDeviceTime();
    }

    @Override
    public Instant getDeviceTime() {
        return dev.getTime();
    }

    @Override
    public ReadOnlyClockInterface refineResolutionForFrequency(int hertz, int size) {
        ref.refineResolutionForFrequency(hertz, size);
        return this;
    }
}

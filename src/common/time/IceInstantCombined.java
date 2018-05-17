package common.time;

import java.time.Instant;

public class IceInstantCombined implements IceInstantInterface {

    final IceInstantInterface ref;
    final IceInstantInterface dev;

    public IceInstantCombined(IceInstantInterface ref, IceInstantInterface dev) {
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
    public IceInstantInterface refineResolutionForFrequency(int hertz, int size) {
        ref.refineResolutionForFrequency(hertz, size);
        return this;
    }
}

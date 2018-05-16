package common.time;

import java.time.Instant;

public interface ReadOnlyClockInterface {
    Instant getTime();
    boolean hasDeviceTime();
    Instant getDeviceTime();
    ReadOnlyClockInterface refineResolutionForFrequency(int hertz, int size);
}

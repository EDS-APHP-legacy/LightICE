package common.time;

import java.time.Instant;

public interface IceInstantInterface {
    Instant getTime();
    boolean hasDeviceTime();
    Instant getDeviceTime();
    IceInstantInterface refineResolutionForFrequency(int hertz, int size);
}

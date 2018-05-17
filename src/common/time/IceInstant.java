package common.time;

import java.time.Instant;

public class IceInstant {
    private final Instant ms;

    public static IceInstant now() {
        return new IceInstant(System.currentTimeMillis(), TimeUnit.MS);
    }

    public IceInstant(long timestamp, TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.SEC)
            this.ms = Instant.ofEpochSecond(timestamp);
        else if (timeUnit == TimeUnit.MS)
            this.ms = Instant.ofEpochMilli(timestamp);
        else if (timeUnit == TimeUnit.µS)
            this.ms = Instant.ofEpochSecond(timestamp / 1000000L, timestamp % 1000000L);
        else if (timeUnit == TimeUnit.NS) {
            this.ms = Instant.ofEpochSecond(timestamp / 1000000000L, timestamp % 1000000000L);
            System.err.println("[WARNING] Using IceInstant with nanoseconds can lead to wrapping around to Long.MIN_VALUE.");
        }
        else {
            throw new UnsupportedOperationException("TimeUnit " + timeUnit.toString() + " is not supported by IceInstant.");
        }
    }

    public String toString() {
        return ms.toString();
    }

    public Instant getTime() {
        return ms;
    }

    public Instant getDeviceTime() {
        return ms;
    }

    public boolean hasDeviceTime() {
        return true;
    }

    public IceInstant refineResolutionForFrequency(int hertz, int size) {
        return this;
    }
}

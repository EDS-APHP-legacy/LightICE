package fakedds;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public final class Time_t extends Struct implements Externalizable, Copyable {
    public static final int TIME_INVALID_SEC = -1;
    public static final int TIMESTAMP_INVALID_SEC = -1;
    public static final int TIME_INVALID_NSEC = -1;
    public static final int TIMESTAMP_INVALID_NSEC = -1;
    public static final Time_t TIME_INVALID = new Time_t(-1, -1);
    public static final Time_t TIME_ZERO = new Time_t(0, 0);
    public int sec;
    public int nanosec;

    public Time_t(Time_t var1) {
        this(var1.sec, var1.nanosec);
    }

    public Time_t(int var1, int var2) {
        this.sec = var1;
        this.nanosec = var2;
    }

    public static Time_t from_micros(long var0) {
        return new Time_t((int)(var0 / 1000000L), (int)(var0 % 1000000L) * 1000);
    }

    public static Time_t from_nanos(long var0) {
        return new Time_t((int)(var0 / 1000000000L), (int)(var0 % 1000000000L));
    }

    public static Time_t from_millis(long var0) {
        return new Time_t((int)(var0 / 1000L), (int)(var0 % 1000L) * 1000000);
    }

    public static Time_t from_seconds(long var0) {
        return new Time_t((int)var0, 0);
    }

    public static Time_t now() {
        long var0 = System.currentTimeMillis();
        return new Time_t((int)(var0 / 1000L), (int)(var0 % 1000L * 1000000L));
    }

    public boolean is_invalid() {
        return this.sec < 0 || this.nanosec < 0;
    }

    public boolean is_zero() {
        return this.sec == 0 && this.nanosec == 0;
    }

    public void pull_from_nativeI(long var1) {
        this.sec = get_native_secI(var1);
        this.nanosec = get_native_nanosecI(var1);
    }

    public void writeExternal(ObjectOutput var1) throws IOException {
        var1.writeInt(this.sec);
        var1.writeInt(this.nanosec);
    }

    public void readExternal(ObjectInput var1) throws IOException, ClassNotFoundException {
        this.sec = var1.readInt();
        this.nanosec = var1.readInt();
    }

    public Object copy_from(Object var1) {
        this.sec = ((Time_t)var1).sec;
        this.nanosec = ((Time_t)var1).nanosec;
        return this;
    }

    public final boolean equals(Object var1) {
        boolean var2 = false;
        if (this == var1) {
            var2 = true;
        } else if (var1 != null && var1.getClass() == this.getClass()) {
            Time_t var3 = (Time_t)var1;
            var2 = var3.sec == this.sec && var3.nanosec == this.nanosec;
        }

        return var2;
    }

    public final int hashCode() {
        return this.sec + this.nanosec;
    }

    public void push_to_nativeI(long var1) {
        push_to_nativeI(var1, this.sec, this.nanosec);
    }

    private static native int get_native_secI(long var0);

    private static native int get_native_nanosecI(long var0);

    private static native void push_to_nativeI(long var0, int var2, int var3);
}

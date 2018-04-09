package fakedds;

import java.io.Serializable;


public final class InstanceHandle_t implements Copyable, Serializable {
    public static final int KEY_HASH_MAX_LENGTH = 16;
    private final byte[] _value;
    private int _length;
    boolean is_valid;
    public static final InstanceHandle_t HANDLE_NIL = new InstanceHandle_t();

    public InstanceHandle_t() {
        this._value = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        this._length = 16;
        this.is_valid = false;
        this.is_valid = false;
    }

    public InstanceHandle_t(InstanceHandle_t var1) {
        this();
        this.copy_from(var1);
    }

    public boolean is_nil() {
        return !this.is_valid;
    }

    public Object copy_from(Object var1) {
        InstanceHandle_t var2 = (InstanceHandle_t)var1;
        this._length = var2._length;
        this.is_valid = var2.is_valid;
        if (this.is_valid) {
            System.arraycopy(var2._value, 0, this._value, 0, this._length);
        }

        return this;
    }

    public static InstanceHandle_t createI() {
        return new InstanceHandle_t();
    }

    public static InstanceHandle_t createCopyI(InstanceHandle_t var0) {
        return new InstanceHandle_t(var0);
    }

    public byte[] get_valuesI() {
        return this._value;
    }

    public int get_lengthI() {
        return this._length;
    }

    public void set_lengthI(int var1) {
        this._length = var1;
    }

    public byte[] get_values_as_byte_arrayI() {
        return this._value;
    }

    public void pull_from_nativeI(long var1) {
        this._length = get_native_valueI(var1, this._value);
        if (this._length < 0) {
            this.is_valid = false;
            this._length = 16;
        } else {
            this.is_valid = true;
        }

    }

    public void push_to_nativeI(long var1) {
        push_to_nativeI(var1, this._value, this._length, this.is_valid);
    }

    public boolean equals(Object var1) {
        boolean var2;
        if (this == var1) {
            var2 = true;
        } else if (var1 instanceof InstanceHandle_t) {
            InstanceHandle_t var3 = (InstanceHandle_t)var1;
            if (!this.is_valid && !var3.is_valid) {
                var2 = true;
            } else if (this.is_valid == var3.is_valid && this._length == var3._length) {
                var2 = true;

                for(int var4 = 0; var4 < this._length; ++var4) {
                    if (this._value[var4] != var3._value[var4]) {
                        var2 = false;
                        break;
                    }
                }
            } else {
                var2 = false;
            }
        } else {
            var2 = false;
        }

        return var2;
    }

    public int hashCode() {
        int var1 = 0;
        if (!this.is_nil()) {
            for(int var2 = 0; var2 < this._length; ++var2) {
                var1 += this._value[var2];
            }
        }

        return var1;
    }

    public String toString() {
        String var1 = this.getClass().getName();
        String var2 = this.getClass().getPackage().getName();
        String var3 = var1.substring(var2.length() + 1);
        StringBuffer var4 = new StringBuffer(var3);
        if (this.is_nil()) {
            var4.append(".HANDLE_NIL");
        } else {
            var4.append('[');
            var4.append(Byte.toString(this._value[0]));

            for(int var5 = 1; var5 < this._length; ++var5) {
                var4.append(',').append(this._value[var5]);
            }

            var4.append(']');
        }

        return var4.toString();
    }

    private static native int get_native_valueI(long var0, byte[] var2);

    private static native void push_to_nativeI(long var0, byte[] var2, int var3, boolean var4);
}

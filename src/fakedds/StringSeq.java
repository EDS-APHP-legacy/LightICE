package fakedds;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;


public class StringSeq extends ArraySequence implements Copyable, Externalizable {
    private static final long serialVersionUID = 4383933938484577158L;

    public StringSeq() {
        super(String.class);
    }

    public StringSeq(int var1) {
        super(String.class, var1);
    }

    public StringSeq(Collection var1) {
        super(String.class, var1);
    }

    public final Object copy_from(Object var1) {
        StringSeq var2 = (StringSeq)var1;
        int var3 = var2.size();
        int var4 = this.size();
        this.ensureCapacity(var3);
        if (var3 < var4) {
            this.removeRange(var3, var4);
        }

        int var5;
        for(var5 = 0; var5 < var4 && var5 < var3; ++var5) {
            this.set(var5, var2.get(var5));
        }

        for(var5 = var4; var5 < var3; ++var5) {
            this.add(var2.get(var5));
        }

        return this;
    }

    public void readExternal(ObjectInput var1) throws IOException {
        this.clear();
        int var2 = var1.readInt();
        if (var1 instanceof CdrObjectInput) {
            CdrObjectInput var3 = (CdrObjectInput)var1;

            for(int var4 = 0; var4 < var2; ++var4) {
                this.add(var3.readString());
            }
        } else {
            for(int var5 = 0; var5 < var2; ++var5) {
                this.add(var1.readUTF());
            }
        }

    }

    public void writeExternal(ObjectOutput var1) throws IOException {
        int var2 = this.size();
        var1.writeInt(var2);
        if (var1 instanceof CdrObjectOutput) {
            CdrObjectOutput var3 = (CdrObjectOutput)var1;

            for(int var4 = 0; var4 < var2; ++var4) {
                var3.writeString((String)this.get(var4));
            }
        } else {
            for(int var5 = 0; var5 < var2; ++var5) {
                var1.writeUTF((String)this.get(var5));
            }
        }

    }

    public static void readStringArray(String[] var0, CdrObjectInput var1, int var2) throws IOException {
        for(int var3 = 0; var3 < var2; ++var3) {
            var0[var3] = var1.readString();
        }

    }

    public static void writeStringArray(String[] var0, CdrObjectOutput var1, int var2, int var3) throws IOException {
        for(int var4 = 0; var4 < var2; ++var4) {
            var1.writeString(var0[var4], var3);
        }

    }

    public final void pull_from_nativeI(long var1) {
        int var3 = get_native_lengthI(var1);
        int var4 = this.size();
        int var5;
        if (var3 < var4) {
            this.removeRange(var3, var4);
        } else if (var3 > var4) {
            this.ensureCapacity(var3);

            for(var5 = var4; var5 < var3; ++var5) {
                this.add((Object)null);
            }
        }

        for(var5 = 0; var5 < var3; ++var5) {
            String var6 = get_native_elementI(var1, var5, (String)this.get(var5));
            this.set(var5, var6);
        }

    }
/*

    public final void push_to_nativeI(long var1) {
        int var3 = this.size();
        int var4 = set_native_lengthI(var1, var3);
        int var5 = get_native_lengthI(var1);

        for(int var6 = 0; var6 < var3 && var6 < var5; ++var6) {
            String var7 = (String)this.get(var6);
            RETCODE_ERROR.check_return_codeI(set_native_elementI(var1, var6, var7));
        }

        RETCODE_ERROR.check_return_codeI(var4);
    }
*/

    private static native int get_native_lengthI(long var0);

    private static native int set_native_lengthI(long var0, int var2);

    private static native String get_native_elementI(long var0, int var2, String var3);

    private static native int set_native_elementI(long var0, int var2, String var3);
}

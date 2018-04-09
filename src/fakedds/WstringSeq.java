package fakedds;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;


public final class WstringSeq extends StringSeq {
    public WstringSeq() {
    }

    public WstringSeq(int var1) {
        super(var1);
    }

    public WstringSeq(Collection var1) {
        super(var1);
    }

    public void readExternal(ObjectInput var1) throws IOException {
        if (var1 instanceof CdrObjectInput) {
            CdrObjectInput var2 = (CdrObjectInput)var1;
            this.clear();
            int var3 = var1.readInt();

            for(int var4 = 0; var4 < var3; ++var4) {
                this.add(var2.readWstring());
            }
        } else {
            super.readExternal(var1);
        }

    }

    public void writeExternal(ObjectOutput var1) throws IOException {
        if (var1 instanceof CdrObjectOutput) {
            CdrObjectOutput var2 = (CdrObjectOutput)var1;
            int var3 = this.size();
            var1.writeInt(var3);

            for(int var4 = 0; var4 < var3; ++var4) {
                var2.writeWstring((String)this.get(var4));
            }
        } else {
            super.writeExternal(var1);
        }

    }

    public static void readWstringArray(String[] var0, CdrObjectInput var1, int var2) throws IOException {
        for(int var3 = 0; var3 < var2; ++var3) {
            var0[var3] = var1.readWstring();
        }

    }

    public static void writeWstringArray(String[] var0, CdrObjectOutput var1, int var2, int var3) throws IOException {
        for(int var4 = 0; var4 < var2; ++var4) {
            var1.writeWstring(var0[var4], var3);
        }

    }
}

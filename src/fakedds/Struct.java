package fakedds;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class Struct implements Serializable {
    public abstract boolean equals(Object var1);

    public abstract int hashCode();

    public String toString() {
        StringBuffer var1 = new StringBuffer(128);
        String var2 = this.getClass().getName();
        String var3 = this.getClass().getPackage().getName();
        String var4 = var2.substring(var3.length() + 1);
        var1.append(var4).append('[');
        Field[] var5 = this.getClass().getFields();
        boolean var6 = true;

        for(int var7 = 0; var7 < var5.length; ++var7) {
            Field var8 = var5[var7];
            int var9 = var8.getModifiers();
            if (Modifier.isPublic(var9) && !Modifier.isStatic(var9)) {
                if (!var6) {
                    var1.append(", ");
                }

                var1.append(var8.getName()).append('=');
                String var10 = null;

                try {
                    Object var11 = var8.get(this);
                    var10 = valueOf(var11);
                } catch (IllegalAccessException var12) {
                    var10 = "???";
                }

                var1.append(var10);
                var6 = false;
            }
        }

        var1.append(']');
        return var1.toString();
    }

    protected Struct() {
    }

    protected abstract void pull_from_nativeI(long var1);

    protected abstract void push_to_nativeI(long var1);

    private static String valueOf(Object var0) {
        String var1 = null;
        if (var0 != null && var0.getClass().isArray()) {
            StringBuffer var2 = new StringBuffer(128);
            var2.append('[');
            int var3 = Array.getLength(var0);

            for(int var4 = 0; var4 < var3; ++var4) {
                var2.append(Array.get(var0, var4));
                if (var4 < var3 - 1) {
                    var2.append(", ");
                }
            }

            var2.append(']');
            var1 = var2.toString();
        } else {
            var1 = String.valueOf(var0);
        }

        return var1;
    }
}

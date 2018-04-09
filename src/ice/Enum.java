package ice;

import fakedds.Copyable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public abstract class Enum implements Comparable, Copyable, Serializable {
    private final int _ordinal;
    private final transient String _name;
    private static final int ESTIMATED_ENUM_TYPE_COUNT = 30;
    private static final HashMap _classToMap = new HashMap(30);

    public final int ordinal() {
        return this._ordinal;
    }

    public Object copy_from(Object var1) {
        if (var1 != null && this.getClass() != var1.getClass()) {
            throw new ClassCastException(var1.getClass() + " is not equal  to " + this.getClass());
        } else {
            return var1;
        }
    }

    public final String name() {
        return this._name;
    }

    public int compareTo(Object var1) {
        return this._ordinal - ((Enum)var1)._ordinal;
    }

    public final String toString() {
        return this._name;
    }

    public final boolean equals(Object var1) {
        return super.equals(var1);
    }

    public final int hashCode() {
        return this._ordinal;
    }

    public static Enum valueOf(Class var0, String var1) {
        HashMap var2 = _classToMap;
        synchronized(_classToMap) {
            Map var3 = (Map)_classToMap.get(var0);
            if (var3 == null) {
                return null;
            } else {
                Enum var4 = (Enum)var3.get(var1);
                return var4;
            }
        }
    }

    protected Enum(String var1, int var2) {
        this._ordinal = var2;
        this._name = var1;
        HashMap var3 = _classToMap;
        synchronized(_classToMap) {
            Object var4 = (Map)_classToMap.get(this.getClass());
            if (var4 == null) {
                var4 = new HashMap();
                _classToMap.put(this.getClass(), var4);
            }

            ((Map)var4).put(var1, this);
        }
    }

    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Enums can't be cloned");
    }
}

package fakedds;


public final class Utilities {
    public static boolean equalsNullSafe(Object var0, Object var1) {
        return var0 == null ? var1 == null : var0.equals(var1);
    }

    public static int hashCodeNullSafe(Object var0) {
        return var0 != null ? var0.hashCode() : System.identityHashCode(var0);
    }

    public static void rethrow(RuntimeException var0) {
        throw (RuntimeException)var0.fillInStackTrace();
    }

    private Utilities() {
    }
}

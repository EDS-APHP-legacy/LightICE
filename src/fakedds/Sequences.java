package fakedds;

final class Sequences {
    private static final ClassCastException _incorrectElementType = new ClassCastException("incorrect element type");

    public static void checkElementType(Class var0, Object var1) {
        if (var1 != null) {
            checkType(var0, var1.getClass());
        }

    }

    public static void checkType(Class var0, Class var1) {
        if (!var0.isAssignableFrom(var1)) {
            Utilities.rethrow(_incorrectElementType);
        }

    }

    private Sequences() {
    }
}

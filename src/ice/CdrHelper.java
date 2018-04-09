package ice;


public class CdrHelper {
    private static final String INDENT_STRING = "    ";

    public CdrHelper() {
    }

    public static void printIndent(StringBuffer var0, int var1) {
        for (int var2 = 0; var2 < var1; ++var2) {
            var0.append(INDENT_STRING);
        }

    }
}
package fakedds;

import java.io.IOException;
import java.io.ObjectOutput;


public interface CdrObjectOutput extends ObjectOutput {
    void writeString(String var1, int var2) throws IOException;

    void writeString(String var1) throws IOException;

    void writeWstring(String var1, int var2) throws IOException;

    void writeWstring(String var1) throws IOException;

    void writeLongDouble(double var1) throws IOException;
}

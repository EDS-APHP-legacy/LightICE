package fakedds;

import java.io.IOException;
import java.io.ObjectInput;

public interface CdrObjectInput extends ObjectInput {
    String readString() throws IOException;

    String readWstring() throws IOException;

    double readLongDouble() throws IOException;
}

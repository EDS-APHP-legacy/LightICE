package ice;

import java.io.ObjectStreamException;
import java.util.Arrays;

public class ConnectionType extends Enum
{

    public static final ConnectionType Serial = new ConnectionType("Serial", 0);
    public static final int _Serial = 0;

    public static final ConnectionType Simulated = new ConnectionType("Simulated", 1);
    public static final int _Simulated = 1;

    public static final ConnectionType Network = new ConnectionType("Network", 2);
    public static final int _Network = 2;



    public static ConnectionType valueOf(int ordinal) {
        switch(ordinal) {

            case 0: return ConnectionType.Serial;

            case 1: return ConnectionType.Simulated;

            case 2: return ConnectionType.Network;


        }
        return null;
    }

    public static ConnectionType from_int(int __value) {
        return valueOf(__value);
    }

    public static int[] getOrdinals() {
        int i = 0;
        int[] values = new int[3];


        values[i] = Serial.ordinal();
        i++;

        values[i] = Simulated.ordinal();
        i++;

        values[i] = Network.ordinal();
        i++;


        Arrays.sort(values);
        return values;
    }

    public int value() {
        return super.ordinal();
    }

    /**
     * Create a default instance
     */
    public static ConnectionType create() {


        return valueOf(0);
    }

    /**
     * Print Method
     */
    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();

        CdrHelper.printIndent(strBuffer, indent);

        if (desc != null) {
            strBuffer.append(desc).append(": ");
        }

        strBuffer.append(this);
        strBuffer.append("\n");
        return strBuffer.toString();
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(ordinal());
    }

    private ConnectionType(String name, int ordinal) {
        super(name, ordinal);
    }
}


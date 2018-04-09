package common.serial;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SerialSocket {
    String getPortIdentifier();

    void close() throws IOException;

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    enum StopBits {
        One, OneAndOneHalf, Two
    };

    enum DataBits {
        Seven, Eight
    }

    enum Parity {
        None, Odd, Even
    }

    enum FlowControl {
        Hardware, Software, None
    }

    void setSerialParams(int baud, DataBits dataBits, Parity parity, StopBits stopBits, FlowControl flowControl);
}

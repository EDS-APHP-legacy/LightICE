package common.serial;

import java.util.List;

public interface SerialProvider {
    List<String> getPortNames();

    SerialSocket connect(String portIdentifier, long timeout) throws java.io.IOException;

    void cancelConnect();

    void setDefaultSerialSettings(int baudrate, SerialSocket.DataBits dataBits, SerialSocket.Parity parity, SerialSocket.StopBits stopBits);

    void setDefaultSerialSettings(int baudrate, SerialSocket.DataBits dataBits, SerialSocket.Parity parity, SerialSocket.StopBits stopBits,
                                  SerialSocket.FlowControl flowControl);

    SerialProvider duplicate();

}

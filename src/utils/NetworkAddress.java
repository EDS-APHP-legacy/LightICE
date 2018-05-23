package utils;

import javafx.util.Pair;

public class NetworkAddress {
    private final String host;
    private final Integer port;
    public final boolean isSerial;

    public NetworkAddress(String serialDevice) {
        this.host = serialDevice;
        this.port = null;
        this.isSerial = true;
    }

    public NetworkAddress(String hostname, Integer port) {
        this.host = hostname;
        this.port = port;
        this.isSerial = false;
    }

    public Pair<String, Integer> getTCPAddr() {
        if (this.port == null)
            throw new UnsupportedOperationException("getTCPAddr interface not supported for Serial devices.");
        return new Pair<>(this.host, this.port);
    }

    public String getSerialAddr() {
        if (this.port != null)
            throw new UnsupportedOperationException("getSerialAddr interface not supported for Network devices.");
        return this.host;
    }
}

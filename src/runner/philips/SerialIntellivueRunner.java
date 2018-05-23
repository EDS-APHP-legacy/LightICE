package runner.philips;

import common.DeviceIdentity;
import common.serial.SerialProviderFactory;
import ice.ConnectionState;
import ice.ConnectionType;
import runner.AbstractDeviceRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class SerialIntellivueRunner extends AbstractIntellivueRunner {
    private RS232Adapter rs232Adapter;


    public SerialIntellivueRunner(DeviceIdentity deviceIdentity) throws IOException {
        super(deviceIdentity);
        deviceConnectivity.valid_targets.userData.addAll(SerialProviderFactory.getDefaultProvider().getPortNames());
        System.out.println(deviceConnectivity.valid_targets.userData);
    }

    @Override
    public boolean connect() {
        if (null != rs232Adapter) {
            throw new IllegalStateException("Multiple calls to connect are not currently supported");
        }
        try {
            int[] ports = getAvailablePorts(2);
            InetSocketAddress serialSide = new InetSocketAddress(InetAddress.getLoopbackAddress(), ports[0]);
            InetSocketAddress networkSide = new InetSocketAddress(InetAddress.getLoopbackAddress(), ports[1]);
            state(ConnectionState.Connecting, "initializing RS-232 to UDP adapter");
            rs232Adapter = new RS232Adapter(this.deviceIdentity.getAddr().getSerialAddr(), serialSide, networkSide, AbstractDeviceRunner.threadGroup, networkLoop);
            connect(serialSide, networkSide);
            return true;
        } catch (IOException e) {
            state(ConnectionState.Terminal, "error initializing RS-232 to UDP " + e.getMessage());
            log.error("error initializing RS-232 to UDP", e);
            return false;
        }

    }

    private static int[] getAvailablePorts(int cnt) throws IOException {
        int[] twoports = new int[cnt];
        for (int i = 0; i < cnt; i++) {
            ServerSocket ss = new ServerSocket(0);
            twoports[i] = ss.getLocalPort();
            ss.close();
        }
        return twoports;
    }

    @Override
    protected ConnectionType getConnectionType() {
        return ConnectionType.Serial;
    }

    @Override
    public void shutdown() {
        if(null != rs232Adapter) {
            rs232Adapter.shutdown();
        }
        super.shutdown();
    }

}

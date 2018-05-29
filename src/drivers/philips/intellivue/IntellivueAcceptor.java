package drivers.philips.intellivue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.List;

import common.net.NetworkLoop;
import common.net.TaskQueue;
import drivers.philips.intellivue.association.AssociationAccept;
import drivers.philips.intellivue.association.impl.AssociationAcceptImpl;
import drivers.philips.intellivue.attribute.Attribute;
import drivers.philips.intellivue.attribute.AttributeFactory;
import drivers.philips.intellivue.connectindication.ConnectIndicationImpl;
import drivers.philips.intellivue.data.*;
import drivers.philips.intellivue.data.ProtocolSupport.ProtocolSupportEntry;
import drivers.philips.intellivue.data.ProtocolSupport.ProtocolSupportEntry.ApplicationProtocol;
import drivers.philips.intellivue.data.ProtocolSupport.ProtocolSupportEntry.TransportProtocol;
import drivers.philips.intellivue.data.String;
import drivers.philips.intellivue.dataexport.CommandType;
import drivers.philips.intellivue.dataexport.DataExportInvoke;
import drivers.philips.intellivue.dataexport.command.EventReportInterface;
import drivers.philips.intellivue.dataexport.command.SetInterface;
import drivers.philips.intellivue.dataexport.command.impl.EventReport;
import drivers.philips.intellivue.dataexport.event.impl.MdsCreateEventImpl;
import drivers.philips.intellivue.dataexport.impl.DataExportInvokeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntellivueAcceptor extends Intellivue {

    private static final Logger log = LoggerFactory.getLogger(IntellivueAcceptor.class);

    protected final TaskQueue.Task<Void> beacon = new TaskQueue.TaskImpl<Void>() {
        @Override
        public Void doExecute(TaskQueue queue) {

            try {
                final List<Network.AddressSubnet> addresses = Network.getBroadcastAddresses();

                for (Network.AddressSubnet address : addresses) {
                    ConnectIndicationImpl ci = new ConnectIndicationImpl();

                    ProtocolSupportEntry e = new ProtocolSupportEntry();
                    e.setAppProtocol(ApplicationProtocol.DataOut);
                    e.setTransProtocol(TransportProtocol.UDP);
                    e.setPortNumber(port);
                    e.setOptions(0);
                    ci.getProtocolSupport().getList().add(e);

                    ci.getIpAddressInformation().setInetAddress(address.getLocalAddress());
                    Network.prefix(ci.getIpAddressInformation().getSubnetMask(), address.getPrefixLength());

                    ByteBuffer bb = ByteBuffer.allocate(5000);
                    bb.order(ByteOrder.BIG_ENDIAN);
                    ci.format(bb);
                    byte[] bytes = new byte[bb.position()];
                    bb.position(0);
                    bb.get(bytes);

                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(bytes, bytes.length, null, 24005);

                    log.info("Transmit to " + address.getInetAddress());

                    dp.setAddress(address.getInetAddress());

                    ds.send(dp);
                    ds.close();
                }

            } catch (SocketException e1) {
                log.error("failed to emit beacon", e1);
            } catch (IOException e1) {
                log.error("failed to emit beacon", e1);
            }
            return null;
        }
    };

    @Override
    protected void handler(SetInterface set, boolean confirmed) throws IOException {
        super.handler(set, confirmed);
        if (confirmed) {
        }
    }

    @Override
    protected synchronized void handleAssociationMessage(SocketAddress sockaddr, drivers.philips.intellivue.association.AssociationConnect message) {
        super.handleAssociationMessage(sockaddr, message);

        AssociationAccept acc = new AssociationAcceptImpl();
        log.debug("Sending accept:" + acc);
        try {
            final DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().setReuseAddress(true);

            channel.connect(new InetSocketAddress(((InetSocketAddress) sockaddr).getAddress(), port));

            networkLoop.register(this, channel);
            send(acc);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        MdsCreateEventImpl m = new MdsCreateEventImpl();

        Attribute<SystemModel> systemModelAttribute = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_MODEL, SystemModel.class);

        Attribute<String> as = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_BED_LABEL, drivers.philips.intellivue.data.String.class);
        Attribute<ProductionSpecification> ps = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_ID_PROD_SPECN, ProductionSpecification.class);

        ProductionSpecification.Entry e = new ProductionSpecification.Entry();
        e.getProdSpec().setString("1234567");
        e.setComponentId(ComponentId.ID_COMP_PRODUCT);
        e.setSpecType(ProductionSpecificationType.SERIAL_NUMBER);
        ps.getValue().getList().add(e);
        systemModelAttribute.getValue().setManufacturer("MD PNP");
        systemModelAttribute.getValue().setModelNumber("ICE TEST ONE");
        m.getAttributes().add(systemModelAttribute);
        m.getAttributes().add(as);
        m.getAttributes().add(ps);

        EventReportInterface er = new EventReport();
        er.setEvent(m);
        er.setEventType(OIDType.lookup(ObjectClass.NOM_NOTI_MDS_CREAT.asInt()));

        DataExportInvoke der = new DataExportInvokeImpl();
        der.setCommandType(CommandType.CMD_EVENT_REPORT);
        der.setCommand(er);

        try {
            send(der);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public IntellivueAcceptor() throws IOException {
        this(DEFAULT_UNICAST_PORT);
    }

    public IntellivueAcceptor(int port) throws IOException {
        super();
        beacon.setInterval(10000L);
        this.port = port;

    }

    private NetworkLoop networkLoop;

    public void accept(final NetworkLoop networkLoop) throws IOException {
        this.networkLoop = networkLoop;


        networkLoop.add(new TaskQueue.TaskImpl<Void>() {
            @Override
            public Void doExecute(TaskQueue queue) {
                try {
                    DatagramChannel channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.socket().setReuseAddress(true);
                    channel.socket().bind(new InetSocketAddress(port));
                    networkLoop.register(IntellivueAcceptor.this, channel);
                    networkLoop.add(beacon);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return null;
            }

        });

        // FIXME: What for?
        networkLoop.add(new TaskQueue.TaskImpl<Void>() {
            @Override
            public Void doExecute(TaskQueue queue) {
                try {
                    final DatagramChannel channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.socket().setReuseAddress(true);
                    channel.socket().bind(new InetSocketAddress(DEFAULT_UNICAST_PORT));

                    networkLoop.register(IntellivueAcceptor.this, channel);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return null;
            }
        });

    }

    protected final int port;

    public static void main(java.lang.String[] args) throws IOException {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : Intellivue.DEFAULT_UNICAST_PORT;
        final NetworkLoop networkLoop = new NetworkLoop();
        final IntellivueAcceptor intellivueAcceptor = new IntellivueAcceptor(port);
        intellivueAcceptor.accept(networkLoop);
        networkLoop.runLoop();
    }
}

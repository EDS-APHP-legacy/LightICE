/*******************************************************************************
 * Copyright (c) 2014, MD PnP Program
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package drivers.philips.intellivue;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import common.io.util.HexUtil;
import common.net.NetworkConnection;
import common.net.NetworkLoop;
import drivers.philips.intellivue.action.ExtendedPollDataRequest;
import drivers.philips.intellivue.action.ExtendedPollDataResult;
import drivers.philips.intellivue.action.SinglePollDataRequest;
import drivers.philips.intellivue.action.SinglePollDataResult;
import drivers.philips.intellivue.action.impl.ExtendedPollDataRequestImpl;
import drivers.philips.intellivue.action.impl.SinglePollDataRequestImpl;
import drivers.philips.intellivue.association.AssociationAbort;
import drivers.philips.intellivue.association.AssociationAccept;
import drivers.philips.intellivue.association.AssociationConnect;
import drivers.philips.intellivue.association.AssociationDisconnect;
import drivers.philips.intellivue.association.AssociationFinish;
import drivers.philips.intellivue.association.AssociationMessage;
import drivers.philips.intellivue.association.AssociationRefuse;
import drivers.philips.intellivue.association.impl.AssociationConnectImpl;
import drivers.philips.intellivue.association.impl.AssociationDisconnectImpl;
import drivers.philips.intellivue.attribute.Attribute;
import drivers.philips.intellivue.attribute.AttributeFactory;
import drivers.philips.intellivue.connectindication.ConnectIndication;
import drivers.philips.intellivue.data.AttributeId;
import drivers.philips.intellivue.data.Label;
import drivers.philips.intellivue.data.MdibObjectSupport;
import drivers.philips.intellivue.data.NomPartition;
import drivers.philips.intellivue.data.OIDType;
import drivers.philips.intellivue.data.ObjectClass;
import drivers.philips.intellivue.data.PollProfileSupport;
import drivers.philips.intellivue.data.ProtocolSupport;
import drivers.philips.intellivue.data.ProtocolSupport.ProtocolSupportEntry;
import drivers.philips.intellivue.data.RelativeTime;
import drivers.philips.intellivue.data.TextIdList;
import drivers.philips.intellivue.dataexport.CommandType;
import drivers.philips.intellivue.dataexport.DataExportError;
import drivers.philips.intellivue.dataexport.DataExportInvoke;
import drivers.philips.intellivue.dataexport.DataExportMessage;
import drivers.philips.intellivue.dataexport.DataExportResult;
import drivers.philips.intellivue.dataexport.ModifyOperator;
import drivers.philips.intellivue.dataexport.command.Action;
import drivers.philips.intellivue.dataexport.command.ActionResult;
import drivers.philips.intellivue.dataexport.command.CommandFactory;
import drivers.philips.intellivue.dataexport.command.EventReport;
import drivers.philips.intellivue.dataexport.command.Get;
import drivers.philips.intellivue.dataexport.command.GetResult;
import drivers.philips.intellivue.dataexport.command.Set;
import drivers.philips.intellivue.dataexport.command.SetResult;
import drivers.philips.intellivue.dataexport.impl.DataExportInvokeImpl;
import drivers.philips.intellivue.dataexport.impl.DataExportResultImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeff Plourde
 *
 */
public class Intellivue implements NetworkConnection {
    public static final int BROADCAST_PORT = 24005;
    public static final int DEFAULT_UNICAST_PORT = 24105;
    public static final int BUFFER_SIZE = 5000;

    private final ByteBuffer inBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final ByteBuffer outBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private final Protocol protocol = new CompoundProtocol();

    public Intellivue() {
        inBuffer.order(ByteOrder.BIG_ENDIAN);
        outBuffer.order(ByteOrder.BIG_ENDIAN);

    }

    private static final Logger log = LoggerFactory.getLogger(Intellivue.class);

    protected static final String lineWrap(String str) {
        return lineWrap(str, CHARS_PER_LINE);
    }

    protected static final String lineWrap(String str, int width) {
        StringBuilder sb = new StringBuilder(str);
        for (int i = sb.length() / width; i > 0; i--) {
            sb.insert(i * width, "\n");
        }
        return sb.toString();
    }

    private static final int CHARS_PER_LINE = 140;

    protected void handler(SocketAddress sockaddr, Message message, SelectionKey sk) throws IOException {
        if (null == message) {
            return;
        }

        if (log.isTraceEnabled()) {
            time.setTime(System.currentTimeMillis());
            log.trace("In Message(" + simpleDateformat.format(time) + "):\n" + lineWrap(message.toString()));
        }
        if (message instanceof DataExportMessage) {
            handler((DataExportMessage) message);
        } else if (message instanceof AssociationMessage) {
            handler(sockaddr, (AssociationMessage) message);
        } else if (message instanceof ConnectIndication) {
            handler((ConnectIndication) message, sk);
        }
    }

    protected void handler(SocketAddress sockaddr, AssociationAccept message) {
    }

    protected void handler(SocketAddress sockaddr, AssociationFinish message) throws IOException {
        AssociationDisconnect disconn = new AssociationDisconnectImpl();
        send(disconn);
    }

    protected void handler(SocketAddress sockaddr, AssociationRefuse message) {
    }

    protected void handler(SocketAddress sockaddr, AssociationDisconnect message) {
    }

    protected void handler(SocketAddress sockaddr, AssociationAbort message) {
    }

    protected void handler(SocketAddress sockaddr, AssociationConnect message) {
    }

    protected void handler(SocketAddress sockaddr, AssociationMessage message) throws IOException {

        switch (message.getType()) {
        case Connect:
            handler(sockaddr, (AssociationConnect) message);
            break;
        case Accept:
            handler(sockaddr, (AssociationAccept) message);
            break;
        case Refuse:
            handler(sockaddr, (AssociationRefuse) message);
            break;
        case Disconnect:
            handler(sockaddr, (AssociationDisconnect) message);
            break;
        case Abort:
            handler(sockaddr, (AssociationAbort) message);
            break;
        case Finish:
            handler(sockaddr, (AssociationFinish) message);
            break;
        default:
            break;
        }
    }

    public static boolean isAcceptable(ProtocolSupportEntry pse) {
        switch (pse.getAppProtocol()) {
        case DataOut:
            break;
        default:
            return false;
        }

        switch (pse.getTransProtocol()) {
        case UDP:
            break;
        default:
            return false;
        }

        return true;
    }

    public static ProtocolSupportEntry acceptable(ConnectIndication connectIndication) {
        ProtocolSupport ps = connectIndication.getProtocolSupport();
        if (null == ps || ps.getList().isEmpty()) {
            return null;
        }
        for (ProtocolSupportEntry e : ps.getList()) {
            if (isAcceptable(e)) {
                return e;
            }
        }
        return null;
    }

    protected void handler(ConnectIndication connectIndication, SelectionKey sk) {
    }

    private int invoke = 0;
    private int poll = 0;

    private final static int MAX_U_SHORT = (1 << Short.SIZE);

    private synchronized int nextInvoke() {
        return (invoke = ++invoke >= MAX_U_SHORT ? 0 : invoke);
    }

    private synchronized int nextPoll() {
        return (poll = ++poll >= MAX_U_SHORT ? 0 : poll);
    }

    @SuppressWarnings("unused")
    private synchronized int lastPoll() {
        return poll;
    }

    public int requestKeepAlive() throws IOException {
        return requestSinglePoll(ObjectClass.NOM_MOC_VMO_AL_MON, AttributeId.NOM_ATTR_GRP_VMO_STATIC);
    }

    public int requestSinglePoll(ObjectClass objectType, AttributeId attrGroup) throws IOException {
        int invoke = nextInvoke();
        DataExportInvoke message = new DataExportInvokeImpl();
        message.setCommandType(CommandType.ConfirmedAction);
        message.setInvoke(invoke);

        Action action = (Action) CommandFactory.buildCommand(CommandType.ConfirmedAction, false);
        action.getManagedObject().setOidType(OIDType.lookup(ObjectClass.NOM_MOC_VMS_MDS.asInt()));
        action.getManagedObject().getGlobalHandleId().setMdsContext(0);
        action.getManagedObject().getGlobalHandleId().setHandleId(0);
        action.setScope(0);
        action.setActionType(OIDType.lookup(ObjectClass.NOM_ACT_POLL_MDIB_DATA.asInt()));

        message.setCommand(action);

        SinglePollDataRequest req = new SinglePollDataRequestImpl();
        req.setPollNumber(nextPoll());
        req.setPolledAttributeGroup(null == attrGroup ? OIDType.lookup(0) : attrGroup.asOid());
        req.getPolledObjectType().setNomPartition(NomPartition.Object);
        req.getPolledObjectType().setOidType(OIDType.lookup(objectType.asInt()));

        action.setAction(req);

        send(message);

        return invoke;
    }

    public int requestExtendedPoll(ObjectClass objectType, Long time) throws IOException {
        return requestExtendedPoll(objectType, time, null);
    }

    public int requestExtendedPoll(ObjectClass objectType, Long time, AttributeId attrGroup) throws IOException {

        int invoke = nextInvoke();
        DataExportInvoke message = new DataExportInvokeImpl();
        message.setCommandType(CommandType.ConfirmedAction);
        message.setInvoke(invoke);

        Action action = (Action) CommandFactory.buildCommand(CommandType.ConfirmedAction, false);
        action.getManagedObject().setOidType(OIDType.lookup(ObjectClass.NOM_MOC_VMS_MDS.asInt()));
        action.getManagedObject().getGlobalHandleId().setMdsContext(0);
        action.getManagedObject().getGlobalHandleId().setHandleId(0);
        action.setScope(0);
        action.setActionType(OIDType.lookup(ObjectClass.NOM_ACT_POLL_MDIB_DATA_EXT.asInt()));

        message.setCommand(action);

        ExtendedPollDataRequest req = new ExtendedPollDataRequestImpl();
        req.setPollNumber(nextPoll());
        req.setPolledAttributeGroup(null == attrGroup ? OIDType.lookup(0) : attrGroup.asOid());

        req.getPolledObjectType().setNomPartition(NomPartition.Object);
        req.getPolledObjectType().setOidType(OIDType.lookup(objectType.asInt()));

        if (null != time) {
            Attribute<RelativeTime> timePeriod = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_TIME_PD_POLL, RelativeTime.class);
            timePeriod.getValue().fromMilliseconds(time);
            req.getPollExtra().add(timePeriod);
        }

        action.setAction(req);

        send(message);

        return invoke;
    }

    public int requestGet(OIDType oids) throws IOException {
        return requestGet(Arrays.asList(new OIDType[] { oids }));
    }

    public int requestGet(OIDType[] oids) throws IOException {
        return requestGet(Arrays.asList(oids));
    }

    public int requestGet(List<OIDType> oids) throws IOException {
        int invoke = nextInvoke();

        DataExportInvoke message = new DataExportInvokeImpl();
        message.setCommandType(CommandType.Get);
        message.setInvoke(invoke);

        Get get = (Get) CommandFactory.buildCommand(CommandType.Get, false);
        get.getManagedObject().setOidType(OIDType.lookup(ObjectClass.NOM_MOC_VMS_MDS.asInt()));
        get.getManagedObject().getGlobalHandleId().setMdsContext(0);
        get.getManagedObject().getGlobalHandleId().setHandleId(0);
        get.getAttributeId().addAll(oids);
        message.setCommand(get);
        send(message);
        return invoke;
    }

    public int requestSet(Label[] numerics, Label[] realtimeSampleArrays) throws IOException {
        int invoke;
        DataExportInvoke message = new DataExportInvokeImpl();
        message.setCommandType(CommandType.ConfirmedSet);
        message.setInvoke(invoke = nextInvoke());
        Set set = (Set) CommandFactory.buildCommand(CommandType.ConfirmedSet, false);
        set.getManagedObject().setOidType(ObjectClass.NOM_MOC_VMS_MDS);
        set.getManagedObject().getGlobalHandleId().setMdsContext(0);
        set.getManagedObject().getGlobalHandleId().setHandleId(0);

        if (numerics != null) {
            Attribute<TextIdList> ati = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_POLL_NU_PRIO_LIST, TextIdList.class);
            for (Label l : numerics) {
                ati.getValue().addTextId(l.asLong());
            }
            set.add(ModifyOperator.Replace, ati);
        }

        if (realtimeSampleArrays != null) {
            Attribute<TextIdList> ati = AttributeFactory.getAttribute(AttributeId.NOM_ATTR_POLL_RTSA_PRIO_LIST, TextIdList.class);
            for (Label l : realtimeSampleArrays) {
                ati.getValue().addTextId(l.asLong());
            }
            set.add(ModifyOperator.Replace, ati);
        }

        message.setCommand(set);
        send(message);

        return invoke;
    }

    protected void handler(EventReport eventReport, boolean confirm) throws IOException {
        if (confirm) {
            {
                DataExportResult message = new DataExportResultImpl();

                message.setCommandType(CommandType.ConfirmedEventReport);
                message.setInvoke(eventReport.getMessage().getInvoke());
                message.setCommand(eventReport.createConfirm());
                send(message);
            }
        }
    }

    protected void handler(SinglePollDataResult result) {
    }

    protected void handler(ExtendedPollDataResult result) {

    }

    protected void handler(SetResult result, boolean confirmed) {

    }

    protected void handler(Get get) {

    }

    protected void handler(Set set, boolean confirmed) throws IOException {
        if (confirmed) {
            DataExportResult message = new DataExportResultImpl();
            message.setCommandType(CommandType.ConfirmedSet);
            message.setInvoke(set.getMessage().getInvoke());
            message.setCommand(set.createResult());
            send(message);
        }

    }

    protected void handler(GetResult result) {
    }

    protected void handler(ActionResult action, boolean request) {
        ObjectClass objectclass = ObjectClass.valueOf(action.getActionType().getType());

        if (null == objectclass) {
            return;
        }
        if (!request) {
            switch (objectclass) {
            case NOM_ACT_POLL_MDIB_DATA:
                handler((SinglePollDataResult) action.getAction());
                break;
            case NOM_ACT_POLL_MDIB_DATA_EXT:
                handler((ExtendedPollDataResult) action.getAction());
                break;
            default:
                log.warn("Unknown action=" + action);
                break;
            }
        } else {
            switch (objectclass) {
            case NOM_ACT_POLL_MDIB_DATA:
                handler((SinglePollDataRequest) action.getAction());
                break;
            case NOM_ACT_POLL_MDIB_DATA_EXT:
                handler((ExtendedPollDataRequest) action.getAction());
                break;
            default:
                log.warn("Unknown action=" + action);
                break;
            }
        }
    }

    protected void handler(SinglePollDataRequest action) {

    }

    protected void handler(ExtendedPollDataRequest action) {

    }

    protected void handler(DataExportResult message) {
        switch (message.getCommandType()) {
        case ConfirmedAction:
            handler((ActionResult) message.getCommand(), false);
            break;
        case Get:
            handler((GetResult) message.getCommand());
            break;
        case ConfirmedSet:
            handler((SetResult) message.getCommand(), true);
            break;
        case Set:
            handler((SetResult) message.getCommand(), false);
            break;
        default:
            log.warn("Unknown CommandType=" + message.getCommandType());
            break;
        }
    }

    protected void handler(DataExportInvoke message) throws IOException {
        switch (message.getCommandType()) {
        case ConfirmedEventReport:
            handler((EventReport) message.getCommand(), true);
            break;
        case EventReport:
            handler((EventReport) message.getCommand(), false);
            break;
        case ConfirmedAction:
            handler((Action) message.getCommand(), true);
            break;
        case ConfirmedSet:
            handler((Set) message.getCommand(), true);
            break;
        case Get:
            handler((Get) message.getCommand());
            break;
        case Set:
            handler((Set) message.getCommand(), false);
            break;
        default:
            log.warn("Unknown invoke=" + message);
            break;
        }
    }

    protected void handler(DataExportError error) throws IOException {

    }

    protected void handler(DataExportMessage message) throws IOException {
        if (null == message) {
            return;
        }
        switch (message.getRemoteOperation()) {
        case Invoke:
            handler((DataExportInvoke) message);
            break;
        case Result:
        case LinkedResult:
            handler((DataExportResult) message);
            break;
        case Error:
            handler((DataExportError) message);
            break;
        default:
            log.warn("Unknown remoteOperation:" + message.getRemoteOperation());
            break;
        }

    }

    public void requestAssociation() throws IOException {
        AssociationConnect req = new AssociationConnectImpl();
        PollProfileSupport pps = req.getUserInfo().getPollProfileSupport();

        // On our Revision J MP70 500ms is the minimum supported
        pps.getMinPollPeriod().fromMilliseconds(500L);
        pps.setMaxMtuRx(1456);
        pps.setMaxMtuTx(1456);

        MdibObjectSupport obj = req.getUserInfo().getMdibObjectSupport();
        obj.addClass(ObjectClass.NOM_MOC_VMS_MDS, 1);
        obj.addClass(ObjectClass.NOM_MOC_VMO_METRIC_NU, 0xC9);
        obj.addClass(ObjectClass.NOM_MOC_VMO_METRIC_SA_RT, 0x3C);
        obj.addClass(ObjectClass.NOM_MOC_VMO_METRIC_ENUM, 0x10);
        obj.addClass(ObjectClass.NOM_MOC_PT_DEMOG, 1);
        obj.addClass(ObjectClass.NOM_MOC_VMO_AL_MON, 1);

        // obj.addClass(ObjectClass.NOM_MOC_SCAN, 1);
        // obj.addClass(ObjectClass.NOM_MOC_SCAN_CFG, 1);
        // obj.addClass(ObjectClass.NOM_MOC_SCAN_CFG_EPI, 1);
        // obj.addClass(ObjectClass.NOM_MOC_SCAN_CFG_PERI, 1);
        // obj.addClass(ObjectClass.NOM_MOC_SCAN_CFG_PERI_FAST, 1);

        // obj.addClass(ObjectClass.NOM_MOC_SCAN_UCFG, 1);
        // obj.addClass(ObjectClass.NOM_MOC_SCAN_UCFG_ALSTAT, 1);
        // obj.addClass(ObjectClass.NOM_MOC_SCAN_UCFG_CTXT, 1);

        send(req);

    }

    private List<Message> messageQueue = new ArrayList<Message>();

    private final synchronized int write(DatagramChannel channel, Message message) throws IOException {
        outBuffer.clear();
        protocol.format(message, outBuffer);
        outBuffer.flip();
        outBuffer.mark();

        int cnt = channel.write(outBuffer);

        if (cnt > 0 && log.isTraceEnabled()) {
            outBuffer.reset();
            time.setTime(System.currentTimeMillis());
            log.trace("To " + channel.getRemoteAddress() + "\n" + HexUtil.dump(outBuffer, 50));
        }
        return cnt;
    }

    @Override
    public synchronized void write(SelectionKey sk) throws IOException {
        @SuppressWarnings("unused")
        DatagramChannel channel = (DatagramChannel) sk.channel();
        Message message = null;
        message = messageQueue.isEmpty() ? null : messageQueue.remove(0);

        if (write((DatagramChannel) sk.channel(), message) == 0)
            messageQueue.add(0, message);
        
        if (messageQueue.isEmpty()) {
            sk.interestOps(sk.interestOps() & ~SelectionKey.OP_WRITE);
        } else {
            sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
        }
    }

    protected void readyToWrite() {
    }

    /**
     * Called externally to send a message
     * 
     * @param message
     * @return
     * @throws IOException
     */
    public synchronized boolean send(Message message) throws IOException {
        if (null == message) {
            return false;
        }

        if (log.isTraceEnabled()) {
            time.setTime(System.currentTimeMillis());
            log.trace("Out Message(" + simpleDateformat.format(time) + "):\n" + lineWrap(message.toString()));
        }

        // Try to write the datagram, if unavailable then set interestOps

        int cnt = write(registeredChannel, message);
        if (cnt == 0) {
            registeredKey.interestOps(registeredKey.interestOps() | SelectionKey.OP_WRITE);
            messageQueue.add(message);
            registeredNetworkLoop.wakeup();
            return false;
        } else {
            return true;
        }
    }

    private final Date time = new Date();
    private final DateFormat simpleDateformat = new SimpleDateFormat("HH:mm:ss.SSS");

    protected SelectionKey registeredKey;
    protected NetworkLoop registeredNetworkLoop;
    protected DatagramChannel registeredChannel;

    @Override
    public void registered(NetworkLoop networkLoop, SelectionKey key) {
        if (null != registeredKey && !registeredKey.equals(key)) {
            log.warn("Replacing existing key registration (" + registeredKey + ") with " + key);
        }
        if (null != registeredNetworkLoop && !registeredNetworkLoop.equals(networkLoop)) {
            log.warn("Replacing existing NetworkLoop registration (" + registeredNetworkLoop + ") with " + networkLoop);
        }
        if (!(key.channel() instanceof DatagramChannel)) {
            throw new IllegalArgumentException("Please register only SelectionKeys for DatagramChannels, not " + key.channel().getClass());
        }

        this.registeredChannel = (DatagramChannel) key.channel();
        this.registeredKey = key;
        this.registeredNetworkLoop = networkLoop;
    }

    @Override
    public void unregistered(NetworkLoop networkLoop, SelectionKey key) {
        this.registeredChannel = null;
        this.registeredKey = null;
        this.registeredNetworkLoop = null;
    }

    @Override
    public void read(SelectionKey sk) throws IOException {
        if (sk.channel() instanceof DatagramChannel) {
            DatagramChannel channel = (DatagramChannel) sk.channel();

            inBuffer.clear();
            SocketAddress sockaddr = channel.receive(inBuffer);
            inBuffer.flip();
            if (inBuffer.hasRemaining()) {
                if (log.isTraceEnabled()) {
                    time.setTime(System.currentTimeMillis());
                    log.trace("From " + channel.getRemoteAddress() + " on " + channel.socket().getLocalAddress() + "\n" + HexUtil.dump(inBuffer, 50));
                }
                handler(sockaddr, protocol.parse(inBuffer), sk);
            }
        }
    }

}

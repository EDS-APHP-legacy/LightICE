package drivers.philips.intellivue.dataexport.command.impl;

import java.nio.ByteBuffer;

import common.io.util.Bits;
import drivers.philips.intellivue.action.ActionFactory;
import drivers.philips.intellivue.data.ManagedObjectIdentifier;
import drivers.philips.intellivue.data.OIDType;
import drivers.philips.intellivue.data.ObjectClass;
import drivers.philips.intellivue.dataexport.DataExportAction;
import drivers.philips.intellivue.dataexport.DataExportMessage;
import drivers.philips.intellivue.dataexport.command.ActionResultInterface;
import drivers.philips.intellivue.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionResult implements ActionResultInterface {
    // The Action Result command is used as a response message to the Action message. It is appended to the
    // Operation Result message or an Operation Linked Result message (if the size of the returned data
    // exceeds a maximum message size). The command_type is set to CMD_CONFIRMED_ACTION.

    protected final ManagedObjectIdentifier managedObject = new ManagedObjectIdentifier();
    protected OIDType actionType;

    protected DataExportMessage message;
    protected DataExportAction action;

    private static final Logger log = LoggerFactory.getLogger(ActionResult.class);

    @Override
    public void parse(ByteBuffer bb) {
        managedObject.parse(bb);
        actionType = OIDType.parse(bb);
        int length = Bits.getUnsignedShort(bb);
        action = ActionFactory.buildAction(actionType, false);
        if (null == action) {
            log.warn("Unknown action type:" + actionType);

            bb.position(bb.position() + length);
        } else {
            action.setAction(this);
            action.parse(bb);

        }
    }

    @Override
    public void parseMore(ByteBuffer bb) {
        managedObject.parse(bb);
        actionType = OIDType.parse(bb);
        int length = Bits.getUnsignedShort(bb);
        if (null == action) {
            log.warn("Unknown action type:" + actionType);
            bb.position(bb.position() + length);
        } else {
            action.parseMore(bb);

        }
    }

    @Override
    public void format(ByteBuffer bb) {
        managedObject.format(bb);
        actionType.format(bb);

        Util.PrefixLengthShort.write(bb, action);
    }

    @Override
    public ManagedObjectIdentifier getManagedObject() {
        return managedObject;
    }

    @Override
    public OIDType getActionType() {
        return actionType;
    }

    @Override
    public String toString() {
        java.lang.String at = ObjectClass.valueOf(actionType.getType()) == null ? actionType.toString() : ObjectClass.valueOf(actionType.getType())
                .toString();
        return "[managedObject=" + managedObject + ",actionType=" + at + ",action=" + action + "]";
    }

    @Override
    public DataExportMessage getMessage() {
        return message;
    }

    @Override
    public void setMessage(DataExportMessage message) {
        this.message = message;
    }

    @Override
    public void setActionType(OIDType type) {
        this.actionType = type;
    }

    @Override
    public DataExportAction getAction() {
        return action;
    }

    @Override
    public void setAction(DataExportAction action) {
        this.action = action;
    }
}

package drivers.philips.intellivue.dataexport.command.impl;

import java.nio.ByteBuffer;

import common.io.util.Bits;
import drivers.philips.intellivue.action.ActionFactory;
import drivers.philips.intellivue.data.OIDType;
import drivers.philips.intellivue.dataexport.DataExportAction;
import drivers.philips.intellivue.dataexport.command.Action;
import drivers.philips.intellivue.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionArgument extends ActionResult implements Action {
    // The ACTION command (CMD_CONFIRMED_ACTION) is used to call a Protocol specific
    // method in the receiver. The Protocol uses this command to call the Data Poll method which returns
    // device data. The ACTION command is appended to the Operation Invoke message.

    private long scope;

    private static final Logger log = LoggerFactory.getLogger(ActionArgument.class);

    @Override
    public void parse(ByteBuffer bb) {
        managedObject.parse(bb);
        scope = Bits.getUnsignedInt(bb);
        actionType = OIDType.parse(bb);
        int length = Bits.getUnsignedShort(bb);
        action = ActionFactory.buildAction(actionType, true);
        if (null == action) {
            log.warn("Unknown action type:" + actionType);

            bb.position(bb.position() + length);
        } else {
            action.setAction(this);
            action.parse(bb);
        }
    }

    @Override
    public void format(ByteBuffer bb) {
        managedObject.format(bb);
        Bits.putUnsignedInt(bb, scope);
        actionType.format(bb);

        Util.PrefixLengthShort.write(bb, action);

    }

    @Override
    public long getScope() {
        return scope;
    }

    @Override
    public void setScope(long x) {
        this.scope = x;
    }

    @Override
    public String toString() {
        return "[managedObject=" + managedObject + ",scope=" + scope + ",actionType=" + actionType + ",action=" + action + "]";
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

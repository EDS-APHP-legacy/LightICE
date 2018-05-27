package drivers.philips.intellivue.dataexport;

import java.util.Map;
import drivers.philips.intellivue.OrdinalEnum;

public enum CommandType implements OrdinalEnum.IntType {
    CMD_EVENT_REPORT(0),
    CMD_CONFIRMED_EVENT_REPORT(1),
    CMD_GET(3),
    CMD_SET(4),
    CMD_CONFIRMED_SET(5),
    CMD_CONFIRMED_ACTION(7);

    private final int x;

    private CommandType(final int x) {
        this.x = x;
    }

    private static final Map<Integer, CommandType> map = OrdinalEnum.buildInt(CommandType.class);

    public static final CommandType valueOf(int x) {
        return map.get(x);
    }

    public final int asInt() {
        return x;
    }

    public String toString() {
        switch (this) {
            case CMD_EVENT_REPORT:
                return "An Event Report is used for an unsolicited event message.";
            case CMD_CONFIRMED_EVENT_REPORT:
                return "The Confirmed Event Report is an unsolicited event message for which the receiver must send an Event Report Result message.";
            case CMD_GET:
                return "The Get operation is used to request attribute values of managed objects. The receiver responds with a Get Result message.";
            case CMD_SET:
                return "The Set operation is used to set values of managed objects.";
            case CMD_CONFIRMED_SET:
                return "The Confirmed Set operation is used to set attribute values of managed objects. The receiver responds with a Set Result message.";
            case CMD_CONFIRMED_ACTION:
                return "The Confirmed Action is a message to invoke an activity on the receiver side. The receiver must send an Action Result message.";
        }
        return "";
    }
}

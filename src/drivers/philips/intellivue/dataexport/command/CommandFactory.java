package drivers.philips.intellivue.dataexport.command;

import drivers.philips.intellivue.dataexport.CommandType;
import drivers.philips.intellivue.dataexport.DataExportCommand;
import drivers.philips.intellivue.dataexport.command.impl.*;
import drivers.philips.intellivue.dataexport.command.impl.ActionResult;

public class CommandFactory {
    public static final DataExportCommand buildCommand(CommandType commandType, boolean result) {
        switch (commandType) {
        case CMD_EVENT_REPORT:
        case CMD_CONFIRMED_EVENT_REPORT:
            return new EventReport();
        case CMD_GET:
            return result ? new GetResult() : new GetArgument();
        case CMD_SET:
            // FIXME: is it right that it is used to send data to the monitor?
        case CMD_CONFIRMED_SET:
            return result ? new SetResult() : new SetArgument();
        case CMD_CONFIRMED_ACTION:
            return result ? new ActionResult() : new ActionArgument();
        default:
            throw new IllegalArgumentException("Unknown command type:" + commandType);
        }
    }
}

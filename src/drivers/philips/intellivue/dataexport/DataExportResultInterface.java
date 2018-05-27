package drivers.philips.intellivue.dataexport;

public interface DataExportResultInterface extends DataExportMessage {
    CommandType getCommandType();

    void setCommandType(CommandType commandType);

    DataExportCommand getCommand();

    void setCommand(DataExportCommand dec);

    void parseMore(java.nio.ByteBuffer bb);
}

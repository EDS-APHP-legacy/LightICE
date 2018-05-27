package common;

import utils.NetworkAddress;

public class DeviceIdentity {

    // Information gathered from the configuration
    private final String site;
    private final String service;
    private final String sector;
    private final String room;
    private final String alias;
    private final NetworkAddress addr;
    private final String driver;

    // Information gathered by contacting the device
    private String uniqueDeviceIdentifier; /* maximum length = (64) */
    private String manufacturer;             /* maximum length = (128) */
    private String model;                    /* maximum length = (128) */
    private String serialNumber;            /* maximum length = (128) */
    private String partNumber;              /* maximum length = (128) */


    private static final int UDI_LENGTH = 36;
    private static final char[] UDI_CHARS = new char[26 * 2 + 10];
    static {
        int x = 0;
        for (char i = 'A'; i <= 'Z'; i++)
            UDI_CHARS[x++] = i;
        for (char i = 'a'; i <= 'z'; i++)
            UDI_CHARS[x++] = i;
        for (char i = '0'; i <= '9'; i++)
            UDI_CHARS[x++] = i;
    }

    public DeviceIdentity(String site, String service, String sector, String room, String alias, NetworkAddress addr, String driver) {
        this.site = site;
        this.service = service;
        this.sector = sector;
        this.room = room;
        this.alias = alias;
        this.addr = addr;
        this.driver = driver;
        this.uniqueDeviceIdentifier = randomUDI();
    }

    public static DeviceIdentity fromOther(DeviceIdentity other) {
        DeviceIdentity di = new DeviceIdentity(other.site, other.service, other.sector, other.room, other.alias, other.addr, other.driver);
        di.manufacturer = other.manufacturer;
        di.model = other.model;
        di.uniqueDeviceIdentifier = other.uniqueDeviceIdentifier;
        di.operatingSystem = other.operatingSystem;
        di.serialNumber = other.serialNumber;
        di.partNumber = other.partNumber;
        return di;
    }

    public static String randomUDI() {
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random(System.currentTimeMillis());
        for (int i = 0; i < UDI_LENGTH; i++) {
            sb.append(UDI_CHARS[random.nextInt(UDI_CHARS.length)]);
        }
        return sb.toString();
    }

    public void writeDI() {
        System.out.println("Write Device identity");
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public void setUniqueDeviceIdentifier(String uniqueDeviceIdentifier) {
        this.uniqueDeviceIdentifier = uniqueDeviceIdentifier;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    private String operatingSystem = ""; /* maximum length = (128) */

    public String getUniqueDeviceIdentifier() {
        return uniqueDeviceIdentifier;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getSite() {
        return site;
    }

    public String getService() {
        return service;
    }

    public String getSector() {
        return sector;
    }

    public String getRoom() {
        return room;
    }

    public String getAlias() {
        return alias;
    }

    public NetworkAddress getAddr() {
        return addr;
    }

    public String getAddrString() {
        if (this.addr.isSerial)
            return this.addr.getSerialAddr();
        else {
            return this.addr.getTCPAddr().getKey() + ":" + this.addr.getTCPAddr().getValue();
        }
    }

    public String getDriver() {
        return driver;
    }
}

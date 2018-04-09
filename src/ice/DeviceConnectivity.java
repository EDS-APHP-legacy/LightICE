package ice;

import fakedds.Copyable;

import java.io.Serializable;

public class DeviceConnectivity implements Copyable, Serializable
{

    public String unique_device_identifier = ""; /* maximum length = (64) */
    public ConnectionState state = (ConnectionState) ConnectionState.create();
    public ConnectionType type = (ConnectionType) ConnectionType.create();
    public String info = ""; /* maximum length = (128) */
    public ValidTargets valid_targets = (ValidTargets) ValidTargets.create();


    public DeviceConnectivity() {

    }


    public DeviceConnectivity(DeviceConnectivity other) {
        this();
        copy_from(other);
    }



    public static Object create() {
        DeviceConnectivity self;
        self = new DeviceConnectivity();
        self.clear();
        return self;
    }

    public void clear() {
        unique_device_identifier = "";
        state = ConnectionState.create();
        type = ConnectionType.create();
        info = "";
        if (valid_targets != null)
            valid_targets.clear();

    }

    public boolean equals(Object o) {
        if (o == null)
            return false;

        if(getClass() != o.getClass())
            return false;

        DeviceConnectivity otherObj = (DeviceConnectivity)o;

        if(!unique_device_identifier.equals(otherObj.unique_device_identifier))
            return false;

        if(!state.equals(otherObj.state))
            return false;

        if(!type.equals(otherObj.type))
            return false;

        if(!info.equals(otherObj.info))
            return false;

        if(!valid_targets.equals(otherObj.valid_targets))
            return false;

        return true;
    }

    public int hashCode() {
        int __result = 0;

        __result += unique_device_identifier.hashCode();
        __result += state.hashCode();
        __result += type.hashCode();
        __result += info.hashCode();
        __result += valid_targets.hashCode();

        return __result;
    }


    public Object copy_from(Object src) {
        DeviceConnectivity typedSrc = (DeviceConnectivity) src;
        DeviceConnectivity typedDst = this;

        typedDst.unique_device_identifier = typedSrc.unique_device_identifier;
        typedDst.state = (ConnectionState) typedDst.state.copy_from(typedSrc.state);
        typedDst.type = (ConnectionType) typedDst.type.copy_from(typedSrc.type);
        typedDst.info = typedSrc.info;
        typedDst.valid_targets = (ValidTargets) typedDst.valid_targets.copy_from(typedSrc.valid_targets);

        return this;
    }
}


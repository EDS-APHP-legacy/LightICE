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
package drivers.philips.intellivue.dataexport.error;

import java.util.Map;

import drivers.philips.intellivue.OrdinalEnum;

/**
 * @author Jeff Plourde
 *
 */
public enum     RemoteError implements OrdinalEnum.IntType {
    NoSuchObjectClass(0),
    NoSuchObjectInstance(1),
    AccessDenied(2),
    GetListError(7),
    SetListError(8),
    NoSuchAction(9),
    ProcessingFailure(10),
    InvalidArgumentValue(15),
    InvalidScope(16),
    InvalidObjectInstance(17);

    private final int x;

    private RemoteError(final int x) {
        this.x = x;
    }

    private static final Map<Integer, RemoteError> map = OrdinalEnum.buildInt(RemoteError.class);

    public static final RemoteError valueOf(int x) {
        return map.get(x);
    }

    public final int asInt() {
        return x;
    }

    public final String toString() {
        switch (this) {
            case NoSuchObjectClass:
                return "There is no such object class in the system. An OIDType with the class ID is appended to the message.";
            case NoSuchObjectInstance:
                return"The object instance does not exist. The ManagedObjectId of the instance is appended.";
            case AccessDenied:
                return"Computer Client has not required privileges to perform the operation. No data is appended.";
            case GetListError:
                return"Get operation failed. A GetListError is appended to the message.";
            case SetListError:
                return"Set operation failed. A SetListError is appended to the message.";
            case NoSuchAction:
                return"Unknown action type. The object class ID and action type are appended to the message.";
            case ProcessingFailure:
                return"Generic error indicating an invalid request. A ProcessingFailure is appended to the message.";
            case InvalidArgumentValue:
                return"The argument of the ROSE message was not valid. An Action result is appended.";
            case InvalidScope:
                return"The scope is not valid for the operation. The value of the scope is appended.";
            case InvalidObjectInstance:
                return"Wrong object instance. The ManagedObjectId of the instance is appended.";
        }
        return "";
    }
}

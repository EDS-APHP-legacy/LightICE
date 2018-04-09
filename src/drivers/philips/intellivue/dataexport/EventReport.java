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
package drivers.philips.intellivue.dataexport;

import java.nio.ByteBuffer;

import common.io.util.Bits;
import drivers.philips.intellivue.Formatable;
import drivers.philips.intellivue.Parseable;
import drivers.philips.intellivue.data.ManagedObjectIdentifier;
import drivers.philips.intellivue.data.OIDType;
import drivers.philips.intellivue.data.RelativeTime;

/**
 * @author Jeff Plourde
 *
 */
public class EventReport implements Parseable, Formatable {
    private final ManagedObjectIdentifier identifier = new ManagedObjectIdentifier();
    private final RelativeTime time = new RelativeTime();
    private OIDType oid = OIDType.lookup(0);
    private int length;

    @Override
    public void parse(ByteBuffer bb) {
        identifier.parse(bb);
        time.parse(bb);
        oid = OIDType.lookup(Bits.getUnsignedShort(bb));
        length = Bits.getUnsignedShort(bb);
    }

    @Override
    public void format(ByteBuffer bb) {
        identifier.format(bb);
        time.format(bb);
        oid.format(bb);
        Bits.putUnsignedShort(bb, length);
    }

    @Override
    public String toString() {
        return "[identifier=" + identifier + ",time=" + time + ",oid=" + oid + ",length=" + length + "]";
    }
}

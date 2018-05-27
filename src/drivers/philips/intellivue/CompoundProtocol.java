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

import java.nio.ByteBuffer;

import common.io.util.HexUtil;
import drivers.philips.intellivue.association.AssociationMessage;
import drivers.philips.intellivue.association.AssociationProtocol;
import drivers.philips.intellivue.association.AssociationProtocolImpl;
import drivers.philips.intellivue.connectindication.ConnectIndication;
import drivers.philips.intellivue.connectindication.ConnectIndicationProtocol;
import drivers.philips.intellivue.connectindication.ConnectIndicationProtocolImpl;
import drivers.philips.intellivue.dataexport.DataExportMessage;
import drivers.philips.intellivue.dataexport.DataExportProtocol;
import drivers.philips.intellivue.dataexport.impl.DataExportProtocolImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeff Plourde
 *
 */
public class CompoundProtocol implements Protocol {
    private final static Logger log = LoggerFactory.getLogger(CompoundProtocol.class);
    private final DataExportProtocol dataExportProtocol = new DataExportProtocolImpl();
    private final ConnectIndicationProtocol connectIndicationProtocol = new ConnectIndicationProtocolImpl();
    private final AssociationProtocol associationProtocol = new AssociationProtocolImpl();

    @Override
    public Message parse(ByteBuffer bb) {
        try {
            if (bb.hasRemaining()) {
                int firstByte = 0xFF & bb.get(0);

                switch (firstByte) {
                case 0xE1:
                    return dataExportProtocol.parse(bb);
                case 0x00:
                    return connectIndicationProtocol.parse(bb);
                default:
                    return associationProtocol.parse(bb);
                }
            } else {
                return null;
            }
        } catch (RuntimeException re) {
            bb.position(0);
            log.trace("Offending buffer:\n" + HexUtil.dump(bb, 20));
            log.error("Error parsing message", re);
            return null;
        }
    }

    @Override
    public void format(Message message, ByteBuffer bb) {
        if (message instanceof DataExportMessage) {
            dataExportProtocol.format((DataExportMessage) message, bb);
        } else if (message instanceof AssociationMessage) {
            associationProtocol.format((AssociationMessage) message, bb);
        } else if (message instanceof ConnectIndication) {
            connectIndicationProtocol.format((ConnectIndication) message, bb);
        } else {
            log.warn("Message is an instance of an unknown class (" + message.getClass() + "), cannot format.");
        }
    }

}

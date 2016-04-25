package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class DeleteMessageArchiveIQ extends IQ {

    public DeleteMessageArchiveIQ() {
       //super("list", "urn:xmpp:archive");
        super("remove", "urn:xmpp:archive");
    }


    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute("with", "leki@198.154.106.139");
        xml.rightAngleBracket();

        return xml;
    }

}




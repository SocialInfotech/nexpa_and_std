package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by ldonios on 3/25/2016.
 */
public class MessageArchiveWithIQ extends IQ {

    private String with;

    public MessageArchiveWithIQ(String with) {

        super("query", "urn:xmpp:mam:0");
        this.with = with;
    }

//    public MessageArchiveWithIQ() {
//
//        super("query", "urn:xmpp:mam:0");
//
//    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();

        if (with != null && !with.isEmpty()) {

            xml.append("<x xmlns='jabber:x:data' type='submit'>");
            xml.append("<field var='FORM_TYPE' type='hidden'>");
            xml.append("<value>urn:xmpp:mam:0</value></field>");
            xml.append("<field var='with'>");
            xml.append("<value>" + with + "</value>");
            xml.append("</field></x>");
        }
        xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        xml.append("<max>30</max><before/>");
        xml.append("</set>");


        return xml;
    }


}

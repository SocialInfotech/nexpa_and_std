package com.lpoezy.nexpa.objects;


import org.jivesoftware.smack.packet.IQ;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class CollectionIQ extends IQ {


    private final String with;
    private final String start;

    public CollectionIQ(String with, String start) {
        super("retrieve", "urn:xmpp:archive");

        this.with = with;
        this.start = start;

    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.attribute("with", with);
        xml.attribute("start", start);
        xml.rightAngleBracket();
        xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        xml.append("<max>100</max></set>");
        return xml;
    }

    public interface OnRetrieveListener {

        public void onRetrieve(CollectionIQ collection);
    }
}

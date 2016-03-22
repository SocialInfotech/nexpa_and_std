package com.lpoezy.nexpa.objects;


import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class CollectionIQ extends IQ {


    private  String with;
    private  String start;

    public List<ChatMessage> chtMessages = new ArrayList<ChatMessage>();
    public int last;
    public int count;

    public CollectionIQ(String with, String start) {
        super("retrieve", "urn:xmpp:archive");

        this.with = with;
        this.start = start;

    }

    public CollectionIQ() {
        super("retrieve", "urn:xmpp:archive");
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

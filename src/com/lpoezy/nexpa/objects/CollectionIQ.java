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

    /*
    <iq type='get' id='page1'>
    <retrieve xmlns='urn:xmpp:archive'
    with='juliet@capulet.com/chamber'
    start='1469-07-21T02:56:15Z'>
    <set xmlns='http://jabber.org/protocol/rsm'>
    <max>100</max>
    </set>
    </retrieve>
    </iq>

    //*/


    //<iq id='Ef3QB-14' type='get'>
    // <retrieve xmlns='urn:xmpp:archive'
    // with='momo@198.154.106.139'
    // start='2016-03-23T00:03:36.289Z'>
    // <set xmlns='http://jabber.org/protocol/rsm'>
    // <max>100</max></set>
    // </retrieve>
    // </iq>

    //<iq type="result" id="9g0UI-39" to="momo@198.154.106.139/Smack">
    // <chat xmlns="urn:xmpp:archive" with="leki@198.154.106.139" start="2016-03-22T23:40:48.063Z">
    // <to secs="0">
    // <body>{"body":"hi","senderName":"momo","msgid":"484-37","receiver":"leki","sender":"momo","isMine":true}</body>
    // </to><to secs="15">
    // <body>{"body":"how are you?","senderName":"momo","msgid":"240-27","receiver":"leki","sender":"momo","isMine":true}</body>
    // </to><set xmlns="http://jabber.org/protocol/rsm"
    // ><first index="0">0</first><last>1</last><count>2</count></set></chat></iq>



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

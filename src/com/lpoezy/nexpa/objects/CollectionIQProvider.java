package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class CollectionIQProvider extends IQProvider<CollectionIQ> {

    //<iq type="result" id="mGMsD-13" to="momo@198.154.106.139/Smack">
    // <chat xmlns="urn:xmpp:archive" with="leki@198.154.106.139" start="2016-03-18T09:10:12.311Z">
    // <to secs="0">
    // <body>{"body":"hi","senderName":"momo","msgid":"44-48","receiver":"leki","sender":"momo","isMine":true}</body>
    // </to>
    // <set xmlns="http://jabber.org/protocol/rsm">
    // <first index="0">0</first><last>0</last><count>1</count></set></chat></iq>

    @Override
    public CollectionIQ parse(XmlPullParser xmlPullParser, int i) throws XmlPullParserException, IOException, SmackException {
        return null;
    }
}

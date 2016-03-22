package com.lpoezy.nexpa.objects;

import com.google.gson.Gson;
import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class CollectionIQProvider extends IQProvider<CollectionIQ> {
    private final CollectionIQ.OnRetrieveListener callback;

    public CollectionIQProvider(CollectionIQ.OnRetrieveListener callback) {
            this.callback = callback;
    }

    //<iq type="result" id="XmiWt-13" to="momo@198.154.106.139/Smack">
    // <chat xmlns="urn:xmpp:archive" with="leki@198.154.106.139" start="2016-03-20T01:53:40.760Z">
    // <to secs="0">
    // <body>{"body":"hi\n","msgid":"331-73","receiver":"leki","sender":"momo","senderName":"momo","isMine":true}</body>
    // </to><to secs="5">
    // <body>{"body":"hello","msgid":"699-67","receiver":"leki","sender":"momo","senderName":"momo","isMine":true}</body>
    // </to><to secs="14">
    // <body>{"body":"how are you?","msgid":"370-12","receiver":"leki","sender":"momo","senderName":"momo","isMine":true}</body>
    // </to><to secs="29"><body>{"body":"I\u0027m fine, Thank you.","msgid":"638-30","receiver":"leki","sender":"momo","senderName":"momo","isMine":true}</body>
    // </to><set xmlns="http://jabber.org/protocol/rsm">
    // <first index="0">0</first>
    // <last>3</last>
    // <count>4</count></set></chat></iq>

    @Override
    public CollectionIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {

        CollectionIQ collectionIq = new CollectionIQ();
        int to = 0;
        String body = "";
        int last = 0;
        int count = 0;

        Gson gson = new Gson();


        outerloop:
        while (true) {
            int eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String elementName = parser.getName();
                    //L.debug("elementName: " + elementName);


                    switch (parser.getName()) {

                        case "to":
                            // Initialize the variables from the parsed XML
                            to = Integer.valueOf(parser.getAttributeValue("", "secs"));
                            //L.debug("to: " + to);
                            break;


                        case "body":
                            body = parser.nextText();
                            L.debug("body: " + body);

                            ChatMessage chatMsg = gson.fromJson(body, ChatMessage.class);
                            collectionIq.chtMessages.add(chatMsg);
                            break;

                        case "last":
                            //L.debug("last: " + last);
                            collectionIq.last = Integer.valueOf(parser.nextText());
                            break;

                        case "count":
                            collectionIq.count = Integer.valueOf(parser.nextText());
                            break;

                    }


                    break;


                case XmlPullParser.END_TAG:
                    //L.debug("elementName: " + parser.getName());
                    switch (parser.getName()) {

                        case "chat":
                            if (parser.getDepth() == initialDepth) {
                                break outerloop;
                            }
                            break;
                    }
            }
        }

        if(callback!=null)callback.onRetrieve(collectionIq);

        return collectionIq;
    }
}

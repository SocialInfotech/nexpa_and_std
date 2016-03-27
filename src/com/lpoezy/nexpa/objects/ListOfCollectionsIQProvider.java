package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class ListOfCollectionsIQProvider extends IQProvider<ListOfCollectionsIQ> {

    private final ListOfCollectionsIQ.OnRetrieveListener callback;

    public ListOfCollectionsIQProvider(ListOfCollectionsIQ.OnRetrieveListener callback) {
        this.callback = callback;

    }


    //<message to="kato@198.154.106.139/Smack">
    // <result xmlns="urn:xmpp:mam:0" id="130">
    // <forwarded xmlns="urn:xmpp:forward:0">
    // <delay xmlns="urn:xmpp:delay" stamp="2016-03-23T23:38:38.806Z"/>
    // <message xmlns="" to="kato@198.154.106.139" id="747-19" type="chat" from="roy@198.154.106.139/Smack">
    // <body>{"body":"test","senderName":"roy","msgid":"747-19","receiver":"kato","sender":"roy","isMine":true}</body>
    // <thread>2ecbe32c-4f8d-4287-a09d-e71a4c9187e7</thread>
    // </message>
    // </forwarded>
    // </result>
    // </message>

    //<message to="kato@198.154.106.139/Smack">
    // <result xmlns="urn:xmpp:mam:0" id="133">
    // <forwarded xmlns="urn:xmpp:forward:0">
    // <delay xmlns="urn:xmpp:delay" stamp="2016-03-24T00:03:52.252Z"/>
    // <message xmlns="" to="roy@198.154.106.139" id="410-32" type="chat" from="kato@198.154.106.139/Smack">
    // <body>{"body":"hi roy","msgid":"410-32","receiver":"roy","sender":"kato","senderName":"kato","isMine":true}</body>
    // <thread>87abf6d3-93e7-48f2-a4be-06f00296b301</thread>
    // </message>
    // </forwarded>
    // </result>
    // </message>

    //<message to="kato@198.154.106.139/Smack">
    // <fin xmlns="urn:xmpp:mam:0" complete="true">
    // <set xmlns="http://jabber.org/protocol/rsm">
    // <first index="0">130</first>
    // <last>145</last>
    // <count>16</count>
    // /set>
    // </fin>
    // </message>


    @Override
    public ListOfCollectionsIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {

        ListOfCollectionsIQ listOfCollectionsIQ = new ListOfCollectionsIQ();

        String stamp = "";
        String to = "";
        String type = "";
        String from = "";
        String body = "";
        String thread = "";
        String first = "";
        String last = "";
        String count = "";

        outerloop:
        while (true) {
            int eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String elementName = parser.getName();
                    //L.debug("elementName: " + elementName);
                    switch (parser.getName()) {
                        case "delay":

                            stamp = parser.getAttributeValue("", "stamp");
                            //L.debug("stamp: "+stamp);
                            break;

                        case "message":
                            to = parser.getAttributeValue("", "to");
                            type = parser.getAttributeValue("", "type");
                            from = parser.getAttributeValue("", "from");
                            //L.debug("to: "+to);
                           // L.debug("type: "+type);
                           // L.debug("from: "+from);
                            break;

                        case "body":
                            body = parser.nextText();
                            //L.debug("body: "+body);
                            break;

                        case "thread":
                            thread = parser.nextText();
                            //L.debug("thread: "+thread);
                            break;

                        case "first":
                            first = parser.nextText();
                            //L.debug("first: "+first);
                            break;

                        case "last":
                            last = parser.nextText();
                            //L.debug("last: "+last);
                            break;

                        case "count":
                            count = parser.nextText();
                            //L.debug("count: "+count);
                            break;

                    }
                    break;


                case XmlPullParser.END_TAG:

                    switch (parser.getName()) {
//                        case "chat":
//
//                            ListOfCollectionsIQ.Chat chat = new ListOfCollectionsIQ.Chat();
//                            chat.setWith(with);
//                            chat.setStart(start);
//                            listOfCollectionsIQ.addChat(chat);
//
//                            break;

                        case "delay":

                            break;

                        case "message":

                            break;

                        case "body":

                            break;

                        case "thread":

                            break;

                        case "first":

                            break;

                        case "last":
                            break;

                        case "count":
                            break;

                        case "result":
                        //case "list":
                            if (parser.getDepth() == initialDepth) {
                                break outerloop;
                            }
                            break;
                    }
            }
        }

        if (callback != null) callback.onRetrieve(listOfCollectionsIQ);

        return listOfCollectionsIQ;
    }


}

package com.lpoezy.nexpa.objects;

import com.lpoezy.nexpa.utility.L;

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

    //<iq type="result" id="Rl8dP-61" to="leki@198.154.106.139/Smack">
    // <list xmlns="urn:xmpp:archive"><chat with="momo@198.154.106.139" start="2016-03-22T01:47:25.091Z"/>
    // <chat with="momo@198.154.106.139" start="2016-03-22T01:47:25.098Z"/>
    // <chat with="momo@198.154.106.139" start="2016-03-22T01:47:25.110Z"/>
    // <chat with="momo@198.154.106.139" start="2016-03-22T01:47:25.117Z"/>
    // <set xmlns="http://jabber.org/protocol/rsm"><first index="0">9</first><last>12</last><count>4</count></set></list></iq>


    //<iq type="result" id="Wy20C-11" to="momo@198.154.106.139/Smack">
    // <list xmlns="urn:xmpp:archive">
    // <chat with="leki@198.154.106.139" start="2016-03-18T09:10:12.311Z"/>
    // <chat with="leki@198.154.106.139" start="2016-03-20T01:53:40.760Z"/>
    // <chat with="leki@198.154.106.139" start="2016-03-20T04:51:43.093Z"/>
    // <chat with="roy@198.154.106.139" start="2016-03-20T06:20:16.982Z"/>
    // <chat with="leki@198.154.106.139" start="2016-03-21T01:35:36.245Z"/>
    // <chat with="roy@198.154.106.139" start="2016-03-21T02:42:48.970Z"/>
    // <chat with="null@198.154.106.139" start="2016-03-21T22:44:46.934Z"/>
    // <set xmlns="http://jabber.org/protocol/rsm">
    // <first index="0">1</first><last>7</last><count>7</count></set></list></iq>


    @Override
    public ListOfCollectionsIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {

        ListOfCollectionsIQ listOfCollectionsIQ = new ListOfCollectionsIQ();

        String with = "";
        String start = "";
        String last = "";
        String first = "";
        String count = "";

        outerloop:
        while (true) {
            int eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String elementName = parser.getName();
                    L.debug("elementName: " + elementName);
//                    switch (parser.getName()) {
//                        case "chat":
//
//
//                            // Initialize the variables from the parsed XML
//
//                            with = parser.getAttributeValue("", "with");
//                            //L.debug("with: "+with);
//                            start = parser.getAttributeValue("", "start");
//
//                            break;
//
//                    }
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

                        case "query":
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

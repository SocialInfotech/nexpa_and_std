package com.lpoezy.nexpa.objects;


import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Created by HP PAVILION on 3/24/2016.
 */
public class MAMExtensionProvider extends DataPacketProvider.PacketExtensionProvider {

    private MessageResultElement.OnParseCompleteListener mCallback;


    public MAMExtensionProvider(MessageResultElement.OnParseCompleteListener callback) {

        mCallback = callback;
    }

//</message>
// <message to="kato@198.154.106.139/Smack">
// <fin xmlns="urn:xmpp:mam:0" complete="true">
// <set xmlns="http://jabber.org/protocol/rsm"><first index="0">130</first><last>155</last><count>26</count></set></fin></message><r xmlns='urn:xmpp:sm:3' />

    @Override
    public DataPacketExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
     //L.debug("0000000000000000000000000000000000000000000000000000000000000000000000000");
        String stamp = "";
        String to = "";
        String type = "";
        String from = "";
        String body = "";
        String thread = "";
        String complete = "";
        int first = 0;
        int last = 0;
        int count = 0;

        outerloop:
        while (true) {
            int eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String elementName = parser.getName();

                    //L.debug("elementName: " + elementName);
                    switch (parser.getName()) {

                        case "forwarded":

                            break;



                        case "delay":
                            stamp = parser.getAttributeValue("", "stamp");
                            //L.debug("stamp: " + stamp);
                            break;

                        case "message":

                            to = parser.getAttributeValue("", "to");
                            type = parser.getAttributeValue("", "type");
                            from = parser.getAttributeValue("", "from");

                            //L.debug("to: "+to+", type: "+type+", from: "+from);

                            break;

                        case "body":
                            body = parser.nextText();
                            L.debug("body: "+body);
                            break;

                        case "thread":
                            thread = parser.nextText();
                            // L.debug("thread: "+thread);
                            break;
                    }
                    break;
                case XmlPullParser.END_TAG:

                    // Abort condition: if the are on a end tag (closing element) of the same depth
                    if (parser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;
            }
        }

        MessageResultElement msg = new MessageResultElement(
                stamp, to, type,
                from, body, thread
        );
        if(mCallback!=null)mCallback.onParseComplete(msg);

       // L.debug("0000000000000000000000000000000000000000000000000000000000000000000000000");
        return null;
    }
}

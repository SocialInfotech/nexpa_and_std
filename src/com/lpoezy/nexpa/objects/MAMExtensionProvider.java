package com.lpoezy.nexpa.objects;


import com.lpoezy.nexpa.utility.L;

import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by HP PAVILION on 3/24/2016.
 */
public class MAMExtensionProvider extends DataPacketProvider.PacketExtensionProvider {

    private MessageElement.OnParseCompleteListener mCallback;


    public MAMExtensionProvider(MessageElement.OnParseCompleteListener callback) {

        mCallback = callback;
    }

//    public void addOnParseListener(MessageElement.OnParseCompleteListener callback) {
//
//        mCallback = callback;
//
//    }


    @Override
    public DataPacketExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {

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

        boolean isAdded = false;

        List<MessageElement> msgs = new ArrayList<MessageElement>();

        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {

            switch (parser.getEventType()) {

                case XmlPullParser.START_TAG:

                    String elementName = parser.getName();
                    //L.debug("elementName: " + elementName);

                    switch (parser.getName()) {

                        case "result":

                            break;

                        case "fin":
                            complete = parser.getAttributeValue("", "complete");
                           // L.debug("complete: " + complete);
                            break;

                        case "delay":
                            stamp = parser.getAttributeValue("", "stamp");
                            //L.debug("stamp: " + stamp);
                            break;

                        case "message":

                            isAdded = false;
                            to = parser.getAttributeValue("", "to");
                            type = parser.getAttributeValue("", "type");
                            from = parser.getAttributeValue("", "from");

                             //L.debug("to: "+to+", type: "+type+", from: "+from);

                            break;

                        case "body":
                            body = parser.nextText();
                            //L.debug("body: "+body);
                            break;

                        case "thread":
                            thread = parser.nextText();
                            // L.debug("thread: "+thread);
                            break;

                        case "first":
                            first = Integer.valueOf(parser.nextText());
                            //L.debug("first: "+first);
                            break;

                        case "last":
                            last = Integer.valueOf(parser.nextText());
                            //L.debug("last: "+last);
                            break;

                        case "count":
                            count = Integer.valueOf(parser.nextText());
                            // L.debug("count: "+count);
                            break;

                    }

                    break;

                case XmlPullParser.END_TAG:

                    switch (parser.getName()) {

                        case "fin":
                           // L.debug("mCallback: "+mCallback);
                            if(mCallback!=null){
                                mCallback.onParseComplete(msgs, first, last, count);
                                msgs.clear();
                            }
                            break;

                        case "message":




                            if(type!=null && !isAdded){

                                MessageElement msg = new MessageElement(
                                        stamp, to, type,
                                        from, body, thread
                                );

                                msgs.add(msg);
                                isAdded = true;
                                //L.debug(msgs.size()+", to: " + to + ", type: " + type + ", from: " + from);
                            }


                            break;

                    }
                    break;

                default:
                    break;
            }
            parser.next();
        }


        return null;
    }
}

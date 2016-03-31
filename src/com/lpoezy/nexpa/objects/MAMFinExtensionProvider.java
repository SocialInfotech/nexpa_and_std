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
public class MAMFinExtensionProvider extends DataPacketProvider.PacketExtensionProvider {

    private OnParseCompleteListener mCallback;


    public MAMFinExtensionProvider(OnParseCompleteListener callback) {

        mCallback = callback;
    }

//</message>
// <message to="kato@198.154.106.139/Smack">
// <fin xmlns="urn:xmpp:mam:0" complete="true">
// <set xmlns="http://jabber.org/protocol/rsm"><first index="0">130</first><last>155</last><count>26</count></set></fin></message><r xmlns='urn:xmpp:sm:3' />

    @Override
    public DataPacketExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
     //L.debug("0000000000000000000000000000000000000000000000000000000000000000000000000");

        int first = 0;
        int last = 0;
        int count = 0;

        outerloop:
        while (true) {
            int eventType = parser.next();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String elementName = parser.getName();

                    L.debug("elementName: " + elementName);
                    switch (parser.getName()) {

                        case "first":
                            first = Integer.valueOf(parser.nextText());
                            break;

                        case "last":
                            last = Integer.valueOf(parser.nextText());
                            break;

                        case "count":
                            count = Integer.valueOf(parser.nextText());
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


        if(mCallback!=null)mCallback.onParseComplete(first, last, count);

        //L.debug("0000000000000000000000000000000000000000000000000000000000000000000000000");
        return null;
    }


    public interface OnParseCompleteListener {

        public void onParseComplete(final int first, final int last, final int count);

    }
}

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
public class TestExtensionProvider extends DataPacketProvider.PacketExtensionProvider {

    @Override
    public DataPacketExtension parse(XmlPullParser xmlPullParser, int initialDepth) throws XmlPullParserException, IOException {

        while (xmlPullParser.getEventType() != XmlPullParser.END_DOCUMENT) {
            switch (xmlPullParser.getEventType()) {
                case XmlPullParser.START_TAG:
                    L.debug("cccccccccccccc:" + xmlPullParser.getName());

                    break;
                case XmlPullParser.END_TAG:

                    break;

                default:
                    break;
            }
            xmlPullParser.next();
        }


        return null;
    }
}

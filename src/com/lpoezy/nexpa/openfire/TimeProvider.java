package com.lpoezy.nexpa.openfire;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by ldonios on 3/18/2016.
 */
public class TimeProvider extends IQProvider<Time> {

    @Override
    public Time parse(XmlPullParser parser, int i) throws XmlPullParserException, IOException, SmackException {



        return new Time();
    }
}

package com.lpoezy.nexpa.openfire;

import org.jivesoftware.smack.packet.IQ;

public class Time extends IQ {

    public Time() {
        super("auto", "urn:xmpp:archive");
        //super("database");


    }
    /*/
	<iq id='e8Bt1-9' type='get'>
	<pref xmlns='urn:xmpp:archive'>
	</pref></iq>
	
	//*/

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
            IQChildElementXmlStringBuilder xml) {
        xml.attribute("save", "false");

        xml.rightAngleBracket();

        //xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        //xml.append("<max>30</max></set>");

        return xml;
    }
    //rn2dw-9
}

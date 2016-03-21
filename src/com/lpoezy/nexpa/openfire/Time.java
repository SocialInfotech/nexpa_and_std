package com.lpoezy.nexpa.openfire;

import org.jivesoftware.smack.packet.IQ;

public class Time extends IQ {

    public Time() {
        super("list ", "urn:xmpp:archive");
        //super("database");


    }
    /*/
	<iq id='bnpmh-8' type='get'>
	<database name='openfire' resource='http://openaether.org/projects/jabber_database.html'>
	</database></iq>
	
	//*/

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
            IQChildElementXmlStringBuilder xml) {
        xml.attribute("with", "leki@198.154.106.139");

        xml.rightAngleBracket();

        xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        xml.append("<max>30</max></set>");

        return xml;
    }
    //rn2dw-9
}

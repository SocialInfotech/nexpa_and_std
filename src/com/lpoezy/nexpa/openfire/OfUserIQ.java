package com.lpoezy.nexpa.openfire;

import org.jivesoftware.smack.packet.IQ;




public class OfUserIQ extends IQ{

	public OfUserIQ() {
		//super(childElementName, childElementNamespace);
		super("req", "urn:xmpp:http");
		
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
			IQChildElementXmlStringBuilder xml) {
		xml.attribute("method","GET");
		xml.attribute("resource","/plugins/restapi/v1/users/momo");
		xml.attribute("version","1.1");
        xml.rightAngleBracket();
       
        return xml;
	}

	
	
	

	
	
}

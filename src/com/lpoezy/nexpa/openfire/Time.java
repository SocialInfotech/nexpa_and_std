package com.lpoezy.nexpa.openfire;

import org.jivesoftware.smack.packet.IQ;

public class Time extends IQ{

	public Time() {
		//super("query ", "jabber:iq:search");
		super("database");
		
		
	}
	/*/
	<iq id='bnpmh-8' type='get'>
	<database name='openfire' resource='http://openaether.org/projects/jabber_database.html'>
	</database></iq>
	
	//*/
	
	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(
			IQChildElementXmlStringBuilder xml) {
		xml.attribute("name","openfire");
		xml.attribute("xmlns","http://openaether.org/projects/jabber_database.html");
//		xml.attribute("version","1.1");
        xml.rightAngleBracket();
//        
       // xml.append("<first>momo</first>");
        
		return xml;
	}

}

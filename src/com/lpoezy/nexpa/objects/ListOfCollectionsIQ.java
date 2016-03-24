package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP PAVILION on 3/21/2016.
 */
public class ListOfCollectionsIQ extends IQ {

    public List<Chat> chats = new ArrayList<Chat>();
    public int last;
    public int first;
    public Integer count;

    public ListOfCollectionsIQ() {
       //super("list", "urn:xmpp:archive");
        super("query", "urn:xmpp:mam:0");
    }


//    <iq type="result" id="W0Son-17" to="kato@198.154.106.139/Smack">
//    <query xmlns="urn:xmpp:mam:0">
//    <x xmlns="jabber:x:data" type="form">
//    <field var="FORM_TYPE" type="hidden">
//    <value>urn:xmpp:mam:0</value>
//    </field>
//    <field var="with" type="jid-single"/>
//    <field var="start" type="text-single"/>
//    <field var="end" type="text-single"/>
//    </x>
//    </query>
//    </iq>


//http://xmpp.org/extensions/diff/api/xep/0313/diff/0.3/vs/0.4
    //http://stackoverflow.com/questions/29282498/smack-xep-313-implementation
    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        //xml.attribute("with", "roy@198.154.106.139");
        xml.rightAngleBracket();
        //xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
       // xml.append("<max>30</max></set>");
       // xml.append("<x xmlns='jabber:x:data' type='submit'>");
       // xml.append("<field var='FORM_TYPE' type='hidden'>");
       // xml.append("<value>urn:xmpp:mam:0</value></field>");
       // xml.append("<field var=\"with\" type=\"roy@198.154.106.139\"/>");
       // xml.append("</x>");
       // xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
       // xml.append("<max>30</max><before/>");
       // xml.append("</set>");
        return xml;
    }

    public void addChat(Chat chat) {
        chats.add(chat);
    }

    public static class Chat {

        private String with;
        private String start;


        public void setWith(String with) {
            this.with = with;
        }

        public void setStart(String start) {
            this.start = start;
        }


        public String getWith() {
            return with;
        }

        public String getStart() {
            return start;
        }
    }

    public interface OnRetrieveListener {

        public void onRetrieve(ListOfCollectionsIQ list);
    }
}




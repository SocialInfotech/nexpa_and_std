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
        super("list", "urn:xmpp:archive");
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {

        xml.rightAngleBracket();
        xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        xml.append("<max>30</max></set>");

        return xml;
    }

    public void addChat(Chat chat){
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




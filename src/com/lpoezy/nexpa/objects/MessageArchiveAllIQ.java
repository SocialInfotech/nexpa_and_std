package com.lpoezy.nexpa.objects;

import org.jivesoftware.smack.packet.IQ;

/**
 * Created by ldonios on 3/25/2016.
 */
public class MessageArchiveAllIQ extends IQ {

    public MessageArchiveAllIQ() {
        super("query", "urn:xmpp:mam:0");
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();

        //xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        // xml.append("<max>30</max></set>");
        // xml.append("<x xmlns='jabber:x:data' type='submit'>");
        // xml.append("<field var='FORM_TYPE' type='hidden'>");
        // xml.append("<value>urn:xmpp:mam:0</value></field>");
        // xml.append("<field var=\"with\" type=\"roy@198.154.106.139\"/>");
        // xml.append("</x>");

        xml.append("<set xmlns='http://jabber.org/protocol/rsm'>");
        xml.append("<max>100</max><before/>");
        xml.append("</set>");

        return xml;
    }

//    public static class Message{
//
//        private  String stamp;
//        private  String to;
//        private  String type;
//        private  String from;
//        private  String body;
//        private  String thread;
//
//        public Message(String stamp, String to, String type, String from, String body, String thread) {
//            this.stamp = stamp;
//            this.to = to;
//            this.type = type;
//            this.from = from;
//            this.body = body;
//            this.thread = thread;
//        }
//
//        public String getStamp() {
//            return stamp;
//        }
//
//        public String getTo() {
//            return to;
//        }
//
//        public String getType() {
//            return type;
//        }
//
//        public String getFrom() {
//            return from;
//        }
//
//        public String getBody() {
//            return body;
//        }
//
//        public String getThread() {
//            return thread;
//        }
//    }
//
//    public interface OnParseCompleteListener{
//
//        public void onParseComplete(List<Message> msgs, int first, int last, int count);
//
//    }



}

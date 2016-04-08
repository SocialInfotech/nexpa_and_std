package com.lpoezy.nexpa.objects;

/**
 * Created by ldonios on 3/28/2016.
 */
public class MessageResultElement {


    private String stamp;
    private String to;
    private String type;
    private String from;
    private String body;
    private String thread;

    public MessageResultElement(String stamp, String to, String type, String from, String body, String thread) {
        this.stamp = stamp;
        this.to = to;
        this.type = type;
        this.from = from;
        this.body = body;
        this.thread = thread;
    }

    public String getStamp() {
        return stamp;
    }

    public String getTo() {
        return to;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }

    public String getThread() {
        return thread;
    }


    public interface OnParseCompleteListener {

        public void onParseComplete(MessageResultElement msg);

    }

}

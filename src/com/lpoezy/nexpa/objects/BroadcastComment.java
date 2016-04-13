package com.lpoezy.nexpa.objects;

/**
 * Created by HP PAVILION on 4/12/2016.
 */
public class BroadcastComment {

    private  String body;
    private  boolean isMine;
    private String from;
    private String date;
    private String address;

    public BroadcastComment(String from, String body, String address, String date, boolean isMine){
        this.from = from;
        this.address = address;
        this.body = body;
        this.date = date;
        this.isMine = isMine;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

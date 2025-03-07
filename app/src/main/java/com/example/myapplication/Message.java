package com.example.myapplication;

import java.util.Date;

public class Message {
    private String text;
    private String senderId;
    private Date timestamp;

    public Message() { }

    public Message(String text, String senderId, Date timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}

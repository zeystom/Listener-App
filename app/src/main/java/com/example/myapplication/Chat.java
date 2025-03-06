package com.example.myapplication;

public class Chat {
    private  String uid;
    private  String pfp;
    private String name;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPfp() {
        return pfp;
    }

    public void setPfp(String pfp) {
        this.pfp = pfp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Chat(String uid, String pfp, String name, String lastMessage) {
        this.uid = uid;
        this.pfp = pfp;
        this.name = name;
        this.lastMessage = lastMessage;
    }

    private String lastMessage;


}
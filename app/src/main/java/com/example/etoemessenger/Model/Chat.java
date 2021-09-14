package com.example.etoemessenger.Model;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private String iv;

    public Chat(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.iv = iv;
    }

    public Chat() {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIv() { return iv; }

    public void setIv(String iv) { this.iv = iv; }
}

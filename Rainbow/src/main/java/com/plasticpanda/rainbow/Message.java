package com.plasticpanda.rainbow;

import java.util.Date;

public class Message {

    private String messageID;
    private String author;
    private Date date;
    private String message;
    private boolean isEncrypted;
    private boolean sending;


    public Message() {
        this.author = "";
        this.message = "";
    }

    public Message(String messageID, String author, String message, Date date) {
        this.messageID = messageID;
        this.author = author;
        this.message = message;
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
            "author='" + author + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
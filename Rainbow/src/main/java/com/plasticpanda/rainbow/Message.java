package com.plasticpanda.rainbow;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author Luca Casartelli
 */

@DatabaseTable(tableName = "messages")
public class Message {

    @DatabaseField(canBeNull = false)
    private String messageID;
    @DatabaseField(canBeNull = false)
    private String author;
    @DatabaseField(canBeNull = false)
    private Date date;
    @DatabaseField(canBeNull = false)
    private String message;

    private boolean isEncrypted;
    private boolean sending;


    public Message() {
        this.author = "";
        this.message = "";
    }

    /**
     * @param messageID   message id
     * @param author      message author
     * @param message     message body
     * @param date        message time
     * @param isEncrypted encryption
     */
    public Message(String messageID, String author, String message, Date date, boolean isEncrypted) {
        this.messageID = messageID;
        this.author = author;
        this.message = message;
        this.date = date;
        this.isEncrypted = this.isEncrypted();
    }

    /**
     * @param messageID message id
     * @param author    message author
     * @param message   message body
     * @param date      message time
     */
    public Message(String messageID, String author, String message, Date date) {
        this(messageID, author, message, date, true);
    }

    public String getMessageID() {
        return messageID;
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

    public boolean isSending() {
        return sending;
    }

    public void setSending(boolean sending) {
        this.sending = sending;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    @Override
    public String toString() {
        return "Message{" +
            "author='" + author + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
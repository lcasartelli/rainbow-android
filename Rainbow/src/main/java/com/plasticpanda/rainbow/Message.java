package com.plasticpanda.rainbow;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author Luca Casartelli
 */

@DatabaseTable(tableName = "messages")
public class Message {

    @DatabaseField(id = true)
    private String messageID;
    @DatabaseField(canBeNull = false)
    private String author;
    @DatabaseField(canBeNull = false)
    private Date date;
    @DatabaseField(canBeNull = false)
    private String message;
    @DatabaseField(canBeNull = false)
    private boolean isEncrypted;
    @DatabaseField(canBeNull = false, defaultValue = "false")
    private boolean sending;

    @DatabaseField(canBeNull = false, defaultValue = "m")
    private String type;

    /**
     * @param messageID   message id
     * @param author      message author
     * @param message     message body
     * @param date        message time
     * @param isEncrypted encryption
     */
    public Message(String messageID, String author, String message, Date date, boolean isEncrypted) {
        this(messageID, author, message, date, isEncrypted, false, "m");
    }

    public Message(String messageID, String author, String message, Date date, boolean isEncrypted, boolean sending) {
        this(messageID, author, message, date, isEncrypted, sending, "m");
    }

    public Message(String messageID, String author, String message, Date date, boolean isEncrypted, boolean sending, String type) {
        this.messageID = messageID;
        this.author = author;
        this.message = message;
        this.date = date;
        this.isEncrypted = isEncrypted;
        this.sending = sending;
        this.type = type;
    }


    public Message() {
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

    @Override
    public String toString() {
        return "Message{" +
            "messageID='" + messageID + '\'' +
            ", author='" + author + '\'' +
            ", date=" + date +
            ", message='" + message + '\'' +
            ", isEncrypted=" + isEncrypted +
            '}';
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
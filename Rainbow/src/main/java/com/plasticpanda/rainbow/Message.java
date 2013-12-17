package com.plasticpanda.rainbow;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * @author Luca Casartelli
 */

@DatabaseTable(tableName = "messages")
public class Message {

    private static final String TAG = Message.class.getName();

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
        this.isEncrypted = isEncrypted;
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

    public String getClearMessage() {
        String msg;
        try {
            msg = SecurityUtils.decrypt(this.message);
        } catch (Exception e) {
            msg = this.message;
            Log.e(TAG, "Decryption error: " + this.toString());
        }
        return msg;
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
}
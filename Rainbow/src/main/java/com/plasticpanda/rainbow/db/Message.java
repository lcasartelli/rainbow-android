/*
 * Copyright (C) 2013 Luca Casartelli luca@plasticpanda.com, Plastic Panda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plasticpanda.rainbow.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = DatabaseContract.MESSAGES_TABLE_NAME)
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
    private char type;

    public static final char TEXT_MESSAGE = 't';
    public static final char IMAGE_MESSAGE = 'i';

    /**
     * @param messageID   message id
     * @param author      message author
     * @param message     message body
     * @param date        message time
     * @param isEncrypted encryption
     */
    public Message(String messageID, String author, String message, Date date, boolean isEncrypted) {
        this(messageID, author, message, date, isEncrypted, false, TEXT_MESSAGE);
    }

    public Message(String messageID, String author, String message, Date date, boolean isEncrypted, boolean sending) {
        this(messageID, author, message, date, isEncrypted, sending, TEXT_MESSAGE);
    }

    public Message(String messageID, String author, String message, Date date, boolean isEncrypted, boolean sending, char type) {
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

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
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

    public Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }
}
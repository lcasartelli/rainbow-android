package com.plasticpanda.rainbow;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.net.URL;

@DatabaseTable(tableName = "images")
public class ImageMessage {

    @DatabaseField(id = true)
    private String messageID;
    @DatabaseField(canBeNull = false)
    private URL URL;
    @DatabaseField(canBeNull = false)
    private Uri URI;
    @DatabaseField(canBeNull = false)
    private int width;
    @DatabaseField(canBeNull = false)
    private int height;

    public ImageMessage() {
    }

    public ImageMessage(String messageID, URL url, Uri uri, int width, int height) {
        this.messageID = messageID;
        this.URL = url;
        this.URI = uri;
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Uri getURI() {
        return URI;
    }

    public void setURI(Uri URI) {
        this.URI = URI;
    }

    public URL getURL() {
        return URL;
    }

    public void setURL(URL URL) {
        this.URL = URL;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public static float getAspectRatio(int width, int height) {
        return (float) width / height;
    }
}

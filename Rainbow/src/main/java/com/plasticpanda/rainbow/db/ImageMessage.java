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

@DatabaseTable(tableName = DatabaseContract.IMAGES_TABLE_NAME)
public class ImageMessage {

    @DatabaseField(id = true)
    private String messageID;
    @DatabaseField(canBeNull = false)
    private String URL;
    @DatabaseField(canBeNull = false)
    private String URI;

    public ImageMessage() {
    }

    /**
     * @param messageID message id
     * @param url       remote url
     * @param uri       local uri (gallery)
     */
    public ImageMessage(String messageID, String url, String uri) {
        this.messageID = messageID;
        this.URL = url;
        this.URI = uri;

    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    /**
     * @param width  image width
     * @param height image height
     * @return aspect ratio
     */
    public static float getAspectRatio(int width, int height) {
        return (float) width / height;
    }
}

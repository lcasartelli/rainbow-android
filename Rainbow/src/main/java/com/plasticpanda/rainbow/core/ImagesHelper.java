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

package com.plasticpanda.rainbow.core;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.plasticpanda.rainbow.db.DatabaseHelper;
import com.plasticpanda.rainbow.db.ImageMessage;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.utils.SimpleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

public class ImagesHelper {

    private static final String TAG = ImagesHelper.class.getName();
    private static ImagesHelper sharedInstance;

    private Activity context;

    public ImagesHelper(Activity context) {
        this.context = context;
    }


    public static synchronized ImagesHelper getInstance(Activity context) {
        if (sharedInstance == null) {
            sharedInstance = new ImagesHelper(context);
        }
        return sharedInstance;
    }

    public void generateThumbnail(String url, Message message) throws IOException {

        String imageURL = "http://re.size.li/1/c/426x222/" + url;
        URL thumbURL = new URL(imageURL);
        Bitmap bmp = BitmapFactory.decodeStream(thumbURL.openConnection().getInputStream());
        String uri = storeImage(bmp, this.getThumbFile(message));
        // TODO: thumb cache
    }

    private File getImageFile(Message message) {
        String filename = "img_" + message.getDate().getTime() + ".jpeg";
        File file = null;
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard.exists()) {
            File rainbow_folder = new File(sdcard, "Rainbow");
            if (rainbow_folder.mkdirs()) {
                Log.i(TAG, "Directory created");
            } else {
                Log.i(TAG, "Directory already exist");
            }
            file = new File(rainbow_folder, filename);
        }

        return file;
    }

    private File getThumbFile(Message message) {
        String filename = "img_" + message.getDate().getTime() + ".jpeg";
        File file = null;
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File rainbow_folder = new File(cacheDir, "Rainbow");
            if (rainbow_folder.mkdirs()) {
                Log.i(TAG, "Directory created");
            } else {
                Log.i(TAG, "Directory already exist");
            }
            file = new File(rainbow_folder, filename);
        }
        return file;
    }

    private String storeImage(Bitmap bmp, File file) throws IOException {
        int IMAGE_QUALITY = 70;
        String path = null;
        if (file != null) {
            OutputStream fOut;
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fOut);
            fOut.flush();
            fOut.close();
            path = file.getAbsolutePath();
            Log.e(TAG, "ExternalStorageDirectory doesn't exist");
        }
        return path;
    }

    public void saveImage(final Message message, final SimpleListener imageListener) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... urls) {
                for (String url1 : urls) {
                    URL url;
                    Bitmap bmp;
                    try {
                        String imageURL = "http://re.size.li/1/c/426x222/" + url1;
                        url = new URL(imageURL);
                        bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        String uri = storeImage(bmp, getImageFile(message));
                        ImageMessage img = new ImageMessage(message.getMessageID(), message.getMessage(), uri, bmp.getWidth(), bmp.getHeight());
                        DatabaseHelper.getInstance(context).getImagesDao().create(img);

                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));

                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageListener.onSuccess();
                            }
                        });
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        }.execute(message.getMessage());
    }
}

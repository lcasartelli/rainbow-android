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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.loopj.android.image.WebImageCache;
import com.plasticpanda.rainbow.db.DatabaseHelper;
import com.plasticpanda.rainbow.db.ImageMessage;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.utils.ImageListener;
import com.plasticpanda.rainbow.utils.SecurityUtils;
import com.plasticpanda.rainbow.utils.SimpleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;

public class ImagesHelper {

    private static final String TAG = ImagesHelper.class.getName();
    private static final String SIZELI_URL = "http://re.size.li/1/c/426x222/";
    private static final int MAX_CACHE_SIZE = 300;
    private static ImagesHelper sharedInstance;


    private SharedPreferences sharedPreferences;
    private Activity context;

    // Cache
    public WebImageCache imgCache;
    private int cacheSize;


    public ImagesHelper(Activity context) {
        this.context = context;
        this.sharedPreferences = this.context.getSharedPreferences("rainbow", Context.MODE_PRIVATE);
        // init cache
        this.imgCache = new WebImageCache(this.context);
        this.cacheSize = this.sharedPreferences.getInt("cache_size", 0);
        checkCacheSize();
    }

    private void checkCacheSize() {
        if (cacheSize + 1 > MAX_CACHE_SIZE) {
            imgCache.clear();
            cacheSize = 0;
            sharedPreferences.edit()
                .putInt("cache_size", cacheSize)
                .commit();
        }
    }


    public static synchronized ImagesHelper getInstance(Activity context) {
        if (sharedInstance == null) {
            sharedInstance = new ImagesHelper(context);
        }
        return sharedInstance;
    }


    public void retrieveImageThumb(Message messageDecrypted, final ImageListener imageListener) throws IOException {

        new AsyncTask<Message, Integer, String>() {

            @Override
            protected String doInBackground(Message... messages) {
                for (Message message : messages) {
                    File cacheDir = context.getCacheDir();
                    File rainbow_cache_folder = new File(cacheDir, "Rainbow");
                    Bitmap bmp = imgCache.get(message.getMessage());

                    if (bmp != null) {
                        Log.d(TAG, "get image from cache");
                    } else {
                        String imageURL = SIZELI_URL + message.getMessage();
                        URL thumbURL;
                        try {
                            if (cacheDir != null) {
                                if (rainbow_cache_folder.mkdirs()) {
                                    Log.i(TAG, "Directory created");
                                } else {
                                    Log.i(TAG, "Directory already exist");
                                }

                                thumbURL = new URL(imageURL);

                                bmp = BitmapFactory.decodeStream(
                                    thumbURL
                                        .openConnection()
                                        .getInputStream()
                                );
                                checkCacheSize();
                                imgCache.put(message.getMessage(), bmp);
                                cacheSize += 1;
                                sharedPreferences.edit()
                                    .putInt("cache_size", cacheSize)
                                    .commit();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    final Bitmap finalBmp = bmp;
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageListener.onSuccess(finalBmp);
                        }
                    });

                } // end for

                return null;
            }
        }.execute(messageDecrypted);

    }

    public void retrieveImage(Message messageDecrypted, final SimpleListener imageListener) throws IOException, SQLException {
        String messageContent = SecurityUtils.decrypt(messageDecrypted.getMessage());
        String filename = messageContent;
        File sdcard = Environment.getExternalStorageDirectory();
        File imageFile = null;
        Bitmap bmp;

        if (sdcard.exists()) {
            File rainbow_folder = new File(sdcard, "Rainbow");
            if (rainbow_folder.mkdirs()) {
                Log.i(TAG, "Directory created");
            } else {
                Log.i(TAG, "Directory already exist");
            }
            imageFile = new File(rainbow_folder, filename);
        }

        // get original bitmap from url
        bmp = BitmapFactory.decodeStream(
            new URL(messageContent)
                .openConnection()
                .getInputStream()
        );

        // save image
        String location = storeImage(bmp, imageFile);
        saveImageMessage(messageDecrypted, location);

        // emit broadcast
        context.sendBroadcast(new Intent(
            Intent.ACTION_MEDIA_MOUNTED,
            Uri.parse("file://" + Environment.getExternalStorageDirectory())
        ));
        // callback
        imageListener.onSuccess();
    }

    private void saveImageMessage(Message msg, String uri) throws SQLException {
        String messageContent = SecurityUtils.decrypt(msg.getMessage());
        ImageMessage img = new ImageMessage(
            msg.getMessageID(),
            messageContent,
            uri
        );
        DatabaseHelper.getInstance(context).getImagesDao().create(img);
    }

    public static String storeImage(Bitmap bmp, File file) throws IOException {
        int IMAGE_QUALITY = 70;
        String path = null;
        if (file != null) {
            OutputStream fOut;
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fOut);
            fOut.flush();
            fOut.close();
            path = file.getAbsolutePath();
        }
        return path;
    }
}

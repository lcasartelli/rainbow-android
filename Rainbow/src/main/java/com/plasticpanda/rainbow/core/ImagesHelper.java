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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.loopj.android.image.WebImageCache;
import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.utils.ImageListener;
import com.plasticpanda.rainbow.utils.SecurityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class ImagesHelper {

    private static final String TAG = ImagesHelper.class.getName();
    private static final String SHARED_CACHE_SIZE = "cache_size";
    private static final int MAX_CACHE_SIZE = 300;
    private static ImagesHelper sharedInstance;


    private SharedPreferences sharedPreferences;
    private Activity context;

    // Cache
    private WebImageCache imgCache;
    private int cacheSize;


    public ImagesHelper(Activity context) {
        this.context = context;
        this.sharedPreferences = this.context.getSharedPreferences(RainbowConst.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        // init cache
        this.imgCache = new WebImageCache(this.context);
        this.cacheSize = this.sharedPreferences.getInt(SHARED_CACHE_SIZE, 0);
        checkCacheSize();
    }

    private void checkCacheSize() {
        if (cacheSize + 1 >= MAX_CACHE_SIZE) {
            imgCache.clear();
            cacheSize = 0;
            sharedPreferences.edit()
                .putInt(SHARED_CACHE_SIZE, cacheSize)
                .commit();
        }
    }


    public static synchronized ImagesHelper getInstance(Activity context) {
        if (sharedInstance == null) {
            sharedInstance = new ImagesHelper(context);
        }
        return sharedInstance;
    }


    public void retrieveImageThumb(final Message messageDecrypted, final ImageListener imageListener) {
        new Thread(new Runnable() {
            public void run() {
                File cacheDir = context.getCacheDir();
                File rainbow_cache_folder = new File(cacheDir, RainbowConst.IMAGES_DIRECTORY);
                Bitmap bmp = imgCache.get(messageDecrypted.getMessage());

                if (bmp != null) {
                    Log.d(TAG, "get image from cache");
                } else {
                    String imageURL = context.getString(R.string.images_resize_url) + messageDecrypted.getMessage();
                    URL thumbURL;
                    try {
                        if (cacheDir != null) {
                            if (rainbow_cache_folder.mkdirs()) {
                                Log.i(TAG, "Cache directory created");
                            } else {
                                Log.i(TAG, "Cache directory already exist");
                            }

                            thumbURL = new URL(imageURL);

                            bmp = BitmapFactory.decodeStream(
                                thumbURL
                                    .openConnection()
                                    .getInputStream()
                            );
                            checkCacheSize();
                            imgCache.put(messageDecrypted.getMessage(), bmp);
                            cacheSize += 1;
                            sharedPreferences.edit()
                                .putInt(SHARED_CACHE_SIZE, cacheSize)
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
            }
        }).start();
    }

    public void retrieveImage(final Message messageDecrypted) {
        final String image_url = SecurityUtils.decrypt(messageDecrypted.getMessage());

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Environment.getExternalStorageDirectory().exists()) {

                    File rainbow_folder = new File(Environment.getExternalStorageDirectory(), RainbowConst.IMAGES_DIRECTORY);
                    if (rainbow_folder.mkdirs()) {
                        Log.i(TAG, "Images directory created");
                    } else {
                        Log.i(TAG, "Images directory already exist");
                    }
                    File imageFile = new File(rainbow_folder, getFilename(messageDecrypted));
                    Bitmap bmp = null;
                    try {
                        bmp = BitmapFactory.decodeStream(
                            new URL(image_url)
                                .openConnection()
                                .getInputStream()
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // save image
                    try {
                        storeImage(bmp, imageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // emit broadcast
                    /*context.sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                    ));*/

                    Log.i(TAG, "download attachment done, message: " + messageDecrypted.toString());
                } else {
                    Log.e(TAG, "download attachment failed, message: " + messageDecrypted.toString());
                }
            }
        }).start();
    }

    private static String getFilename(Message msg) {
        return msg.getMessageID() + ".jpeg";
    }


    private static void storeImage(Bitmap bmp, File file) throws IOException {
        if (file != null) {
            OutputStream fOut;
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            fOut.flush();
            fOut.close();
        }
    }

    public Bitmap getImage(Message message) {
        String image_url = getFilename(message);
        Bitmap bmp = null;

        if (Environment.getExternalStorageDirectory().exists()) {
            File rainbow_folder = new File(Environment.getExternalStorageDirectory(), RainbowConst.IMAGES_DIRECTORY);
            File imageFile = new File(rainbow_folder, image_url);
            bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        }

        return bmp;
    }
}

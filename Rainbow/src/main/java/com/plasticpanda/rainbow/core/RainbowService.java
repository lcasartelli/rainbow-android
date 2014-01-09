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

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.ui.MainFragment;
import com.plasticpanda.rainbow.utils.PNListener;


public class RainbowService extends Service {

    private static final String TAG = RainbowService.class.getName();

    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        RainbowHelper.getInstance(this.getApplicationContext()).pubNub(new PNListener() {
            @Override
            public void onReceiveMessage(Message message) {
                if (RainbowApp.isActivityVisible()) {
                    MainFragment.getInstance().refreshAdapter();
                } else {
                    emitNotification(message);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void emitNotification(Message message) {
        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                .setContentTitle(message.getAuthor())
                .setSmallIcon(R.drawable.notification)
                .setContentText("Hello! :)");

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
            0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
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
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.db.Message;
import com.plasticpanda.rainbow.utils.PNListener;
import com.plasticpanda.rainbow.utils.SecurityUtils;


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

        RainbowHelper rainbowHelper = RainbowHelper.getInstance(this.getApplicationContext());

        rainbowHelper.pubNub(new PNListener() {
            @Override
            public void onReceiveMessage(Message message) {

                if (RainbowApp.isActivityVisible()) {
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(new Intent("new-message"));
                } else {
                    emitNotification(message);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void emitNotification(Message message) {
        String messageText = message.getMessage();
        if (message.isEncrypted()) {
            messageText = SecurityUtils.decrypt(messageText);
        }

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                .setContentTitle(message.getAuthor())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification))
                .setSmallIcon(R.drawable.notification)
                .setContentText(messageText);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
            0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (getApplicationContext() != null) {
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);
        }
        super.onTaskRemoved(rootIntent);
    }
}

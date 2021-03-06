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

import android.app.Application;
import android.content.Intent;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.db.DatabaseHelper;
import com.testflightapp.lib.TestFlight;

public class RainbowApp extends Application {

    private static boolean isActivityVisible;

    @Override
    public void onCreate() {
        super.onCreate();
        TestFlight.takeOff(this, getString(R.string.testflight_token));

        DatabaseHelper.getInstance(this.getApplicationContext());
        startService(new Intent(this, RainbowService.class));
    }

    public static boolean isActivityVisible() {
        return isActivityVisible;
    }

    public static void activityResumed() {
        isActivityVisible = true;
    }

    public static void activityPaused() {
        isActivityVisible = false;
    }
}

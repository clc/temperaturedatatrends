/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.clcworld.thermometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Receives the hourly alarm to prompt the user + sets up the next alarm.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Delay the alarm until the right interval from when the user last took
        // a reading.
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        long lastMeasuredTime = prefs.getLong("LAST_READING", 0);
        long lastAlarmedTime = prefs.getLong("LAST_ALARM", 0);
        long intervalMillis = AlarmScheduler.ALARM_EVERY_N_MINUTES * prefs.getInt("INTERVAL", 1) * 60 * 1000; // INTERVAL is in hours, multiply by 60 to get minutes.
        long currentTime = System.currentTimeMillis();
        long measuredDelta = currentTime - lastMeasuredTime;
        long alarmDelta = currentTime - lastAlarmedTime;
        long delta = Math.min(measuredDelta, alarmDelta);

        if (delta < intervalMillis) {
            // Do not annoy the user with more alerts than the interval would
            // dictate.
            AlarmScheduler.setupOneTimeAlarm(context,
                    System.currentTimeMillis() + (intervalMillis - delta));
            return;
        }
        AlarmScheduler.setupOneTimeAlarm(context, System.currentTimeMillis() + intervalMillis);
        // It has been more than the interval for when the user needs to take a
        // reading. Start the LauncherActivity and make them take a reading.
        Editor editor = prefs.edit();
        editor.putLong("LAST_ALARM", currentTime);
        editor.commit();
        Intent i = new Intent(context, LauncherActivity.class);
        i.putExtra("FROM_ALARM", true);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }
}

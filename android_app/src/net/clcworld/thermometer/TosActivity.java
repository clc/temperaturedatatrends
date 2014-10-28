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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import net.clcworld.thermometer.setuppager.SetupPagerActivity;

/**
 * Shows the terms of service for this app.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class TosActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String targetUrl = prefs.getString("targetUrl", "");
        final boolean isActivated = targetUrl.length() > 0;

        final Activity self = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Terms of Service");
        if (isActivated) {
            builder.setMessage(
                    "Terms of Service text for use with Public Health Services goes here.");
        } else {
            builder.setMessage("Terms of Service text for local use only goes here.");
        }

        builder.setPositiveButton("Accept", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (isActivated) {
                    Intent intent = new Intent();
                    intent.setClass(self, SetupPagerActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    // Running without activation; this will only work locally.
                    final SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(self);
                    Editor editor = prefs.edit();
                    editor.putBoolean("ISFIRSTRUN", false);
                    editor.commit();
                    AlarmScheduler.setupOneTimeAlarm(self, System.currentTimeMillis() + 100);
                    finish();
                    return;
                }
            }
        });
        builder.setNegativeButton("Decline", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(self);
                Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }
}

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Main entry point to the app. Determines whether to show the take temperature prompt or to take
 * user through the initial setup process.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class LauncherActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent();
        if (prefs.getBoolean("ISFIRSTRUN", true)) {
            // Launch setup activity if the user hasn't provided a patient ID
            // yet.
            intent.setClass(this, TosActivity.class);
        } else {
            intent.setClass(this, TakeTemperatureReadingActivity.class);

            Intent data = this.getIntent();
            if (data.getBooleanExtra("FROM_ALARM", false)) {
                // Pass on the fact that this intent originated from the
                // recurring alarm.
                intent.putExtra("FROM_ALARM", true);
            }
        }
        finish();
        startActivity(intent);
    }

}

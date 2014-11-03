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
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Accepts an activation link for setting up all the necessary fields for using
 * Temperature Data Logger.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class LinkSetupActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = this.getIntent();

        String input = data.getData().toString() + "";
        Log.e("Received data", input);
        String startingString = "https://tdt/?";
        if (!input.startsWith(startingString)) {
            // Something went horribly wrong, bail!
            Toast.makeText(this, "Invalid TDT link.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        input = input.replace(startingString, "");
        String[] args = input.split("&");

        String targetUrl = "";
        String idField = "";
        String tempField = "";
        String feelingField = "";
        String symptomsField = "";
        String idValue = "";
        String destValue = "";

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("targetUrl=")) {
                targetUrl = arg.replace("targetUrl=", "");
            } else if (arg.startsWith("idField=")) {
                idField = arg.replace("idField=", "");
            } else if (arg.startsWith("tempField=")) {
                tempField = arg.replace("tempField=", "");
            } else if (arg.startsWith("feelingField=")) {
                feelingField = arg.replace("feelingField=", "");
            } else if (arg.startsWith("symptomsField=")) {
            	symptomsField = arg.replace("symptomsField=", "");
            } else if (arg.startsWith("idValue=")) {
                idValue = arg.replace("idValue=", "");
            } else if (arg.startsWith("destValue=")) {
                destValue = arg.replace("destValue=", "");
            }
        }

        if (targetUrl.isEmpty() || idField.isEmpty() || tempField.isEmpty()
                || feelingField.isEmpty()
                || symptomsField.isEmpty()
                || idValue.isEmpty()
                || destValue.isEmpty()) {
            // Something went horribly wrong, bail!
            Toast.makeText(this, "Invalid TDT link.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        // To prevent accidentally resetting information, don't allow a second
        // activation. In the of chance that a second activation is truly
        // needed, users can uninstall the app and reinstall it.
        String savedTargetUrl = prefs.getString("targetUrl", "");
        final boolean isActivated = savedTargetUrl.length() > 0;
        if (isActivated) {
            Log.e("TemperatureDataTrends",
                    "Ignoring activation link. TDT has already been activated.");
            Toast.makeText(this, "Temperature Data Trends has already been activated.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Editor edit = prefs.edit();
        edit.clear();
        edit.putString("targetUrl", targetUrl);
        edit.putString("idField", idField);
        edit.putString("tempField", tempField);
        edit.putString("feelingField", feelingField);
        edit.putString("symptomsField", symptomsField);
        edit.putString("ID", idValue);
        edit.putString("DEST", destValue.replaceAll("%20", " "));
        edit.commit();

        finish();
        Intent intent = new Intent();
        intent.setClass(this, TosActivity.class);
        startActivity(intent);
    }

}

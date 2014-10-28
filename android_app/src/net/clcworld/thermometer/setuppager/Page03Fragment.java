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

package net.clcworld.thermometer.setuppager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.clcworld.thermometer.AlarmScheduler;
import net.clcworld.thermometer.R;

/**
 * Page 3 of the setup wizard.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class Page03Fragment extends Fragment {
    public static Fragment newInstance(Context context) {
        Page03Fragment f = new Page03Fragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.page_03, null);
        final EditText id = (EditText) root.findViewById(R.id.patientId);

        final Activity parent = this.getActivity();

        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(parent);
        id.setText(prefs.getString("ID", ""));

        Button okButton = (Button) root.findViewById(R.id.ok);
        okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Editor editor = prefs.edit();
                String patientId = id.getText().toString();
                if (patientId.length() > 0) {
                    editor.putString("ID", patientId);
                    editor.putBoolean("ISFIRSTRUN", false);
                    editor.commit();
                    AlarmScheduler.setupOneTimeAlarm(parent, System.currentTimeMillis() + 100);
                    Toast.makeText(parent, R.string.patientidsaved, Toast.LENGTH_LONG)
                            .show();
                    parent.finish();
                }
            }
        });

        return root;
    }

}

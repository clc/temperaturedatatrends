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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Shows the UI prompt for taking a temperature measurement and giving a status
 * update about how you are feeling (well vs sick).
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class TakeTemperatureReadingActivity extends Activity {
    private final int NOTIFICATION_NUMBER = 1337;
    private static final String STATUS_WELL = "Well";
    private static final String STATUS_SICK = "Sick";
    private static final String PREFSKEY_HISTORY = "HISTORY";
    private static final String PREFSKEY_SYMPTOMS_EXTREMETIREDNESS = "Extreme tiredness";
    private static final String PREFSKEY_SYMPTOMS_MUSCLEPAIN = "Muscle pain";
    private static final String PREFSKEY_SYMPTOMS_HEADACHE = "Headache";
    private static final String PREFSKEY_SYMPTOMS_SORETHROAT = "Sore throat";
    private static final String PREFSKEY_SYMPTOMS_VOMITING = "Vomiting";
    private static final String PREFSKEY_SYMPTOMS_DIARRHEA = "Diarrhea";
    private static final String PREFSKEY_SYMPTOMS_RASH = "Rash";
    private static final String PREFSKEY_SYMPTOMS_BLEEDING = "Unexplained bleeding";
    private static final String PREFSKEY_SYMPTOMS_PAINRELIEF = "Taking pain relievers";
    private Activity mSelf;
    private EditText mTemperatureEditText;
    private RadioGroup mFeelingRadioGroup;
    private String mFeeling;
    private SharedPreferences mPrefs;
    private NotificationManager mNotificationManager;
    private String mHistory = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelf = this;

        Intent i = new Intent();
        i.setClass(this, LauncherActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent relaunchIntent = PendingIntent.getActivity(this, 0, i, 0);

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent data = this.getIntent();

        if (data.getBooleanExtra("FROM_ALARM", false)) {
            // Only show the persistant notification if it's from the alarm.
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.app_icon)
                            .setTicker(getString(R.string.notification))
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(getString(R.string.notification))
                            .setOngoing(true)
                            .setOnlyAlertOnce(false)
                            .setContentIntent(relaunchIntent)
                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setLights(Color.RED, 500, 500);
            mNotificationManager.notify(NOTIFICATION_NUMBER, builder.build());
        }

        mPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        mFeeling = mPrefs.getString("lastFeeling", STATUS_WELL);
        mHistory = mPrefs.getString(PREFSKEY_HISTORY, "");
        final String id = mPrefs.getString("ID", "");

        setContentView(R.layout.temperature);
        mTemperatureEditText = (EditText) findViewById(R.id.temperature);
        mTemperatureEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        final LinearLayout symptomsList = (LinearLayout) findViewById(R.id.symptoms);
        final CheckBox extremeTiredness = (CheckBox) findViewById(R.id.extremetiredness);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_EXTREMETIREDNESS, false)) {
            extremeTiredness.setChecked(true);
        }
        final CheckBox musclepain = (CheckBox) findViewById(R.id.musclepain);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_MUSCLEPAIN, false)) {
            musclepain.setChecked(true);
        }
        final CheckBox headache = (CheckBox) findViewById(R.id.headache);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_HEADACHE, false)) {
            headache.setChecked(true);
        }
        final CheckBox sorethroat = (CheckBox) findViewById(R.id.sorethroat);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_SORETHROAT, false)) {
            sorethroat.setChecked(true);
        }
        final CheckBox vomiting = (CheckBox) findViewById(R.id.vomiting);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_VOMITING, false)) {
            vomiting.setChecked(true);
        }
        final CheckBox diarrhea = (CheckBox) findViewById(R.id.diarrhea);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_DIARRHEA, false)) {
            diarrhea.setChecked(true);
        }
        final CheckBox rash = (CheckBox) findViewById(R.id.rash);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_RASH, false)) {
            rash.setChecked(true);
        }
        final CheckBox bleeding = (CheckBox) findViewById(R.id.bleeding);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_BLEEDING, false)) {
            bleeding.setChecked(true);
        }
        final CheckBox painrelief = (CheckBox) findViewById(R.id.painrelief);
        if (mPrefs.getBoolean(PREFSKEY_SYMPTOMS_PAINRELIEF, false)) {
            painrelief.setChecked(true);
        }

        final Button submitButton = (Button) findViewById(R.id.submit);
        submitButton.setText(R.string.save);
        final String destination = mPrefs.getString("DEST", "");
        if (destination.length() > 0) {
            submitButton.setText(getString(R.string.submit) + destination);
        }
        submitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String tempString = mTemperatureEditText.getText().toString();
                try {
                    float t = Float.parseFloat(tempString);
                    // Come up with better thresholds + use an alert
                    // confirmation prompt.
                    if ((t < 91) || (t > 120)) {
                        Toast.makeText(mSelf, R.string.retake, Toast.LENGTH_LONG).show();
                        return;
                    }

                    mTemperatureEditText.setEnabled(false);
                    submitButton.setEnabled(false);
                    Editor editor = mPrefs.edit();
                    editor.putLong("LAST_READING", System.currentTimeMillis());

                    ArrayList<String> symptoms = new ArrayList<String>();
                    if (mFeeling.equals(STATUS_WELL)) {
                        editor.putBoolean(PREFSKEY_SYMPTOMS_EXTREMETIREDNESS, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_MUSCLEPAIN, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_HEADACHE, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_SORETHROAT, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_VOMITING, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_DIARRHEA, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_RASH, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_BLEEDING, false);
                        editor.putBoolean(PREFSKEY_SYMPTOMS_PAINRELIEF, false);
                    } else {
                        if (extremeTiredness.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_EXTREMETIREDNESS, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_EXTREMETIREDNESS);
                        }
                        if (musclepain.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_MUSCLEPAIN, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_MUSCLEPAIN);
                        }
                        if (headache.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_HEADACHE, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_HEADACHE);
                        }
                        if (sorethroat.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_SORETHROAT, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_SORETHROAT);
                        }
                        if (vomiting.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_VOMITING, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_VOMITING);
                        }
                        if (diarrhea.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_DIARRHEA, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_DIARRHEA);
                        }
                        if (rash.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_RASH, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_RASH);
                        }
                        if (bleeding.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_BLEEDING, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_BLEEDING);
                        }
                        if (painrelief.isChecked()) {
                            editor.putBoolean(PREFSKEY_SYMPTOMS_PAINRELIEF, true);
                            symptoms.add(PREFSKEY_SYMPTOMS_PAINRELIEF);
                        }
                    }

                    editor.commit();
                    mNotificationManager.cancelAll();

                    DecimalFormat decFormat = new DecimalFormat("0.0");
                    sendData(id, decFormat.format(t), mFeeling, symptoms);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mFeelingRadioGroup = (RadioGroup) findViewById(R.id.radioGroupFeeling);
        int selectedId = R.id.radioWell;
        if (mFeeling.equals(STATUS_SICK)) {
            selectedId = R.id.radioSick;
            if (destination.length() > 0) {
                symptomsList.setVisibility(View.VISIBLE);
            }
        }
        mFeelingRadioGroup.check(selectedId);
        mFeelingRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioSick) {
                    mFeeling = STATUS_SICK;
                    if (destination.length() > 0) {
                        symptomsList.setVisibility(View.VISIBLE);
                    }
                } else {
                    mFeeling = STATUS_WELL;
                    symptomsList.setVisibility(View.GONE);
                }
                Editor editor = mPrefs.edit();
                editor.putString("lastFeeling", mFeeling);
                editor.commit();
            }
        });

        Button historyButton = (Button) findViewById(R.id.history);
        if (mHistory.length() < 1) {
            historyButton.setVisibility(View.GONE);
        }
        historyButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        mSelf);
                alertDialogBuilder.setTitle(R.string.history);
                alertDialogBuilder.setPositiveButton(R.string.ok, null);
                AlertDialog dialog = alertDialogBuilder.create();
                TextView text = new TextView(mSelf);
                text.setTextSize(16);
                text.setPadding(40, 40, 40, 40);
                text.setText(mHistory);
                ScrollView scroll = new ScrollView(mSelf);
                scroll.addView(text);
                dialog.setView(scroll);
                dialog.show();
            }
        });

        Button viewTosButton = (Button) findViewById(R.id.viewtos);
        viewTosButton.setText(R.string.tos_title_local);
        if (destination.length() > 0) {
            viewTosButton.setText(R.string.tos_title_publichealth);
        }
        viewTosButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent viewTosIntent = new Intent();
                viewTosIntent.setClass(mSelf, TosActivity.class);
                viewTosIntent.putExtra("VIEW_ONLY", true);
                startActivity(viewTosIntent);
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void sendData(final String patientId, final String temperature, final String feeling,
            final ArrayList<String> symptoms) {
        final String targetUrl = mPrefs.getString("targetUrl", "");
        final String idField = mPrefs.getString("idField", "");
        final String tempField = mPrefs.getString("tempField", "");
        final String feelingField = mPrefs.getString("feelingField", "");
        final String symptomsField = mPrefs.getString("symptomsField", "");
        if (targetUrl.length() > 0) {
            UploadUtils.sendData(targetUrl, idField, tempField, feelingField, symptomsField,
                    patientId,
                    temperature,
                    feeling, symptoms);
            Toast.makeText(mSelf, R.string.submitted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mSelf, R.string.saved, Toast.LENGTH_SHORT).show();
        }
                
        String entry = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
                new Date()) + "    " + temperature + "°F    " + feeling + "\n";
        Editor editor = mPrefs.edit();
        editor.putString(PREFSKEY_HISTORY, entry + mHistory);
        editor.commit();

        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        long lastReadingTime = mPrefs.getLong("LAST_READING", -1);
        if (lastReadingTime != -1) {
            long timeDelta = System.currentTimeMillis() - lastReadingTime;
            String templateStr = this.getString(R.string.timesincelastmeasurement);
            String timeSinceLastReading = String.format(templateStr,
                    TimeUnit.MILLISECONDS.toHours(timeDelta),
                            TimeUnit.MILLISECONDS.toMinutes(timeDelta)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDelta)));
            final TextView timeSinceLastReadingTextView = (TextView) findViewById(
                    R.id.timeSinceLastMeasurement);
            timeSinceLastReadingTextView.setText(timeSinceLastReading);
        }
    }
}

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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for uploading data using HTTP Post.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class UploadUtils {

    public static void sendData(
            final String targetUrl, final String idField, final String tempField,
            final String feelingField,
            final String symptomsField,
            final String id, final String temp, final String feeling, final ArrayList<String> symptoms) {
        new Thread(new Runnable() {
            public void run() {
                // Create a new HttpClient and Post Header
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(targetUrl);

                try {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
                           2);
                    nameValuePairs.add(new BasicNameValuePair(
                            idField, id));
                    nameValuePairs.add(new BasicNameValuePair(
                            tempField, temp));
                    nameValuePairs.add(new BasicNameValuePair(
                            feelingField, feeling));
                    for (int i=0; i<symptoms.size(); i++){
                        nameValuePairs.add(new BasicNameValuePair(
                        		symptomsField, symptoms.get(i)));
                    }
                    // Must put this extra pageHistory field here, otherwise Forms will reject the symptoms.
                    nameValuePairs.add(new BasicNameValuePair(
                            "pageHistory", "0,1"));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    
                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

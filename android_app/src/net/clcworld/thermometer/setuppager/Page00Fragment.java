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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import net.clcworld.thermometer.R;

/**
 * Page 0 of the setup wizard.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class Page00Fragment extends Fragment {

    public static Fragment newInstance(Context context) {
        Page00Fragment f = new Page00Fragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.page_00, null);
        Button next = (Button) root.findViewById(R.id.next);
        final SetupPagerActivity parent = (SetupPagerActivity) this.getActivity();
        next.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                parent.jumpToPage(1);
            }            
        });        
        return root;
    }
}

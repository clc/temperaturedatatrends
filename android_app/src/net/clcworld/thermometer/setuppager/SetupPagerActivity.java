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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import net.clcworld.thermometer.R;

/**
 * Setup wizard that introduces the user to this app and gets them setup.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class SetupPagerActivity extends FragmentActivity {
    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private CircleView mCircle00;
    private CircleView mCircle01;
    private CircleView mCircle02;
    private CircleView mCircle03;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager);
        setUpView();
        setTab();
        mCircle00 = (CircleView) findViewById(R.id.circle00);
        mCircle01 = (CircleView) findViewById(R.id.circle01);
        mCircle02 = (CircleView) findViewById(R.id.circle02);
        mCircle03 = (CircleView) findViewById(R.id.circle03);
        mCircle00.setEnabled(true);
    }

    private void setUpView() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new ViewPagerAdapter(getApplicationContext(), getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
    }

    private void setTab() {
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            public void onPageScrollStateChanged(int position) {
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mCircle00.setEnabled(true);
                        mCircle01.setEnabled(false);
                        mCircle02.setEnabled(false);
                        mCircle03.setEnabled(false);
                        break;
                    case 1:
                        mCircle00.setEnabled(false);
                        mCircle01.setEnabled(true);
                        mCircle02.setEnabled(false);
                        mCircle03.setEnabled(false);
                        break;
                    case 2:
                        mCircle00.setEnabled(false);
                        mCircle01.setEnabled(false);
                        mCircle02.setEnabled(true);
                        mCircle03.setEnabled(false);
                        break;
                    case 3:
                        mCircle00.setEnabled(false);
                        mCircle01.setEnabled(false);
                        mCircle02.setEnabled(false);
                        mCircle03.setEnabled(true);
                        break;
                }
                mCircle00.postInvalidate();
                mCircle01.postInvalidate();
                mCircle02.postInvalidate();
                mCircle03.postInvalidate();
            }

        });

    }
}

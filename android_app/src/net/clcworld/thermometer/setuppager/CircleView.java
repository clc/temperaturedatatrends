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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Draws the progress circles at the bottom of the setup wizard.
 * 
 * @author Charles L Chen (clchen@google.com)
 */
public class CircleView extends View {
    private Paint mPaint;

    /**
     * @param context
     */
    public CircleView(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
        mPaint.setAntiAlias(true);
    }

    /**
     * @param context
     * @param attrs
     */
    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
        mPaint.setAntiAlias(true);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            mPaint.setColor(Color.DKGRAY);
        } else {
            mPaint.setColor(Color.LTGRAY);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth() / 2,
                mPaint);
    }

}

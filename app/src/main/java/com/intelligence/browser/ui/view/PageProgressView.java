
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligence.browser.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PageProgressView extends ImageView {

    public static final int MAX_PROGRESS = 10000;
    private static final int MSG_UPDATE = 42;
    private static final int MSG_SELF_UPDATE = 43;
    private static final int STEPS = 10;
    private static final int DELAY = 40;

    private int mCurrentProgress;
    private int mTargetProgress;
    private int mIncrement;
    private Rect mBounds;
    private Handler mHandler;
    boolean mIsSelfUpdate;
    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PageProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public PageProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     */
    public PageProgressView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context ctx) {
        mBounds = new Rect(0,0,0,0);
        mCurrentProgress = 0;
        mTargetProgress = 0;
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_UPDATE) {
                    mCurrentProgress = Math.min(mTargetProgress,
                            mCurrentProgress + mIncrement);
                    mBounds.right = getWidth() * mCurrentProgress / MAX_PROGRESS;
                    postInvalidate();
                    if (mCurrentProgress < mTargetProgress) {
                        sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE), DELAY);
                    }
                } else if (msg.what == MSG_SELF_UPDATE) {
                    mCurrentProgress += mIncrement;
                    mBounds.right = getWidth() * mCurrentProgress / MAX_PROGRESS;
                    if (mBounds.right >= getWidth()) {
                        mBounds.right = getWidth();
                        setVisibility(GONE);
                    } else {
                        setVisibility(VISIBLE);
                    }
                    postInvalidate();
                    if (mCurrentProgress < mTargetProgress) {
                        sendMessageDelayed(mHandler.obtainMessage(MSG_SELF_UPDATE), DELAY);
                    }
                }
            }
        };
    }

    @Override
    public void onLayout(boolean f, int l, int t, int r, int b) {
        mBounds.left = 0;
        mBounds.right = (r - l) * mCurrentProgress / MAX_PROGRESS;
        mBounds.top = 0;
        mBounds.bottom = b-t;
    }

    public void setProgress(int progress) {
        if (progress != MAX_PROGRESS) {
            mIsSelfUpdate =false;
        }
        if (mIsSelfUpdate) {
            return;
        }
        mCurrentProgress = mTargetProgress;
        mTargetProgress = progress;
        if (mTargetProgress == MAX_PROGRESS && mTargetProgress - mCurrentProgress > 1000) {
            setVisibility(VISIBLE);
            mIsSelfUpdate = true;
            mIncrement = (MAX_PROGRESS - mCurrentProgress) / STEPS;
            mHandler.removeMessages(MSG_UPDATE);
            mHandler.removeMessages(MSG_SELF_UPDATE);
            mHandler.sendEmptyMessage(MSG_SELF_UPDATE);
        } else {
            mIsSelfUpdate = false;
            mIncrement = (mTargetProgress - mCurrentProgress) / STEPS;
            mHandler.removeMessages(MSG_UPDATE);
            mHandler.removeMessages(MSG_SELF_UPDATE);
            mHandler.sendEmptyMessage(MSG_UPDATE);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        Drawable d = getDrawable();
        d.setBounds(mBounds);
        d.draw(canvas);
    }
}

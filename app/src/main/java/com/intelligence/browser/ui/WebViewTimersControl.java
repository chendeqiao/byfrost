/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.intelligence.browser.ui;

import android.os.Looper;
import android.webkit.WebView;

public class WebViewTimersControl {

    private static final String LOGTAG = "WebViewTimersControl";

    private static WebViewTimersControl sInstance;

    private boolean mBrowserActive;
    private boolean mPrerenderActive;

    /**
     * Get the static instance. Must be called from UI thread.
     */
    public static WebViewTimersControl getInstance() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("WebViewTimersControl.get() called on wrong thread");
        }
        if (sInstance == null) {
            sInstance = new WebViewTimersControl();
        }
        return sInstance;
    }

    private WebViewTimersControl() {
    }

    private void resumeTimers(WebView wv) {
        if (wv != null) {
            wv.resumeTimers();
        }
    }

    private void maybePauseTimers(WebView wv) {
        if (!mBrowserActive && !mPrerenderActive && wv != null) {
            wv.pauseTimers();
        }
    }

    public void onBrowserActivityResume(WebView wv) {
        mBrowserActive = true;
        resumeTimers(wv);
    }

    public void onBrowserActivityPause(WebView wv) {
        mBrowserActive = false;
        maybePauseTimers(wv);
    }

    public void onPrerenderStart(WebView wv) {
        mPrerenderActive = true;
        resumeTimers(wv);
    }

    public void onPrerenderDone(WebView wv) {
        mPrerenderActive = false;
        maybePauseTimers(wv);
    }

}

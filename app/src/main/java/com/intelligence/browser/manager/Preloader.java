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

package com.intelligence.browser.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.controller.PreloadController;
import com.intelligence.browser.webview.PreloadedTabControl;
import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.ui.WebViewTimersControl;

import java.util.Map;

public class Preloader {

    private final static String LOGTAG = "browser.preloader";

    private static final int PRERENDER_TIMEOUT_MILLIS = 30 * 1000; // 30s

    private static Preloader sInstance;

    private final Context mContext;
    private final Handler mHandler;
    private final BrowserWebViewFactory mFactory;
    private volatile PreloaderSession mSession;

    public static void initialize(Context context) {
        sInstance = new Preloader(context);
    }

    public static Preloader getInstance() {
        return sInstance;
    }

    private Preloader(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mSession = null;
        mFactory = new BrowserWebViewFactory(context);

    }

    private PreloaderSession getSession(String id) {
        if (mSession == null) {
            mSession = new PreloaderSession(id);
            WebViewTimersControl.getInstance().onPrerenderStart(
                    mSession.getWebView());
            return mSession;
        } else if (mSession.mId.equals(id)) {
            return mSession;
        }

        return null;
    }

    private PreloaderSession takeSession(String id) {
        PreloaderSession s = null;
        if (mSession != null && mSession.mId.equals(id)) {
            s = mSession;
            mSession = null;
        }

        if (s != null) {
            s.cancelTimeout();
        }

        return s;
    }

    public void handlePreloadRequest(String id, String url, Map<String, String> headers,
            String searchBoxQuery) {
        PreloaderSession s = getSession(id);
        if (s == null) {
            return;
        }

        s.touch(); // reset timer
        PreloadedTabControl tab = s.getTabControl();
        if (searchBoxQuery != null) {
            tab.loadUrlIfChanged(url, headers);
            tab.setQuery(searchBoxQuery);
        } else {
            tab.loadUrl(url, headers);
        }
    }

    public void cancelSearchBoxPreload(String id) {
        PreloaderSession s = getSession(id);
        if (s != null) {
            s.touch(); // reset timer
            PreloadedTabControl tab = s.getTabControl();
            tab.searchBoxCancel();
        }
    }

    public void discardPreload(String id) {
        PreloaderSession s = takeSession(id);
        if (s != null) {
            WebViewTimersControl.getInstance().onPrerenderDone(s == null ? null : s.getWebView());
            PreloadedTabControl t = s.getTabControl();
            t.destroy();
        }
    }

    /**
     * Return a preloaded tab, and remove it from the preloader. This is used when the
     * view is about to be displayed.
     */
    public PreloadedTabControl getPreloadedTab(String id) {
        PreloaderSession s = takeSession(id);
        return s == null ? null : s.getTabControl();
    }

    private class PreloaderSession {
        private final String mId;
        private final PreloadedTabControl mTabControl;

        private final Runnable mTimeoutTask = new Runnable(){
            @Override
            public void run() {
                discardPreload(mId);
            }};

        public PreloaderSession(String id) {
            mId = id;
            mTabControl = new PreloadedTabControl(
                    new Tab(new PreloadController(mContext), mFactory.createWebView(BrowserSettings.getInstance().noFooterBrowser())));
            touch();
        }

        public void cancelTimeout() {
            mHandler.removeCallbacks(mTimeoutTask);
        }

        public void touch() {
            cancelTimeout();
            mHandler.postDelayed(mTimeoutTask, PRERENDER_TIMEOUT_MILLIS);
        }

        public PreloadedTabControl getTabControl() {
            return mTabControl;
        }

        public WebView getWebView() {
            Tab t = mTabControl.getTab();
            return t == null? null : t.getMainBrowserWebView();
        }

    }

}

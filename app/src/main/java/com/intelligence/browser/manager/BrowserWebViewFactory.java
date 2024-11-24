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
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.intelligence.browser.webview.BaseWebView;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.base.WebViewFactory;
import com.intelligence.browser.utils.ChannelUtil;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.commonlib.tools.BuildUtil;

public class BrowserWebViewFactory implements WebViewFactory {


    private final Context mContext;

    public BrowserWebViewFactory(Context context) {
        mContext = context;
    }

    protected PreBrowserWebView instantiateWebView() {
        return new PreBrowserWebView(mContext);
    }

    @Override
    public PreBrowserWebView createSubWebView(boolean privateBrowsing) {
        return createWebView(privateBrowsing);
    }

    @Override
    public PreBrowserWebView createWebView(final boolean privateBrowsing) {
        final PreBrowserWebView preBrowserWebView = instantiateWebView();
        preBrowserWebView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
            @Override
            public void call(BaseWebView baseWebView) {
                initWebViewSettings(preBrowserWebView, baseWebView);
            }
        });
        preBrowserWebView.setPrivateBrowsing(privateBrowsing);
        return preBrowserWebView;
    }

    protected void initWebViewSettings(PreBrowserWebView preBrowserWebView, WebView w) {
        w.setScrollbarFadingEnabled(true);
        w.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        w.setMapTrackballToArrowKeys(false); // use trackball directly
        // Enable the built-in zoom
        w.getSettings().setBuiltInZoomControls(true);
        final PackageManager pm = mContext.getPackageManager();
        boolean supportsMultiTouch =
                pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)
                        || pm.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT);
        w.getSettings().setDisplayZoomControls(!supportsMultiTouch);

        // Add this WebView to the settings observer list and update the
        // settings
        final BrowserSettings s = BrowserSettings.getInstance();
        s.startManagingSettings(preBrowserWebView);

        if (Build.VERSION.SDK_INT > BuildUtil.VERSION_CODES.KITKAT) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(w, cookieManager.acceptCookie());
        }

        if (android.os.Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.KITKAT) {
            // Remote Web Debugging is always enabled, where available.
            boolean isOpen = BrowserSettings.getInstance().getOpenDebugStatus();
            if (isOpen || !ChannelUtil.isOpenPlatform()) {
                WebView.setWebContentsDebuggingEnabled(isOpen);
            }
        }
    }

}

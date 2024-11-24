/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.intelligence.browser.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.intelligence.browser.view.ScrollThumb;
import com.intelligence.commonlib.tools.DisplayUtil;

public class BaseWebView extends WebView { // implements FullscreenListener
    private boolean mBackgroundRemoved = false;
    private OnScrollChangedListener mOnScrollChangedListener;
    private WebChromeClient mWebChromeClient;
    private WebViewClient mWebViewClient;
    private boolean mPrivateBrowsing = false;

    private GestureDetector mGestureDetector;
    private ScrollThumb mScrollThumb;
    private OnDestroyListener mOnDestroyListener;

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public BaseWebView(
            Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public BaseWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     */
    public BaseWebView(Context context) {
        super(context);
        init();
    }

    private void init() {

        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (getContentHeight() * getScale() < DisplayUtil.getScreenHeight(getContext()) * 2
                        || !ScrollThumb.checkVelocityThreshold(BaseWebView.this, BaseWebView.this, velocityY)) {
                    //网页长度小于2倍屏幕时，不显示
                    return false;
                }

                if (mScrollThumb == null) {
                    mScrollThumb = new ScrollThumb(BaseWebView.this, BaseWebView.this.getContext(), false);
                    mScrollThumb.setEnabled(true);
                }

                if (!mScrollThumb.isEnabled()) {
                    mScrollThumb.setEnabled(true);
                } else if (!mScrollThumb.isVisible()) {
                    mScrollThumb.setFadingEnabled(true);
                }
                mScrollThumb.awakenScrollThumb(5000, true);
                return true;
            }

        });
    }

    public void setOnDestroyListener(OnDestroyListener onDestroyListener) {
        this.mOnDestroyListener = onDestroyListener;
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        mWebChromeClient = client;
        super.setWebChromeClient(client);
    }

    public WebChromeClient getWebChromeClient() {
        return mWebChromeClient;
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        mWebViewClient = client;
        super.setWebViewClient(client);
    }

    public WebViewClient getWebViewClient() {
        return mWebViewClient;
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (!mBackgroundRemoved && getRootView().getBackground() != null) {
            mBackgroundRemoved = true;
            post(new Runnable() {
                public void run() {
                    getRootView().setBackgroundDrawable(null);
                }
            });
        }
        if (mScrollThumb != null) {
            mScrollThumb.onDrawScrollThumb(c);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
        if (mScrollThumb != null && mScrollThumb.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mScrollThumb != null && mScrollThumb.onInterceptTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void drawContent(Canvas c) {
        draw(c);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        }

        // webview的高度
        float webcontent = getContentHeight() * getScale();
        // 当前webview的高度
        float webnow = getHeight() + getScrollY();
        if (!canScrollVertically(1)) {
            //处于底端
            if(mOnScrollChangedListener != null) {
                mOnScrollChangedListener.onPageTop(false);
            }
        } else if (getScrollY() == 0 && !canScrollVertically(-1)) {
            //处于顶端
            if (mOnScrollChangedListener != null) {
                mOnScrollChangedListener.onPageTop(true);
            }
        } else {
            if(mOnScrollChangedListener != null) {
                mOnScrollChangedListener.onPageTop(false);
            }
//            mOnScrollChangeListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mOnDestroyListener != null) {
            mOnDestroyListener.onDestroy();
        }
    }

    @Override
    public boolean isPrivateBrowsingEnabled() {
        return mPrivateBrowsing;
    }


    /* Make sure the local webview remains in sync, since the engine won't retain that data */
    public void setPrivateBrowsing(boolean state) {
        mPrivateBrowsing = state;
        if (state) {
            // Disable ALL the things
            this.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//            this.getSettings().setAppCacheEnabled(false);
            this.getSettings().setDatabaseEnabled(false);
            this.getSettings().setGeolocationEnabled(false);
            this.getSettings().setSaveFormData(false);
            this.getSettings().setSavePassword(false);
            this.getSettings().setSupportMultipleWindows(false);
//            this.getSettings().setAppCacheMaxSize(0);
            this.clearHistory();
        }
    }



    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
        void onPageTop(boolean istop);
    }

    public interface OnDestroyListener {
        void onDestroy();
    }

}

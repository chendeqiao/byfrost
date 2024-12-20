package com.intelligence.browser.ui.webview;

import android.webkit.JavascriptInterface;

public class JsInterfaceInject {
    private WebviewVideoPlayerLayer.MediaInfoListener mListener;

    @JavascriptInterface
    public void getVideoErrorInfo(String info) {
        if (mListener != null) {
            mListener.getVideoErrorInfo(info);
        }
    }

    @JavascriptInterface
    public void getMediaDuration(String duration) {
        if (mListener != null) {
            mListener.getMediaDuration(duration);
        }
    }

    @JavascriptInterface
    public void getVideoUrl(String url,String src) {
        if (mListener != null) {
            mListener.getVideoUrl(url,src);
        }
    }

    @JavascriptInterface
    public void getCurrentPlayTime(String time) {
        if (mListener != null) {
            mListener.getCurrentPlayTime(time);
        }
    }

    @JavascriptInterface
    public void canGetVideoElement(boolean success) {
        if (mListener != null) {
            mListener.canControlVideoPlay(success);
        }
    }

    @JavascriptInterface
    public void isPaused(boolean isPaused) {
        if (mListener != null) {
            mListener.isPaused(isPaused);
        }
    }

    @JavascriptInterface
    public void initMediaControlsProgressBar() {
        if (mListener != null) {
            mListener.initMediaControlsProgressBar();
        }
    }

    @JavascriptInterface
    public void played() {
        if (mListener != null) {
            mListener.played();
        }
    }

    @JavascriptInterface
    public void paused() {
        if (mListener != null) {
            mListener.paused();
        }
    }

    public void setListener(WebviewVideoPlayerLayer.MediaInfoListener listener) {
        mListener = listener;
    }

}

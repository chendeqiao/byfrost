/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.intelligence.browser.ui.media;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.webview.WebviewVideoPlayerLayer;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.download.util.FileUtils;
import com.intelligence.browser.ui.BaseActivity;

public class VideoActivity extends BaseActivity implements FullWebviewVideoToolLayer.VideoPlayListener {
    private String mVideoUrls;
    private String mVideoTitle;
    public static final String VIDEO_URL_PRAMAS = "Extras";
    public static final String VIDEO_TITLE_PRAMAS = "Title";
    public static final String VIDEO_FROM_NET = "from";

    private FullWebviewVideoToolLayer mToolLayer;
    private FullVideoScreenHolder mFullscreenContainer;
    private View mVideoLoading;

    private View mVideoLoadingIcon;
    private boolean isMusic;

    private BrowserVideoView mBrowserVideoView;
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.browser_video_page);
        initView();
        initData();
        initParams();

    }

    private void initParams(){
        int videoType = BrowserSettings.getInstance().getUserVideo();
        if (videoType == 0 && !isMusic) {
            boolean isLand = FileUtils.getVideoRatio(mVideoUrls);
            if (isLand) {
                ScreenUtils.setLandscape(this);
            } else {
                ScreenUtils.setPortrait(this);
            }
        } else if (videoType == 1 && !isMusic) {
            ScreenUtils.setLandscape(this);
        } else {
            ScreenUtils.setPortrait(this);
        }
        mVideoTitle = getIntent().getStringExtra(VIDEO_TITLE_PRAMAS);
    }

    private void initView(){
        mBrowserVideoView = findViewById(R.id.videoview);
        updateStatusBarState(true);
        mFullscreenContainer  = findViewById(R.id.full_video_holder);
        mVideoLoading  = findViewById(R.id.video_loading);
        mVideoLoadingIcon  = findViewById(R.id.video_loading_icon);
        Animation rotateAnimation = AnimationUtils.loadAnimation(VideoActivity.this, R.anim.browser_video_loading_progress_black);
        mVideoLoadingIcon.startAnimation(rotateAnimation);

        mFullscreenContainer.setFocusable(true);
        mFullscreenContainer.setFocusableInTouchMode(true);
        mFullscreenContainer.requestFocus();
        mToolLayer = new FullWebviewVideoToolLayer(VideoActivity.this, mBrowserVideoView,mVideoTitle,this);
        mToolLayer.setPreView(mBrowserVideoView);
        mToolLayer.setListener((WebviewVideoPlayerLayer.Listener) mFullscreenContainer);

        mFullscreenContainer.addView(mToolLayer.getLayer(), COVER_SCREEN_PARAMS);
        mFullscreenContainer.setKeepScreenOn(true);
    }

    private void initData() {
        mVideoUrls = getIntent().getStringExtra(VIDEO_URL_PRAMAS);
        if (!TextUtils.isEmpty(mVideoUrls)) {
            Uri videoUri = Uri.parse(mVideoUrls);
            mBrowserVideoView.setVideoURI(videoUri);
        } else {
            // 获取 Intent
            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();
            String type = intent.getType();

            isMusic = type.startsWith("audio/");
            if (Intent.ACTION_VIEW.equals(action) && data != null) {
                mBrowserVideoView.setVideoURI(data);
            }
        }
    }

    public void updateStatusBarState(boolean enabled) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onPlayFinish() {
    }

    @Override
    public void onStartPlay() {
        if(!isMusic) {
            mVideoLoading.clearAnimation();
            mVideoLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBack() {
        ScreenUtils.setPortrait(this);
        if(mBrowserVideoView != null){
            mBrowserVideoView.pause();
        }
        this.finish();
    }

    @Override
    public void setRequestedOrientation() {
        int originalOrientation = getRequestedOrientation();
        if (originalOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            ScreenUtils.setPortrait(this);
            if (mToolLayer != null) {
                mToolLayer.hideShade();
            }
        } else {
            ScreenUtils.setLandscape(this);
        }
    }

}

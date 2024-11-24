/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.intelligence.componentlib.photoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.media.PhotoInfo;


@SuppressWarnings("unused")
public class PhotoItemView extends FrameLayout {
    private PhotoView mPhotoView;
    private ImageView mLoadingView;
    private ImageView mErrorView;

    public PhotoItemView(Context context) {
        this(context, null);
    }

    public PhotoItemView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoItemView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.browser_photo_item_view, this);
        mPhotoView = findViewById(R.id.photo_view);
        mLoadingView = findViewById(R.id.loading_view);
        mErrorView = findViewById(R.id.error_view);

    }

    public void setImageUrl(PhotoInfo photoInfo, OnClickListener onClickListener) {
        mPhotoView.setOnClickListener(onClickListener);
        mPhotoView.setImageResource(0);
        mPhotoView.setBackground(null);
        mPhotoView.setImageDrawable(null);
        mLoadingView.setVisibility(VISIBLE);
        mLoadingView.setImageDrawable(getResources().getDrawable(R.drawable.browser_app_small_icon));
        mErrorView.setVisibility(GONE);
        Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.browser_video_loading_progress_black);
        mLoadingView.startAnimation(rotateAnimation);
        mPhotoView.setImageUrl(photoInfo.mImageUrl, new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                mLoadingView.setImageResource(0);
                mLoadingView.setBackground(null);
                mLoadingView.setImageDrawable(null);
                mLoadingView.setVisibility(GONE);
                mLoadingView.clearAnimation();
                mErrorView.setVisibility(VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                mErrorView.setVisibility(GONE);
                mLoadingView.setImageResource(0);
                mLoadingView.setBackground(null);
                mLoadingView.setImageDrawable(null);
                mLoadingView.setVisibility(GONE);
                mLoadingView.clearAnimation();
                return false;
            }
        });
    }
}

package com.intelligence.browser.ui.media;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

public class BrowserVideoView extends android.widget.VideoView {

	private int videoWidth;
	private int videoHeight;

	public BrowserVideoView(Context context) {
		super(context);
	}

	public BrowserVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BrowserVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}

}

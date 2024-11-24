package com.intelligence.componentlib.photoview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class HackyViewPager extends ViewPager {
    private boolean isPagingEnabled = true;
	
    public HackyViewPager(Context context) {
        super(context);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isPagingEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			return isPagingEnabled &&  super.onInterceptTouchEvent(ev);
		} catch (IllegalArgumentException e) {
			return false;
		}
    }

    // Method to enable or disable paging
    public void setPagingEnabled(boolean enabled) {
        this.isPagingEnabled = enabled;
    }
}

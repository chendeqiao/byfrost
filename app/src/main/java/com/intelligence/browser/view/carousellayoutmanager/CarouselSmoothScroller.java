package com.intelligence.browser.view.carousellayoutmanager;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearSmoothScroller;

public abstract class CarouselSmoothScroller extends LinearSmoothScroller {
    private Context mContext;
    protected CarouselSmoothScroller(final Context context) {
        super(context);
        mContext = context;
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public int calculateDyToMakeVisible(final View view, final int snapPreference) {
        final CarouselLayoutManager layoutManager = (CarouselLayoutManager) getLayoutManager();
        if (null == layoutManager || !layoutManager.canScrollVertically()) {
            return 0;
        }

        return layoutManager.getOffsetForCurrentView(view);
    }

    @SuppressWarnings("RefusedBequest")
    @Override
    public int calculateDxToMakeVisible(final View view, final int snapPreference) {
        final CarouselLayoutManager layoutManager = (CarouselLayoutManager) getLayoutManager();
        if (null == layoutManager || !layoutManager.canScrollHorizontally()) {
            return 0;
        }
        return layoutManager.getOffsetForCurrentView(view);
    }

    @Override
    protected int calculateTimeForScrolling(int dx) {
        return (int) Math.ceil(Math.abs(dx) * 40.0f / mContext.getResources().getDisplayMetrics().densityDpi);
    }
}

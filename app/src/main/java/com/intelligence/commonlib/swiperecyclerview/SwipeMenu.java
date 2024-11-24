package com.intelligence.commonlib.swiperecyclerview;

import android.content.Context;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SwipeMenu {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private SwipeMenuLayout mSwipeMenuLayout;
    private int mViewType;
    private int orientation;
    private List<SwipeMenuItem> mSwipeMenuItems;

    SwipeMenu(SwipeMenuLayout swipeMenuLayout, int viewType) {
        this.mSwipeMenuLayout = swipeMenuLayout;
        this.mViewType = viewType;
        this.mSwipeMenuItems = new ArrayList(2);
    }

    public void setOpenPercent(float openPercent) {
        if (openPercent != this.mSwipeMenuLayout.getOpenPercent()) {
            openPercent = openPercent > 1.0F ? 1.0F : (openPercent < 0.0F ? 0.0F : openPercent);
            this.mSwipeMenuLayout.setOpenPercent(openPercent);
        }

    }

    public void setScrollerDuration(int scrollerDuration) {
        this.mSwipeMenuLayout.setScrollerDuration(scrollerDuration);
    }

    public void setOrientation(int orientation) {
        if (orientation != 0 && orientation != 1) {
            throw new IllegalArgumentException("Use SwipeMenu#HORIZONTAL or SwipeMenu#VERTICAL.");
        } else {
            this.orientation = orientation;
        }
    }

    public int getOrientation() {
        return this.orientation;
    }

    public void addMenuItem(SwipeMenuItem item) {
        this.mSwipeMenuItems.add(item);
    }

    public void removeMenuItem(SwipeMenuItem item) {
        this.mSwipeMenuItems.remove(item);
    }

    public List<SwipeMenuItem> getMenuItems() {
        return this.mSwipeMenuItems;
    }

    public SwipeMenuItem getMenuItem(int index) {
        return this.mSwipeMenuItems.get(index);
    }

    public Context getContext() {
        return this.mSwipeMenuLayout.getContext();
    }

    public int getViewType() {
        return this.mViewType;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }
}

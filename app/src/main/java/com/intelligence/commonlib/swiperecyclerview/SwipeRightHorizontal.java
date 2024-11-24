package com.intelligence.commonlib.swiperecyclerview;

import android.view.View;
import android.widget.OverScroller;

class SwipeRightHorizontal extends SwipeHorizontal {
    public SwipeRightHorizontal(View menuView) {
        super(-1, menuView);
    }

    public boolean isMenuOpen(int scrollX) {
        int i = -this.getMenuView().getWidth() * this.getDirection();
        return scrollX >= i && i != 0;
    }

    public boolean isMenuOpenNotEqual(int scrollX) {
        return scrollX > -this.getMenuView().getWidth() * this.getDirection();
    }

    public void autoOpenMenu(OverScroller scroller, int scrollX, int duration) {
        scroller.startScroll(Math.abs(scrollX), 0, this.getMenuView().getWidth() - Math.abs(scrollX), 0, duration);
    }

    public void autoCloseMenu(OverScroller scroller, int scrollX, int duration) {
        scroller.startScroll(-Math.abs(scrollX), 0, Math.abs(scrollX), 0, duration);
    }

    public Checker checkXY(int x, int y) {
        this.mChecker.x = x;
        this.mChecker.y = y;
        this.mChecker.shouldResetSwipe = this.mChecker.x == 0;

        if (this.mChecker.x < 0) {
            this.mChecker.x = 0;
        }

        if (this.mChecker.x > this.getMenuView().getWidth()) {
            this.mChecker.x = this.getMenuView().getWidth();
        }

        return this.mChecker;
    }

    public boolean isClickOnContentView(int contentViewWidth, float x) {
        return x < (float)(contentViewWidth - this.getMenuView().getWidth());
    }
}

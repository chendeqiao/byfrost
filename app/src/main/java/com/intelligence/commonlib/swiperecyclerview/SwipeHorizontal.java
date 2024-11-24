package com.intelligence.commonlib.swiperecyclerview;

import android.view.View;
import android.widget.OverScroller;

abstract class SwipeHorizontal {
    private int direction;
    private View menuView;
    protected SwipeHorizontal.Checker mChecker;

    public SwipeHorizontal(int direction, View menuView) {
        this.direction = direction;
        this.menuView = menuView;
        this.mChecker = new SwipeHorizontal.Checker();
    }

    public abstract boolean isMenuOpen(int var1);

    public abstract boolean isMenuOpenNotEqual(int var1);

    public abstract void autoOpenMenu(OverScroller var1, int var2, int var3);

    public abstract void autoCloseMenu(OverScroller var1, int var2, int var3);

    public abstract SwipeHorizontal.Checker checkXY(int var1, int var2);

    public abstract boolean isClickOnContentView(int var1, float var2);

    public int getDirection() {
        return this.direction;
    }

    public View getMenuView() {
        return this.menuView;
    }

    public int getMenuWidth() {
        return this.menuView.getWidth();
    }

    public static final class Checker {
        public int x;
        public int y;
        public boolean shouldResetSwipe;

        public Checker() {
        }
    }
}

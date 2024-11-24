package com.intelligence.browser.view;

import android.view.MotionEvent;

public interface OnCustomGestureListener {
    boolean onDown(MotionEvent e);
    void onUp(MotionEvent event);
    void onMove(MotionEvent event);
    void onDoubleTap(MotionEvent e);
    boolean onSingleTapUp(MotionEvent e);
    boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
    void onLongPress(MotionEvent e);
    boolean onTouch(MotionEvent event);
}

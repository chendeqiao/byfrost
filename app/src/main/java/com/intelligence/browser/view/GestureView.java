package com.intelligence.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class GestureView extends RelativeLayout {

    public GestureView(Context context) {
        super(context);
        init(context);
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                if (mSimpleOnGestureListener != null) {
                    mSimpleOnGestureListener.onDown(e);
                }
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mSimpleOnGestureListener != null) {
                    mSimpleOnGestureListener.onSingleTapUp(e);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mSimpleOnGestureListener != null) {
                    mSimpleOnGestureListener.onScroll(e1, e2, distanceX, distanceY);
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (mSimpleOnGestureListener != null) {
                    mSimpleOnGestureListener.onLongPress(e);
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    public void setSimpleOnGestureListener(OnCustomGestureListener simpleOnGestureListener) {
        mSimpleOnGestureListener = simpleOnGestureListener;
    }

    private OnCustomGestureListener mSimpleOnGestureListener;
    private GestureDetector mGestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isOutScreenTouch = false;
        if (mSimpleOnGestureListener != null) {
            isOutScreenTouch = mSimpleOnGestureListener.onTouch(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mSimpleOnGestureListener != null) {
                mSimpleOnGestureListener.onDown(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mSimpleOnGestureListener != null) {
                mSimpleOnGestureListener.onUp(event);
            }
        }
        if(isOutScreenTouch){
            return false;
        }
        mGestureDetector.onTouchEvent(event);
        return false;
    }
}


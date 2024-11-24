package com.intelligence.browser.ui.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.SchemeUtil;

public class BrowserBottomArrow extends LinearLayout implements ValueAnimator.AnimatorUpdateListener {


    public BrowserBottomArrow(Context context) {
        super(context);
        init();
    }

    public BrowserBottomArrow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BrowserBottomArrow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BrowserBottomArrow(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.browser_bottom_arrow, this);
        setOrientation(VERTICAL);
        setClipChildren(false);
    }

    ValueAnimator animator;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setAlpha(0.5f);
        }

        int[] as = new int[getChildCount() + 1];
        for (int i = 0; i < getChildCount() + 1; i++) {
            as[i] = i;
        }
        animator = ValueAnimator.ofInt(as);
        animator.setDuration(450 * getChildCount());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(this);
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    private float mDragLastTouchY;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragLastTouchY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float dy = ev.getY() - mDragLastTouchY;
                if (Math.abs(dy) > 10) {
                    SchemeUtil.jumpToScheme(getContext(),SchemeUtil.BROWSER_SCHEME_PATH_NEWS);
                }
                break;
            case MotionEvent.ACTION_UP:
                SchemeUtil.jumpToScheme(getContext(),SchemeUtil.BROWSER_SCHEME_PATH_NEWS);
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (i == value) {
                getChildAt(i).setAlpha(0.70f);
            } else {
                getChildAt(i).setAlpha(0.15f);
            }
        }
    }
}

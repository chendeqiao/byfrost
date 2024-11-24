package com.intelligence.browser.utils;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;

import com.intelligence.commonlib.tools.ScreenUtils;

public class AnimationUtils {

    private static final int COLOR_OFFSET = 0xff;
    private static final int COLOR_OFFSET_R = 16;
    private static final int COLOR_OFFSET_G = 8;
    private static final int COLOR_OFFSET_B = 4;
    private static final int ALPHA_OFFSET =24;
    private static final float ALPHA_INIT = 0f;

    /**
     * this is alpha init
     */
    public static void setChildAlpha(ViewGroup viewGroup) {
        viewGroup.setAlpha(ALPHA_INIT);
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                setChildAlpha((ViewGroup) child);
            } else {
                child.setAlpha(ALPHA_INIT);
            }
        }
    }

    /**
     * this is color gradient
     */
    public static int getColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> ALPHA_OFFSET) & COLOR_OFFSET;
        int startR = (startValue >> COLOR_OFFSET_R) & COLOR_OFFSET;
        int startG = (startValue >> COLOR_OFFSET_G) & COLOR_OFFSET;
        int startB = startValue & COLOR_OFFSET;

        int endA = (endValue >> ALPHA_OFFSET) & COLOR_OFFSET;
        int endR = (endValue >> COLOR_OFFSET_R) & COLOR_OFFSET;
        int endG = (endValue >> COLOR_OFFSET_G) & COLOR_OFFSET;
        int endB = endValue & COLOR_OFFSET;

        return (startA + (int) (fraction * (endA - startA))) << ALPHA_OFFSET |
                ((startR + (int) (fraction * (endR - startR))) << COLOR_OFFSET_R) |
                ((startG + (int) (fraction * (endG - startG))) << COLOR_OFFSET_G) |
                ((startB + (int) (fraction * (endB - startB))));
    }


    public static void startShakeAndRotateAnimation(View view) {
        view.setPivotX(ScreenUtils.dpToPx(view.getContext(),10));  // X轴居中
        view.setPivotY(ScreenUtils.dpToPx(view.getContext(),35));      // Y轴底部

        // 旋转动画
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, -3f, 3f,0);
        rotateAnimator.setDuration(500);
        rotateAnimator.setRepeatCount(1);
        rotateAnimator.setInterpolator(new CycleInterpolator(1));

        rotateAnimator.start();
    }
}

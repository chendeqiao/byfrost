package com.intelligence.browser.view.moplbutton;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IntegerRes;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.DisplayUtil;

/**
 * Created by Wenhao on 2016/10/11.
 */

public class MorphAction {

    public static void morphToSquare(Context context, final MorphingButton btnMorph, int duration, int stringResId) {
        MorphingButton.Params square = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(dimen(context, R.dimen.mb_height_56))
                .width(dimen(context, R.dimen.mb_width_237))
                .height(dimen(context, R.dimen.mb_height_56))
                .color(color(context, R.color.mb_blue))
                .colorPressed(color(context, R.color.mb_blue_dark))
                .text(context.getString(stringResId));
        btnMorph.morph(square);
    }

    public static void morphToFailure(Context context, final MorphingButton btnMorph, int duration) {
        MorphingButton.Params circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(dimen(context, R.dimen.mb_height_56))
                .width(dimen(context, R.dimen.mb_height_56))
                .height(dimen(context, R.dimen.mb_height_56))
                .color(color(context, R.color.mb_blue))
                .colorPressed(color(context, R.color.mb_blue_dark))
                .icon(R.drawable.browser_history_fab_deleted);
        btnMorph.morph(circle);
    }

    public static int dimen(Context context, @DimenRes int resId) {
        return (int) context.getResources().getDimension(resId);
    }

    public static int color(Context context, @ColorRes int resId) {
        return context.getResources().getColor(resId);
    }

    public static int integer(Context context, @IntegerRes int resId) {
        return context.getResources().getInteger(resId);
    }

    public static void morphMoveRotation(final MorphingButton btnMorph, int duration) {
        PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("rotation", 300f, 360f);
        PropertyValuesHolder p3 = PropertyValuesHolder.ofFloat("translationY", (float) DisplayUtil.dip2px(btnMorph
                .getContext(), btnMorph.getResources().getDimension(R.dimen.mb_width_100)), 0f);
        ObjectAnimator.ofPropertyValuesHolder(btnMorph, p1, p3).setDuration(duration).start();
    }

    public static void morphMove(final MorphingButton btnMorph, int duration) {
        PropertyValuesHolder p3 = PropertyValuesHolder.ofFloat("translationY", (float) DisplayUtil.dip2px(btnMorph
                .getContext(), btnMorph.getResources().getDimension(R.dimen.mb_width_100)), 0f);
        ObjectAnimator.ofPropertyValuesHolder(btnMorph, p3).setDuration(duration).start();
    }

    public static void morphMoveOut(final MorphingButton btnMorph, int duration) {
        float bottom = btnMorph.getResources().getDimension(R.dimen.mb_width_100);
        PropertyValuesHolder p = PropertyValuesHolder.ofFloat("translationY", 0f, (float) DisplayUtil.dip2px(btnMorph
                .getContext(), bottom));
        ObjectAnimator.ofPropertyValuesHolder(btnMorph, p).setDuration(duration).start();
    }

    public static void morphMoveOutRotation(final MorphingButton btnMorph, int duration) {
        PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("rotation", 360f, 300f);
        PropertyValuesHolder p = PropertyValuesHolder.ofFloat("translationY", 0f, (float) DisplayUtil.dip2px(btnMorph
                .getContext(), btnMorph.getResources().getDimension(R.dimen.mb_width_100)));
        ObjectAnimator.ofPropertyValuesHolder(btnMorph, p, p1).setDuration(duration).start();
    }
}

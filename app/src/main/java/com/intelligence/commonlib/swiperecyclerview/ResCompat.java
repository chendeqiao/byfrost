package com.intelligence.commonlib.swiperecyclerview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;

public class ResCompat {
    public ResCompat() {
    }

    public static Drawable getDrawable(Context context, int drawableId) {
        return getDrawable(context, drawableId, null);
    }

    public static Drawable getDrawable(Context context, int drawableId, Theme theme) {
        Resources resources = context.getResources();
        Class<?> resourcesClass = resources.getClass();
        Method getDrawableMethod;
        if (VERSION.SDK_INT >= 21) {
            try {
                getDrawableMethod = resourcesClass.getMethod("getDrawable", Integer.TYPE, Theme.class);
                getDrawableMethod.setAccessible(true);
                return (Drawable)getDrawableMethod.invoke(resources, drawableId, theme);
            } catch (Throwable var6) {
            }
        } else {
            try {
                getDrawableMethod = resourcesClass.getMethod("getDrawable", Integer.TYPE);
                getDrawableMethod.setAccessible(true);
                return (Drawable)getDrawableMethod.invoke(resources, drawableId);
            } catch (Throwable var7) {
            }
        }

        return null;
    }

    public static int getColor(Context context, int colorId) {
        return getColor(context, colorId, null);
    }

    public static int getColor(Context context, int colorId, Theme theme) {
        Resources resources = context.getResources();
        Class<?> resourcesClass = resources.getClass();
        Method getColorMethod;
        if (VERSION.SDK_INT >= 23) {
            try {
                getColorMethod = resourcesClass.getMethod("getColor", Integer.TYPE, Theme.class);
                getColorMethod.setAccessible(true);
                return (Integer)getColorMethod.invoke(resources, colorId, theme);
            } catch (Throwable var6) {
            }
        } else {
            try {
                getColorMethod = resourcesClass.getMethod("getColor", Integer.TYPE);
                getColorMethod.setAccessible(true);
                return (Integer)getColorMethod.invoke(resources, colorId);
            } catch (Throwable var7) {
            }
        }

        return -16777216;
    }

    public static void setBackground(View view, int drawableId) {
        setBackground(view, getDrawable(view.getContext(), drawableId));
    }

    public static void setBackground(View view, Drawable background) {
        if (VERSION.SDK_INT >= 16) {
            setBackground("setBackground", view, background);
        } else {
            setBackground("setBackgroundDrawable", view, background);
        }

    }

    public static void setBackground(String method, View view, Drawable background) {
        try {
            Method viewMethod = view.getClass().getMethod(method, Drawable.class);
            viewMethod.setAccessible(true);
            viewMethod.invoke(view, background);
        } catch (Throwable var4) {
        }

    }

    public static void setTextAppearance(TextView view, int textAppearance) {
        Class<?> resourcesClass = view.getClass();
        Method getColorMethod;
        if (VERSION.SDK_INT >= 23) {
            try {
                getColorMethod = resourcesClass.getMethod("setTextAppearance", Context.class, Integer.TYPE);
                getColorMethod.setAccessible(true);
                getColorMethod.invoke(view, view.getContext(), textAppearance);
            } catch (Throwable var5) {
            }
        } else {
            try {
                getColorMethod = resourcesClass.getMethod("setTextAppearance", Integer.TYPE);
                getColorMethod.setAccessible(true);
                getColorMethod.invoke(view, textAppearance);
            } catch (Throwable var4) {
            }
        }

    }
}

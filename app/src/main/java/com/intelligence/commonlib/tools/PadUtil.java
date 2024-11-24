package com.intelligence.commonlib.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

public class PadUtil {
    private String TAG = "pad-a";
    public static float sPadAdaptWindowRatioHW = 1.1267606F;

    public PadUtil() {
    }

    public static int getPadLandscapeWidth(Context context) {
        int deviceH;
        if (ScreenUtils.isPortrait(context)) {
            deviceH = ScreenUtils.getRealScreenWidth(context);
        } else {
            deviceH = ScreenUtils.getRealScreenHeight(context);
        }

        return (int)((float)deviceH / sPadAdaptWindowRatioHW + 0.5F);
    }

    private static int getOrientation(String tagName) {
        if ("portrait".equals(tagName)) {
            return 1;
        } else if ("landscape".equals(tagName)) {
            return 0;
        } else if ("user".equals(tagName)) {
            return 2;
        } else if ("behind".equals(tagName)) {
            return 3;
        } else if ("sensor".equals(tagName)) {
            return 4;
        } else if ("nosensor".equals(tagName)) {
            return 5;
        } else if ("sensorLandscape".equals(tagName)) {
            return 6;
        } else if ("sensorPortrait".equals(tagName)) {
            return 7;
        } else if ("reverseLandscape".equals(tagName)) {
            return 8;
        } else if ("reversePortrait".equals(tagName)) {
            return 9;
        } else if ("fullSensor".equals(tagName)) {
            return 10;
        } else if ("userLandscape".equals(tagName)) {
            return 11;
        } else if ("userPortrait".equals(tagName)) {
            return 12;
        } else if ("fullUser".equals(tagName)) {
            return 13;
        } else {
            return "locked".equals(tagName) ? 14 : -1;
        }
    }

    public static int getWhiteSpaceWidth(Activity activity) {
        if (activity == null) {
            return 0;
        } else {
            return isPad(activity) && ScreenUtils.isLandscape(activity) ? (ScreenUtils.getRealScreenWidth(activity) - ScreenUtils.getScreenWidth(activity)) / 2 : 0;
        }
    }

    public static boolean isPad(Context context) {
        if (context == null) {
            return false;
        } else if (isFoldable(context)) {
            return false;
        } else {
            return getScreenLayout(context) >= 3;
        }
    }

    public static boolean isPhone(Context context) {
        return !isPad(context) && !isFoldable(context);
    }

    public static boolean isFoldable(Context context) {
        return isFoldDevice();
    }

    private static boolean isFoldDevice() {
        String brand = Build.BRAND.toLowerCase();
        if (brand.equals("huawei")) {
            return Build.MODEL.equals("RLI-AN00") || Build.MODEL.equals("RLI-N29") || Build.MODEL.equals("TAH-N29") || Build.MODEL.equals("TAH-AN00") || Build.MODEL.equals("TAH-AN00m") || Build.MODEL.equals("RHA-AN00m");
        } else return brand.equals("samsung") && Build.MODEL.equals("SM-F9000");

    }

    public static boolean isFoldStatus(Context context) {
        if (context == null) {
            return false;
        } else {
            Configuration config = context.getApplicationContext().getResources().getConfiguration();
            float ratio = 1.0F * (float)config.screenWidthDp / (float)config.screenHeightDp;
            return !(ratio >= 0.75F) || !(ratio <= 1.3333334F);
        }
    }

    public static String getActivityName(Activity activity) {
        return activity.getClass().getName();
    }

    public static int getScreenLayout(Context context) {
        return context == null ? -1 : context.getApplicationContext().getResources().getConfiguration().screenLayout & 15;
    }

    private int getOrientation(Context context) {
        return context == null ? 1 : context.getApplicationContext().getResources().getConfiguration().orientation;
    }

    public static int getCommonCustomPageWidth(Activity activity) {
        if (Build.VERSION.SDK_INT <= 26) {
            return ScreenUtils.isLandscape(activity) ? ScreenUtils.getRealScreenHeight(activity) : ScreenUtils.getRealScreenWidth(activity);
        } else {
            return 0;
        }
    }

    public static class ActivityFoldState {
        public int ori;
        public int screenLayout;

        public ActivityFoldState() {
        }
    }
}

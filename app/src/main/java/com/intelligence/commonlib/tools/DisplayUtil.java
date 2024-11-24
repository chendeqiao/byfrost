package com.intelligence.commonlib.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.DimenRes;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;

import java.lang.reflect.Method;

public class DisplayUtil {

    private static int TAB_COUNT_TO_CHANGE_SIZE = 10;
    public static float DEFAULT_BRIGHTNESS = -1.0f;

//    /**
//     * 如果当前的处于夜间模式就会变更屏幕亮度
//     *
//     * @param activity
//     */
    public static void changeScreenBrightnessIfNightMode(Activity activity) {
        if(BrowserSettings.getInstance() == null){
            return;
        }
        float value = BrowserSettings.getInstance().getBrightness();
        if (BrowserSettings.getInstance().getNightMode() && value != DEFAULT_BRIGHTNESS) {
            setScreenBrightness(activity, BrowserSettings.getInstance().getBrightness());
        }
    }

    /**
     * 将dip 或dp值转换为px值，保证尺寸大小不变
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int dp2px(Context context, float dipValue) {
        return dip2px(context,dipValue);
    }

    private static int sRealHeight;

    public static int getRealSize(Context context) {
        if (context != null) {
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            try {
                Class<?> disPlayClass = Class.forName("android.view.Display");
                Point realSize = new Point();
                Method method = disPlayClass.getMethod("getRealSize", Point.class);
                method.invoke(display, realSize);
                sRealHeight = realSize.y;
            } catch (Exception e) {
                sRealHeight = getScreenHeight(context);
            }
        }
        return sRealHeight;
    }

    /**
     * 获取底部导航栏高度
     *
     * @param context
     * @return
     */
    public static int getNavBarHeight(Context context) {
        return getRealSize(context) - getScreenHeight(context);
    }

    /**
     * 获取屏幕的宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        return ScreenUtils.getScreenWidth(context);
//        DisplayMetrics dm = new DisplayMetrics();
//        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        manager.getDefaultDisplay().getMetrics(dm);
//        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        return ScreenUtils.getScreenHeight(context);
//        DisplayMetrics dm = new DisplayMetrics();
//        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        manager.getDefaultDisplay().getMetrics(dm);
//        return dm.heightPixels;
    }

    public static int getDimenPxValue(Context context, @DimenRes int dimenRes) {
        return context.getResources().getDimensionPixelSize(dimenRes);
    }

    /**
     * 获取DisplayMetrics
     *
     * @param context
     * @return
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static void resetTabSwitcherTextSize(TextView tv, int count) {
        int textSize;
        if (count < TAB_COUNT_TO_CHANGE_SIZE) {
            textSize = tv.getContext().getResources().getInteger(R.integer.tab_count_less_than_ten);
        } else {
            textSize = tv.getContext().getResources().getInteger(R.integer.tab_count_greater_than_ten);
        }

        String tabCount = (count <= 0 ? 1 : count) + "";

        tv.setTextSize(textSize);
        tv.setText(tabCount);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static boolean isScreenPortrait(Context context) {
        Configuration configuration = context.getResources().getConfiguration(); //获取设置的配置信息
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT; //获取屏幕方向
    }

    /**
     * 设置屏幕亮度
     *
     * @param activity
     * @param brightness 0 最暗　1 最亮
     */
    public static void setScreenBrightness(Activity activity, float brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 获取屏幕亮度
     *
     * @param activity
     */
    public static float getScreenBrightness(Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        return lp.screenBrightness;
    }

    /**
     * 用于没有保存设置亮度时，获取当前系统亮度值0-255，为了适配版本升级转化为float(0-1)
     *
     * @param activity
     * @return
     */
    public static float getSystemBrightness(Activity activity) {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness / (float) 255;
    }
}

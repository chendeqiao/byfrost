package com.intelligence.commonlib.tools;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.intelligence.browser.R;

import java.lang.reflect.Field;

public class ScreenUtils {

    public static int sCustomScreenWidth;
    public static int sCustomScreenHeight;

    /**
     * dp转px  返回float类型
     *
     * @param context 上下文环境
     * @param dp      要转换的dp值
     * @return float类型
     */
    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        //插件框架内部不稳定，getResources()方法被重写后，getPackageName在静态调用时为null导致崩溃
        //return dp * context.getResources().getDisplayMetrics().density;
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * dp转px  返回int类型
     *
     * @param context
     * @param dp      要转换的dp值
     * @return int类型
     */
    public static int dpToPxInt(Context context, float dp) {
        return (int) (dpToPx(context, dp) + 0.5f);
    }

    /**
     * dp转px  返回int类型
     *
     * @param context
     * @param dp      要转换的dp值
     * @return int类型
     */
    public static int dpToPxIntRound(Context context, float dp) {
        return Math.round(dpToPx(context, dp) + 0.5f);
    }

    /**
     * sp转px
     *
     * @param context 上下文环境
     * @param spValue 要转换的spValue值
     * @return int类型
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context 上下文环境
     * @param px      要转换的px值
     * @return float类型
     */
    public static float pxToDp(Context context, float px) {
        if (context == null || px == 0) {
            return -1;
        }
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * px转dp
     *
     * @param context 上下文环境
     * @param px      要转换的px值
     * @return int类型
     */
    public static int pxToDpInt(Context context, float px) {
        return (int) (pxToDp(context, px) + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context 上下文环境
     * @param px      要转换的px值
     * @return int类型
     */
    public static int pxToDpIntRound(Context context, float px) {
        return Math.round(pxToDp(context, px) + 0.5f);
    }

    /**
     * 获取 status_bar_height 的高度
     *
     * @param mContext 上下文环境
     * @return int类型
     */
    public static int getStatusBarHeight(Context mContext) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = mContext.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }
    /**
     * 获取ActionBar高度
     *
     * @param activity activity
     * @return ActionBar高度
     */
    public static int getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * 获取手机屏幕宽度
     *
     * @param context 上下文环境
     * @return int类型
     */
    public static int getScreenWidth(Context context) {
        // pad适配后，屏幕宽度和页面有关，需要由页面自己指定
        if (context instanceof IPadScreenAdapter) {
            IPadScreenAdapter adapter = ((IPadScreenAdapter)context);
            if (adapter.usePageAdaptSize()) {
                int cWidth = adapter.getPageAdaptScreenWidth();
                if (cWidth != 0) {
                    return cWidth;
                } else {
                    return getDeviceScreenWidth(context);
                }
            }
        }
        // 如果页面没有指定pad适配的宽度，那么采用全局的pad适配值。
        if (sCustomScreenWidth != 0) {
            return sCustomScreenWidth;
        } else {
            return getDeviceScreenWidth(context);
        }
    }


    public static int getScreenWidthDP(Context context) {
       return (int) pxToDp(context,getScreenWidth(context));
    }

    public static int getScreenHeightDP(Context context) {
        return (int) pxToDp(context,getScreenHeight(context));
    }
    /**
     * 获取手机屏幕高度
     *
     * @param context 上下文环境
     * @return int类型
     */
    public static int getScreenHeight(Context context) {
        // pad适配后，屏幕高度和页面有关，需要由页面自己指定
        if (context instanceof IPadScreenAdapter) {
            IPadScreenAdapter adapter = ((IPadScreenAdapter) context);
            if (adapter.usePageAdaptSize()) {
                int cHeight = adapter.getPageAdaptScreenHeight();
                if (cHeight != 0) {
                    return cHeight;
                } else {
                    return getDeviceScreenHeight(context);
                }
            }
        }
        // 如果页面没有指定pad适配的高度，那么采用全局的pad适配值。
        if (sCustomScreenHeight != 0) {
            return sCustomScreenHeight;
        } else {
            return getDeviceScreenHeight(context);
        }
    }

    public static int getHeaderHeight(Context context) {
        return (int) (ScreenUtils.getScreenHeight(context) * (0.466f));
    }

    public static int getHeaderHeightForCardTips(Context context) {
        return (int) ((ScreenUtils.getScreenHeight(context) - context.getResources().getDimension(R.dimen.browser_homepage_card_tips)) * (0.466f));
    }

    /**
     * 获取设备屏幕宽度
     * @param context
     * @return
     */
    public static int getDeviceScreenWidth(Context context){
        if (context == null) {
            return 0;
        }
        // 如果context是Activity，在插件化平台上，获取的宽度不对，改为getRealScreenWidth
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics metric = new DisplayMetrics();
//        windowManager.getDefaultDisplay().getMetrics(metric);
//        return metric.widthPixels;
        return getRealScreenWidth(context);
    }

    /**
     * 获取设备屏幕高度
     * @param context
     * @return
     */
    private static int sDeviceScreenHeightPortrait = -1;
    private static int sDeviceScreenHeightLandscape = -1;

    /**
     * 获取显示区的屏幕高度（非全屏界面，不包括状态栏）
     * @param context
     * @return
     */
    public static int getDeviceScreenHeight(Context context){
        if (context == null) {
            return 0;
        }
        checkCacheValid(context);
        if (isPortrait(context)) {
            if (sDeviceScreenHeightPortrait == -1) {
                WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metric = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(metric);
                sDeviceScreenHeightPortrait = metric.heightPixels;
            }
            return sDeviceScreenHeightPortrait;
        } else {
            if (sDeviceScreenHeightLandscape == -1) {
                WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metric = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(metric);
                sDeviceScreenHeightLandscape = metric.heightPixels;
            }
            return sDeviceScreenHeightLandscape;
        }
    }

    /**
     * 内部私有方法，不对外开放
     * 屏幕物理尺寸变化，重置所有相关缓存
     * @param context
     */
    private static void checkCacheValid(Context context){
        if (isScreenSizeChanged(context)) {
            // 屏幕物理尺寸变化，重置所有相关缓存
            sDeviceScreenHeightPortrait = -1;
            sDeviceScreenHeightLandscape = -1;
            sPortraitRealSizeCache = null;
            sLandscapeRealSizeCache = null;
            sCustomScreenWidth = 0;
            sCustomScreenHeight = 0;
        }
    }

    private static int sScreenLayout;

    /**
     * 内部私有方法，不对外开放
     * 判断屏幕尺寸是否变化
     * @param context
     * @return
     */
    private static boolean isScreenSizeChanged(Context context){
        int layout = context.getApplicationContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (sScreenLayout != layout) {
            sScreenLayout = layout;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取物理设备宽度
     * @param context
     * @return
     */
    public static int getRealScreenWidth(Context context){
        return getDeviceScreenSizeWithCache(context)[0];
    }

    /**
     * 获取物理屏幕的高度
     * @param context
     * @return
     */
    public static int getRealScreenHeight(Context context){
        return getDeviceScreenSizeWithCache(context)[1];
    }

    /**
     * 纵向屏幕的物理尺寸缓存，用于优化调用
     * by yaogd
     */
    private static int[] sPortraitRealSizeCache = null;
    /**
     * 横向屏幕的物理尺寸缓存，用于优化调用
     * by yaogd
     */
    private static int[] sLandscapeRealSizeCache = null;

    /**
     * 内部私有方法，不对外开放
     * 获取屏幕的真实宽高
     * size[0] = widthPixels;
     * size[1] = heightPixels;
     *
     * @param context
     * @return
     */
    private static int[] getDeviceScreenSizeWithCache(Context context) {
        // 汽车之家插件化hook缺陷，不能直接用context.getResources().getConfiguration().orientation
        int orientation = context.getApplicationContext().getResources().getConfiguration().orientation;
        int[] result;
        checkCacheValid(context);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            result = sLandscapeRealSizeCache;
            if (result == null) {
                result = doGetRealScreenSize(context);
                if(result[0] > result[1]){
                    // the result may be wrong sometimes, do not cache !!!!
                    sLandscapeRealSizeCache = result;
                }
            }
            return result;
        } else {
            result = sPortraitRealSizeCache;
            if (result == null) {
                result = doGetRealScreenSize(context);
                if(result[0] < result[1]){
                    // the result may be wrong sometimes, do not cache !!!!
                    sPortraitRealSizeCache = result;
                }
            }
            return result;
        }
    }

    /**
     * 内部私有方法，不对外开放
     * size[0] = widthPixels;
     * size[1] = heightPixels;
     *
     * @param context
     * @return
     */
    private static int[] doGetRealScreenSize(Context context) {
        int[] size = new int[2];
        int widthPixels, heightPixels;
        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        widthPixels = metrics.widthPixels;
        heightPixels = metrics.heightPixels;
        try {
            // used when 17 > SDK_INT >= 14; 包括状态栏和菜单栏
            widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
            heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
        } catch (Exception ignored) {
        }
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                // used when SDK_INT >= 17; 包括状态栏和菜单栏
                Point realSize = new Point();
                d.getRealSize(realSize);
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                widthPixels = realSize.x;
                heightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        }
        size[0] = widthPixels;
        size[1] = heightPixels;
        return size;
    }

    /**
     * 获取屏幕高度(包括底部虚拟导航栏的高度，一般用于沉浸式页面获取高度)
     *
     * @return
     */
    public static int getScreenHeightForVirtualNav(Context context) {
        if (context == null) {
            return 0;
        }

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        if (Build.VERSION.SDK_INT >= 19) {
            // 可能有虚拟按键的情况
            defaultDisplay.getRealSize(outPoint);
        } else {
            // 不可能有虚拟按键
            defaultDisplay.getSize(outPoint);
        }
        int realSizeHeight = outPoint.y;
        return realSizeHeight;
    }

    /**
     * 当前是否是横屏
     * 受插件化影响，context.getResources().getConfiguration()不会随着转屏更新
     * 需要用context.getApplicationContext().getResources()
     * @param context context
     * @return boolean
     */
    public static boolean isLandscape(Context context) {
        return context.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 当前是否是竖屏
     * 受插件化影响，context.getResources().getConfiguration()不会随着转屏更新
     * 需要用context.getApplicationContext().getResources()
     * @param context context
     * @return boolean
     */
    public static boolean isPortrait(Context context) {
        if (context == null) return true;
        return context.getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 设置屏幕为横屏
     *
     * @param activity activity
     */
    public static void setLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 设置屏幕为竖屏
     *
     * @param activity activity
     */
    public static void setPortrait(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 判断当前界面是否是全屏
     * @param activity
     * @return
     */
    public static boolean isFullScreen(Activity activity) {
        int flag = activity.getWindow().getAttributes().flags;
        return (flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    /**
     * 判断是否有NavigationBar
     *
     * @param context
     * @return
     */
    public static boolean hasNavBar(Context context) {
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return  context.getResources().getBoolean(id);
        }
        return false;
    }

    /**
     * 动态隐藏软键盘
     *
     * @param activity activity
     */
    public static void hideSoftInput(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 动态显示软键盘
     *
     * @param context 上下文
     * @param edit    输入框
     */
    public static void showSoftInput(Context context, EditText edit) {
        if(context == null || edit == null){
            return;
        }

        edit.setFocusable(true);
        edit.setFocusableInTouchMode(true);
        edit.requestFocus();
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edit, 0);
    }

    /**
     * 判断是否锁屏
     *
     * @param context 上下文
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isScreenLock(Context context) {
        KeyguardManager km = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }

    /**
     * 获取系统亮度
     * 取值在(0 -- 255)之间
     */
    public static int getSystemScreenBrightness(Context context) {
        int values = 0;
        try {
            values = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 设置当前屏幕量度0到255
     */
    public static void setActivityScreenBrightness(Activity activity, int paramInt) {
        if (activity == null) {
            return;
        }

        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        localLayoutParams.screenBrightness = calculationScreenBrightnessValue(paramInt);
        localWindow.setAttributes(localLayoutParams);
    }

    /**
     * 将0到255之间的值转化为0-1之前的浮点小数，代表量度
     *
     * @param paramInt 0-255
     * @return 最小为0：最低屏幕亮度，最大为1，最高屏幕亮度。
     */
    public static float calculationScreenBrightnessValue(int paramInt) {
        if (paramInt < 0) {
            paramInt = 0;
        }

        if (paramInt > 255) {
            paramInt = 255;
        }

        return paramInt / 255.0F;
    }

    public static void resetActivityScreenBrightness(Activity activity) {
        if (activity == null) {
            return;
        }

        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        localLayoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        localWindow.setAttributes(localLayoutParams);
    }

    /**
     * 是否保持当前屏幕常亮
     *
     * @param shouldKeepScreenOn
     */
    public static void keepScreenOn(Activity attachActivity, boolean shouldKeepScreenOn) {
        if (attachActivity == null) {
            return;
        }
        Activity activity = attachActivity;
        if (attachActivity.getParent() != null && attachActivity.getParent() instanceof TabActivity) {
            activity = attachActivity.getParent();
        }
        if (shouldKeepScreenOn) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
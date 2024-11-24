package com.intelligence.commonlib.tools;

import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.IdRes;

import java.util.HashMap;
import java.util.Map;

public class AHPadAdaptUtil extends PadUtil implements IPadScreenAdapter, ComponentCallbacks {
    private String TAG = "pad-a";
    public static float sPadAdaptWindowRatioHW = 1.1267606F;
    private static String META_NAME_PAD = "ah_pad_orientation";
    private static String META_NAME_PHONE = "ah_phone_orientation";
    private static String META_NAME_LAYOUT = "ah_layout_adapt";
    private static String META_VALUE_AUTO = "auto";
    private static String META_VALUE_LANDSCAPE = "landscape";
    private static String META_VALUE_PORTRAIT = "portrait";
    private static String META_VALUE_NONE = "none";
    private String mPadOri;
    private String mPhoneOri;
    private int mActivityConfigOri;
    private String mLayoutAdapt;
    private float mPadAdaptWindowRatioHW;
    private String mActivityName;
    private Drawable mPadPortraitBg;
    private Drawable mPadLandscapeBg;
    private Drawable mPadContentBg;
    private boolean isExcluded;
    private boolean isTabActivity;
    private int mCurrentScreenWidth;
    private int mUserCustomWidth;
    private boolean isStrictMode;
    private boolean notAdaptLayout;
    private static Map<String, ActivityFoldState> sFoldStateMap;
    public static boolean sPadSimulator;
    public static boolean sFoldSimulator;
    public static boolean sIsFoldDevice;
    private Activity mActivity;
    private View mPadAdaptView;
    private boolean mUseCustomView;
    private int mCustomAdaptViewID;
    private static Map<String, Integer> sActivityConfigCache = new HashMap();
    private boolean isPaused;
    Handler mCompatHandler;

    public AHPadAdaptUtil() {
        this.mPadAdaptWindowRatioHW = sPadAdaptWindowRatioHW;
        this.isStrictMode = true;
    }

    public void onCreateBeforeSuper(Activity activity) {
        this.onCreateBeforeSuper(activity, 0);
    }

    public void onCreateBeforeSuper(Activity activity, @IdRes int id) {
        if (activity != null) {
            this.mActivity = activity;
            this.mCustomAdaptViewID = id;
            this.mActivityName = activity.getClass().getSimpleName();
            if (false) {
                this.isExcluded = true;
            } else {
                if (isFoldable(activity)) {
                    if (sFoldStateMap == null) {
                        sFoldStateMap = new HashMap();
                    }

                    ActivityFoldState state = new ActivityFoldState();
                    state.ori = activity.getApplicationContext().getResources().getConfiguration().orientation;
                    state.screenLayout = getScreenLayout(activity);
                    sFoldStateMap.put(getActivityName(activity), state);
                }

                ActivityInfo info = null;

                try {
                    ComponentName cName = activity.getComponentName();
                    info = activity.getPackageManager().getActivityInfo(cName, 128);
                } catch (PackageManager.NameNotFoundException var5) {
                    var5.printStackTrace();
                }

                if (info != null) {
                    if (info.screenOrientation != 14 && info.screenOrientation != 3) {
                        this.mActivityConfigOri = info.screenOrientation;

                        if (this.isStrictMode) {
                            return;
                        }

                        if (info.screenOrientation != -1) {
                            return;
                        }
                    }

                    this.mCurrentScreenWidth = this.mUserCustomWidth = 0;
                    if (!isFoldable(activity) && activity instanceof IPadScreenAdapter) {
                        this.mUserCustomWidth = ((IPadScreenAdapter)activity).getCustomPageWidth();
                    }

                    this.mActivity.registerComponentCallbacks(this);
                    if (info.metaData != null) {
                        this.mPadOri = info.metaData.getString(META_NAME_PAD);
                        this.mPhoneOri = info.metaData.getString(META_NAME_PHONE);
                        this.mLayoutAdapt = info.metaData.getString(META_NAME_LAYOUT);
                    }

                    this.mPadOri = this.mPadOri == null ? META_VALUE_AUTO : this.mPadOri;
                    this.mPhoneOri = this.mPhoneOri == null ? META_VALUE_PORTRAIT : this.mPhoneOri;
                    if (isFoldable(activity)) {
                        this.mPadOri = this.mPhoneOri;
                    }

                    if (this.mCustomAdaptViewID == 0) {
                        this.startAdapt(this.mActivity, 0);
                    }

                }
            }
        }
    }

    public void onResume() {
        if (this.mActivity != null && this.isPaused && isPad(this.mActivity) && AppDeviceHelper.isMIUIV9()) {
            this.isPaused = false;
            if (this.mCompatHandler == null) {
                this.mCompatHandler = new Handler(Looper.getMainLooper());
            }

            this.startAdapt(this.mActivity, 3);
        }

    }

    public void onPause() {
        this.isPaused = true;
        if (this.mCompatHandler != null) {
            this.mCompatHandler.removeCallbacks(null);
        }

    }

    public void afterSetContentView() {
        if (this.mCustomAdaptViewID != 0) {
            this.startAdapt(this.mActivity, 0);
        }

    }

    public void onFinishAndDestroy() {
        if (this.mActivity != null) {
            this.mActivity.unregisterComponentCallbacks(this);
        }

        if (sFoldStateMap != null && this.mActivity != null) {
            sFoldStateMap.remove(this.mActivity.getClass().getName());
        }

        this.mActivity = null;
    }

    private void startAdapt(final Activity activity, int retryCount) {
        boolean isPad = isPad(activity);
        String metaTag = isPad ? this.mPadOri : this.mPhoneOri;

        if (metaTag != null && !META_VALUE_NONE.equals(metaTag)) {
            if (retryCount == 0) {
                if (META_VALUE_AUTO.equals(metaTag)) {
                    metaTag = "user";
                    Configuration cfg = activity.getApplicationContext().getResources().getConfiguration();
                    this.adaptWH(activity, cfg.orientation, false);
                }

                if (META_VALUE_LANDSCAPE.equals(metaTag) || "sensorLandscape".equals(metaTag) || "reverseLandscape".equals(metaTag)) {
                    this.adaptWH(activity, 2, false);
                }

                if (META_VALUE_PORTRAIT.equals(metaTag) || "sensorPortrait".equals(metaTag) || "reversePortrait".equals(metaTag)) {
                    this.adaptWH(activity, 1, false);
                }
            }

            final int ori = getOrientation(metaTag);
            if (retryCount > 0 && this.mCompatHandler != null) {
                for(int i = 0; i < retryCount; ++i) {
                    this.mCompatHandler.postDelayed(new Runnable() {
                        public void run() {
                            activity.setRequestedOrientation(ori);
                        }
                    }, 400 + i * 200);
                }
            } else {
                activity.setRequestedOrientation(ori);
            }

        } else {
            this.isExcluded = true;
        }
    }

    private void adaptWH(Activity activity, int ori, boolean isConfigChanged) {
        if (activity != null && !META_VALUE_NONE.equals(this.mLayoutAdapt) && isPad(activity) && !isFoldable(activity)) {
            if (!isConfigChanged) {
                Configuration cfg = activity.getApplicationContext().getResources().getConfiguration();
                if (ori != cfg.orientation) {
                    return;
                }
            }

            if (this.mPadAdaptView == null) {
                this.mPadAdaptView = activity.getWindow().getDecorView().findViewById(16908290);
            }

            if (this.mCustomAdaptViewID != 0 && this.mPadAdaptView != null) {
                View customAdaptView = this.mPadAdaptView.findViewById(this.mCustomAdaptViewID);
                if (customAdaptView != null) {
                    this.mUseCustomView = true;
                    this.mPadAdaptView = customAdaptView;
                } else {
                    this.mUseCustomView = false;
                }
            }

            this.changeLayoutWidth(activity, this.mPadAdaptView, ori, this.mUseCustomView);
        }
    }

    private void changeLayoutWidth(Activity activity, View view, int ori, boolean useCustomView) {
        if (view != null && activity != null) {
            if (this.notAdaptLayout) {
                this.matchParentLayout(view, null, useCustomView);
            } else {
                ViewGroup.LayoutParams LP = view.getLayoutParams();
                if (ori == 2) {
                    int padLWidth;
                    if (this.mUserCustomWidth != 0) {
                        padLWidth = this.mCurrentScreenWidth = this.mUserCustomWidth;
                    } else {
                        padLWidth = getPadLandscapeWidth(activity);
                        ScreenUtils.sCustomScreenWidth = this.mCurrentScreenWidth = padLWidth;
                    }

                    if (useCustomView) {
                        this.matchLandscapePadding(view, padLWidth);
                    } else if (LP instanceof android.widget.LinearLayout.LayoutParams) {
                        ((android.widget.LinearLayout.LayoutParams)LP).gravity = 17;
                        LP.width = padLWidth;
                        view.setLayoutParams(LP);
                    } else if (LP instanceof ViewGroup.MarginLayoutParams) {
                        LP.width = padLWidth;
                        ViewGroup.MarginLayoutParams marginLP = (ViewGroup.MarginLayoutParams) LP;
                        int deviceW = ScreenUtils.getRealScreenWidth(activity);
                        int rlMargin = (deviceW - padLWidth) / 2;
                        if (Build.VERSION.SDK_INT >= 17) {
                            if (0 == marginLP.getLayoutDirection()) {
                                marginLP.leftMargin = rlMargin;
                            } else {
                                marginLP.rightMargin = rlMargin;
                            }
                        } else {
                            marginLP.leftMargin = rlMargin;
                        }

                        view.setLayoutParams(LP);
                    } else {
                        this.matchLandscapePadding(view, padLWidth);
                    }
                } else if (ori == 1) {
                    ScreenUtils.sCustomScreenWidth = this.mCurrentScreenWidth = 0;
                    this.matchParentLayout(view, LP, useCustomView);
                }

            }
        }
    }

    private void matchLandscapePadding(View view, int padLWidth) {
        int deviceW = ScreenUtils.getRealScreenWidth(view.getContext());
        int leftRight = (deviceW - padLWidth) / 2;
        Integer pL = (Integer)view.getTag(2131361892);
        Integer pR = (Integer)view.getTag(2131361893);
        if (pL == null && pR == null) {
            pL = view.getPaddingLeft();
            pR = view.getPaddingRight();
            view.setTag(2131361892, pL);
            view.setTag(2131361893, pR);
        }

        view.setPadding(leftRight + pL, view.getPaddingTop(), leftRight + pR, view.getPaddingBottom());
    }

    private void matchPortraitPadding(View view) {
        Integer pL = (Integer)view.getTag(2131361892);
        Integer pR = (Integer)view.getTag(2131361893);
        if (pL != null && pR != null) {
            view.setPadding(pL, view.getPaddingTop(), pR, view.getPaddingBottom());
        }

    }

    private void matchParentLayout(View view, ViewGroup.LayoutParams LP, boolean useCustomView) {
        if (useCustomView) {
            this.matchPortraitPadding(view);
        } else {
            if (LP == null) {
                LP = view.getLayoutParams();
            }

            if (LP instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLP = (ViewGroup.MarginLayoutParams) LP;
                marginLP.leftMargin = 0;
                marginLP.rightMargin = 0;
            } else {
                this.matchPortraitPadding(view);
            }

            LP.width = -1;
            LP.height = -1;
            view.setLayoutParams(LP);
        }

    }

    public void setNotAdaptLayout(boolean notAdaptLayout) {
        if (this.mActivity != null && isPad(this.mActivity)) {
            this.notAdaptLayout = notAdaptLayout;
            this.changeLayoutWidth(this.mActivity, this.mPadAdaptView, this.getOrientation(this.mActivity), this.mUseCustomView);
        }

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

    private void initPadBackground(Window window, View content) {
        if (this.mPadPortraitBg == null) {
            this.mPadPortraitBg = window.getDecorView().getBackground();
        }

        if (this.mPadLandscapeBg == null) {
            this.mPadLandscapeBg = new ColorDrawable(content.getContext().getResources().getColor(android.R.color.background_dark));
        }

        if (this.mPadContentBg == null) {
            this.mPadContentBg = content.getBackground();
        }

    }

    private Drawable getPadLandContentBg() {
        if (this.mPadContentBg == null) {
            return this.mPadPortraitBg != null ? this.mPadPortraitBg : new ColorDrawable(Color.parseColor("#ffffff"));
        } else {
            return this.mPadContentBg;
        }
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

    public String getPadOrientation() {
        return this.mPadOri;
    }

    public String getPhoneOrientation() {
        return this.mPhoneOri;
    }

    public static int getWhiteSpaceWidth(Activity activity) {
        if (activity == null) {
            return 0;
        } else {
            return isPad(activity) && ScreenUtils.isLandscape(activity) ? (ScreenUtils.getRealScreenWidth(activity) - ScreenUtils.getScreenWidth(activity)) / 2 : 0;
        }
    }

    public static int getPadAdaptConfigure(Activity activity) {
        if (activity == null) {
            return -1;
        } else {
            Integer screenOrientation = sActivityConfigCache.get(activity.getClass().getName());
            if (screenOrientation != null) {
                return screenOrientation;
            } else {
                ActivityInfo info = null;

                try {
                    ComponentName cName = activity.getComponentName();
                    info = activity.getPackageManager().getActivityInfo(cName, 128);
                } catch (PackageManager.NameNotFoundException var5) {
                    var5.printStackTrace();
                }

                if (info == null) {
                    screenOrientation = -1;
                } else if (info.screenOrientation != 14 && info.screenOrientation != 3) {
                    screenOrientation = info.screenOrientation;
                } else {
                    if (info.metaData != null) {
                        String layoutAdapt = info.metaData.getString(META_NAME_LAYOUT);
                        if (!META_VALUE_NONE.equals(layoutAdapt)) {
                            String ori;
                            if (isFoldable(activity)) {
                                ori = info.metaData.getString(META_NAME_PHONE);
                            } else if (isPad(activity)) {
                                ori = info.metaData.getString(META_NAME_PAD);
                            } else {
                                ori = info.metaData.getString(META_NAME_PHONE);
                            }

                            screenOrientation = getOrientation(ori);
                        }
                    }

                    if (screenOrientation == null) {
                        if (isFoldable(activity)) {
                            screenOrientation = 1;
                        } else if (isPad(activity)) {
                            screenOrientation = 4;
                        } else {
                            screenOrientation = 1;
                        }
                    }
                }

                sActivityConfigCache.put(activity.getClass().getName(), screenOrientation);
                return screenOrientation;
            }
        }
    }

    public static boolean isPad(Context context) {
        return false;
    }

    public static boolean isFoldable(Context context) {
        if (sFoldSimulator) {
            return true;
        } else {
            return isHuaweiFoldDevice() || isSamsungFoldDevice();
        }
    }

    public static boolean isHuaweiFoldDevice() {
        String brand = Build.BRAND.toLowerCase();
        return brand.equals("huawei") && (Build.MODEL.equals("RLI-AN00") || Build.MODEL.equals("RLI-N29") || Build.MODEL.equals("TAH-N29") || Build.MODEL.equals("TAH-AN00") || Build.MODEL.equals("TAH-AN00m") || Build.MODEL.equals("RHA-AN00m"));
    }

    public static boolean isSamsungFoldDevice() {
        String brand = Build.BRAND.toLowerCase();
        return brand.equals("samsung") && (Build.MODEL.equals("SM-F9000") || Build.MODEL.equals("SM-W2020"));
    }

    public static int getFoldStatus(Context context, String activityName) {
        if (sFoldStateMap == null) {
            return 0;
        } else {
            ActivityFoldState state = sFoldStateMap.get(activityName);
            if (state == null) {
                return 0;
            } else {
                Configuration config = context.getApplicationContext().getResources().getConfiguration();
                int ori = config.orientation;
                if (state.ori != ori) {
                    state.ori = ori;
                    return 0;
                } else {
                    return isFoldStatus(context) ? -1 : -2;
                }
            }
        }
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

    public void setStrictMode(boolean isStrictMode) {
        this.isStrictMode = isStrictMode;
    }

    public int getPageAdaptScreenWidth() {
        return this.mCurrentScreenWidth;
    }

    public int getPageAdaptScreenHeight() {
        return 0;
    }

    public boolean usePageAdaptSize() {
        return !this.isTabActivity;
    }

    public int getCustomPageWidth() {
        return 0;
    }

    public static int getCommonCustomPageWidth(Activity activity) {
        if (Build.VERSION.SDK_INT <= 26) {
            return ScreenUtils.isLandscape(activity) ? ScreenUtils.getRealScreenHeight(activity) : ScreenUtils.getRealScreenWidth(activity);
        } else {
            return 0;
        }
    }

    public int getCustomPageHeight() {
        return 0;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.isExcluded) {
        } else {
            this.adaptWH(this.mActivity, newConfig.orientation, true);
        }
    }

    public void onLowMemory() {
    }

    public static class ActivityFoldState {
        public int ori;
        public int screenLayout;

        public ActivityFoldState() {
        }
    }
}

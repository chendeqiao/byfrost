package com.intelligence.browser.utils;

import android.text.format.DateUtils;

import com.intelligence.commonlib.tools.SharedPreferencesUtils;

public class RedSystemControll {
    public static boolean sIsShowNavigationRed;
    public static boolean sIsShowAddRecommendRed;
    public static boolean sIsShowSettingRed;
    public static boolean sIsShowSlogoRed;

    public static boolean isCanShowAddRecommendRed(){
        return !sIsShowNavigationRed;
    }

    public static boolean isCanShowSettingRed() {
        int versioncode = (int) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_EDIT, 0);
        return !sIsShowNavigationRed && !sIsShowAddRecommendRed && versioncode > 0
                && !((int) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_SETTING, 0) >0);
    }

    public static boolean isCanShowHotRed() {
        try {
            long lastClickTime = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.TIME_HOT_CURRENT_CLICK, 0l);
            return !DateUtils.isToday(lastClickTime);
        }catch (Exception e){
            return false;
        }
    }
}

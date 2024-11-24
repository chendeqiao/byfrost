package com.intelligence.browser.markLock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;


public class ApplicationUtils {
    private static String[] showStrs = new String[]{"z"};

    //独立浏览器的 applicationID
    public static final String PKG_SEPARATE = "com.kwah.privatebrowser";
    public static final boolean APP_TEST = false;

    public static String getApplicationPackageName(Context context) {
        if (context != null && context.getApplicationContext() != null) {
            return context.getApplicationContext().getPackageName();
        }
        return "";
    }

    public static boolean isIndividualPrivacyApp(Context context) {
//        String pkgName = getApplicationPackageName(context);
//        if (pkgName != null && pkgName.equals(PKG_SEPARATE)) {
//            return true;
//        }
//        return false;
        return false;
    }


    private static String genericIDIfNeeded() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BrowserApplication.getInstance());
        String originID = sp.getString("key_APPID", null);
        if (originID != null && originID.length() > 5)
            return originID;
        String id = UUID.randomUUID().toString();
        sp.edit().putString("key_APPID", id).apply();
        return id;
    }

    /**
     * 获取用户唯一识别码
     */
    public static String getID() {
        return genericIDIfNeeded();
    }


}

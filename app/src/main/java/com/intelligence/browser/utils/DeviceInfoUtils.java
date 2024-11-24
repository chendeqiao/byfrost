package com.intelligence.browser.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import androidx.core.app.ActivityCompat;

import com.intelligence.browser.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceInfoUtils {

    public static String APP_DOWN_LOAD_URL = "https://a.app.qq.com/o/simple.jsp?pkgname=";
    public static String APP_DOWN_LOAD_URL_googleplay = "https://play.google.com/store/apps/details?id=";

    //客户端信息
    public interface DeviceInfo {
        String BRAND = Build.BRAND; //手机品牌
        String MODEL = Build.MODEL; //手机型号
        String HADRWARENO = Build.HARDWARE;
        String OSVERSION = Build.BOARD;
        String PRODUCTCODE = Build.DEVICE;
        String OFFICEVERSION = Build.FINGERPRINT;
        String PRODUCTMODE = Build.PRODUCT;
        String SERIAL_NO = Build.SERIAL;
    }

    /**
     * 获取应用版本
     */
    public static String getAppVersionName(Context context) {
        String packageName = context.getPackageName();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAppName(Context context){
        try {
            String applicationName = context.getResources().getString(R.string.application_name);
            return applicationName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取屏幕尺寸
     */
    public static String getScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels + "*" + dm.heightPixels;
    }

    /**
     * 获取应用versioncode
     */
    public static int getAppVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getUserInfo(Context context) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        String versionCode = getAppVersionCode(context) + "";
        String versionName = getAppVersionName(context);
        String userinfo = "Time:" + time + ";versionCode:" + versionCode + ";versionName:" + versionName;
        return userinfo;
    }
    /**
     * 获取应用包名
     */
    public static String getAppPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getAppDownUrl(Context context) {
        if(BrowserApplication.getInstance().isChina()) {
            return APP_DOWN_LOAD_URL + context.getPackageName();
        }else {
            return APP_DOWN_LOAD_URL_googleplay + context.getPackageName();
        }
    }

    public static void openAppMark(Context context) {
        try {
            if (BrowserApplication.getInstance().isChina() && !isGooglePlayInstalled(context)) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getAppPackageName(context))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id="+getAppPackageName(context)));
                intent.setPackage("com.android.vending");
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            ToastUtil.show("抱歉，您没有安装任何应用市场");
        }
    }

    public static boolean isGooglePlayInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            // 尝试获取Google Play商店的包信息
            PackageInfo packageInfo = packageManager.getPackageInfo("com.android.vending", 0);
            return packageInfo != null; // 如果获取成功，说明已安装
        } catch (PackageManager.NameNotFoundException e) {
            // Google Play商店未安装
            return false;
        }
    }

    public static boolean isAppOnForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context
                .ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    public static String getDefaultLanguage() {
        String language = Locale.getDefault().toString();
        return TextUtils.isEmpty(language) ? "en_US" : language;
    }

    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }


    public static final int REQUEST_EXTERNAL_STORAGE = 112;
    public static final int REQUEST_EXTERNAL_STORAGE_FIRST = 113;
    public static final int REQUEST_EXTERNAL_STORAGE_WALL = 114;

    public static final int REQUEST_EXTERNAL_STORAGE_QR = 117;

    public static final int REQUEST_EXTERNAL_STORAGE_IMAGE_WALL = 115;
    public static final int REQUEST_EXTERNAL_STORAGE_IMAGE_DOWN = 116;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public static boolean verifyStoragePermissions(Activity activity,int requestCode) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,requestCode);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

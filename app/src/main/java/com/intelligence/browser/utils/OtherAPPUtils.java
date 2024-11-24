package com.intelligence.browser.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OtherAPPUtils {

    public static final String GOOGLE_PLAY = "com.android.vending";
    private static final String GOOGLE_PLAY_APP = "com.google.android.finsky.activities.LaunchUrlHandlerActivity";
    private static final String GOOGLE_PLAY_APP_URI = "market://details?id=";
    public static final String GOOGLE_PLAY_APP_WEB_URI = "https://play.google.com/store/apps/details?id=";

    public static final String ADM_APP = "com.dv.adm";
    public static final String ADM_APP_AEDITOR = "com.dv.adm.AEditor";

    public static final String ADM_APP_PRO = "com.dv.adm.pay";
    public static final String ADM_APP_AEDITOR_PRO = "com.dv.adm.pay.AEditor";

    public static void startGooglePlayAPP(Context context, String appName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(GOOGLE_PLAY);
        if (intent == null) {
            return;
        }
        ComponentName comp = new ComponentName(GOOGLE_PLAY, GOOGLE_PLAY_APP);
        intent.setComponent(comp);
        intent.setData(Uri.parse(GOOGLE_PLAY_APP_URI + appName));
        context.startActivity(intent);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    public static void downloadWithADM(Context context, String url, String mimetype) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName()+".fileprovider", new File(url));
            intent.setDataAndType(contentUri, mimetype);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse(url), mimetype);
        }

        ComponentName cn = new ComponentName(ADM_APP,
                ADM_APP_AEDITOR);
        intent.setComponent(cn);
        context.startActivity(intent);
    }

    public static void downloadWithADMPRO(Context context, String url, String mimetype) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse(url), mimetype);

        ComponentName cn = new ComponentName(ADM_APP_PRO,
                ADM_APP_AEDITOR_PRO);
        intent.setComponent(cn);
        context.startActivity(intent);
    }
}

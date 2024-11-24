package com.intelligence.browser.utils;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import org.checkerframework.checker.units.qual.A;

public class DefaultBrowserSetUtils {
    public static final String KEY_DEFAULT_BROWSER_SETTING = "key_default_browser_setting";

    public static final String BROWSABLE_CATEGORY = "android.intent.category.BROWSABLE";

    public static ResolveInfo findDefaultBrowser(Context context) {
        Intent browserIntent = new Intent("android.intent.action.VIEW");
        return context.getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public static boolean isThereNoDefaultBrowser(Context context) {
        ResolveInfo defaultBrowser = findDefaultBrowser(context);
        return "android".equals(defaultBrowser.activityInfo.packageName);
    }

    public static void openOneUrlToSetDefaultBrowser(Context context, String url) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory(BROWSABLE_CATEGORY);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        intent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
        intent.putExtra(KEY_DEFAULT_BROWSER_SETTING, true);
        context.startActivity(intent);
    }

    public static void setDefaultBrowser(Activity context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 及以上
                RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                    Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER);
                    // 启动“设置默认浏览器”页面
                    ((Activity) context).startActivityForResult(intent, 1001);
                }
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "无法打开设置页面", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openAppInfoSettingView(Context context, String targetPackageName) {
        Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String pkg = "com.android.settings";
        String cls = "com.android.settings.applications.InstalledAppDetails";
        i.setComponent(new ComponentName(pkg, cls));
        i.setData(Uri.parse("package:" + targetPackageName));
        context.startActivity(i);
    }

    public static boolean isDefaultBrowser(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10 及以上
            RoleManager roleManager = (RoleManager) context.getSystemService(Context.ROLE_SERVICE);
            return roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_BROWSER);
        } else {
            // Android 9 及以下
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("http://"));
            browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);

            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                String defaultBrowserPackage = resolveInfo.activityInfo.packageName;
                return context.getPackageName().equals(defaultBrowserPackage);
            }
        }
        return false;
    }

    public static boolean isThisBrowserSetAsDefault(Context context) {
        ResolveInfo defaultBrowser = findDefaultBrowser(context);
        return context.getPackageName().equals(defaultBrowser.activityInfo.packageName);
    }

    public static boolean canSetDefaultBrowser(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ActivityInfo in = pm.getActivityInfo(new ComponentName("android", "com.android.internal.app.ResolverActivity"), PackageManager.MATCH_DEFAULT_ONLY);
            return in != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static String getDefaultBrowserName(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        PackageManager packageManager = context.getPackageManager();

        // 获取默认浏览器应用的ResolveInfo
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo != null) {
            String packageName = resolveInfo.activityInfo.packageName;

            try {
                // 获取应用名称
                CharSequence appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0));
                return appName.toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null; // 如果没有找到默认浏览器
    }
}

package com.intelligence.commonlib.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.intelligence.commonlib.Global;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final int SIGNAL_LEVELS = 5;
    private static final int MAX_PERCENT = 100;
    public static boolean isWifiConnect() {
        ConnectivityManager cm = (ConnectivityManager) Global.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (null != info) {
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_ETHERNET:
                case ConnectivityManager.TYPE_BLUETOOTH:
                    return true;
                default:
                    return false;
             }
        }
        return false;
    }

    public static int getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) Global.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            return networkInfo.getType();
        }
        return -1;
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) Global.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    public static boolean isProxyEnabled(Context context) {
        return android.provider.Settings.Secure.getInt(
                context.getContentResolver(), "http_proxy_enabled", 1) != 0;
    }

    public static boolean isProxyForWifiOnly(Context context) {
        return android.provider.Settings.Secure.getInt(
                context.getContentResolver(), "http_proxy_wifi_only", 0) != 0;
    }

    public static int getWifiStrengthPercent() {
        WifiManager wifiManager = (WifiManager) Global.getInstance().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getBSSID() == null) {
            return -1;
        }
        int strength = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), SIGNAL_LEVELS);
        return  strength * MAX_PERCENT / (SIGNAL_LEVELS - 1);
    }
}

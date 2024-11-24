package com.intelligence.browser.downloads.support;

import android.content.Context;

import com.intelligence.commonlib.download.util.NetworkSensor;

public class WinkNetworkSensor implements NetworkSensor {

    Callback mCallback;
//    final NetworkHelper mHelper;
    public WinkNetworkSensor() {
//        mHelper = NetworkHelper.sharedHelper();
    }

    @Override
    public void register(Context context, Callback callback) {
//        final NetworkHelper helper = mHelper;
//        helper.registerNetworkSensor(context);
        mCallback = callback;
//        helper.addNetworkInductor(this);
    }

    @Override
    public void unregister(Context context) {
        mCallback = null;
//        NetworkHelper.sharedHelper().removeNetworkInductor(this);
    }

    @Override
    public NetworkStatus getNetworkStatus() {
//        return toNetworkStatus(mHelper.getNetworkStatus());
        return null;
    }

//    private NetworkStatus toNetworkStatus(NetworkHelper.NetworkStatus status) {
//        int value = status.ordinal();
//        NetworkStatus[] all = NetworkStatus.values();
//        if (all.length <= value)
//            throw new IllegalStateException("invalid network status");
//        return all[value];
//    }
//
//    @Override
//    public void onNetworkChanged(NetworkHelper.NetworkStatus status) {
//        final Callback callback = mCallback;
//        if (callback != null) {
//            callback.onNetworkChanged(toNetworkStatus(status));
//        }
//    }
}

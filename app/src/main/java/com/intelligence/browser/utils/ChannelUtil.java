package com.intelligence.browser.utils;

import android.text.TextUtils;

public class ChannelUtil {

    private static final String CHANNEL_NONE = "00000000";
    private static final String CHANNEL_APPS_CN_PREFIX = "3000";
    private static final String CHANNEL_APPS_EN_PREFIX = "4000";

    private final static String CHANNEL_CHINA_START = "8000";//国内渠道的首数字

    private static final String[] sUpdateList = {
            CHANNEL_NONE,
            CHANNEL_APPS_CN_PREFIX,
            CHANNEL_APPS_EN_PREFIX

    };

    public static boolean isUpdateEnabled() {
        String channelId = getChannelId();
        if (TextUtils.isEmpty(channelId)) return false;
        for (String str : sUpdateList) {
            if (channelId.equals(str) || channelId.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGoogleChannel() {
        return true;
//        String channelId = getChannelId();
//        return !TextUtils.isEmpty(channelId) && channelId.startsWith(CHANNEL_GOOGLE_PREFIX);
    }

    public static String getChannelId() {
//        return Browser.getInstance().getApplicationContext().getString(R.string.channel);
        return "googleplay";
    }

    public static boolean isOpenPlatform() {
        return CHANNEL_NONE.equals(getChannelId());
    }

    /**
     * 是否是国内渠道
     *
     * @return
     */
    public static boolean isChinaChannel() {
        String channelId = getChannelId();
        return !TextUtils.isEmpty(channelId) && channelId.startsWith(CHANNEL_CHINA_START);
    }
}

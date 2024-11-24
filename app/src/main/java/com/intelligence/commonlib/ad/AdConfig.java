package com.intelligence.commonlib.ad;//package com.yunxin.commonlib.ad;
//
//import android.view.ViewGroup;
//
//import com.bytedance.sdk.openadsdk.AdSlot;
//import com.bytedance.sdk.openadsdk.TTAdConstant;
//import com.yunxin.commonlib.tools.SharedPreferencesUtils;
//
//public class AdConfig {
//    AdLoadListener mAdLoadListener;
//    String adId;
//    int adType;
//    int width;
//    int height;
//    int orientation = TTAdConstant.HORIZONTAL;
//    int adCount = 3;
//    ViewGroup adParentView;
//    int adForbidTime = 0;
//    private final static int mInitPageFinishCount = 3;
//    private static int COUNT_INTERVAL_WEB = 3;
//    private static int mAdCount = 0;
//    private static int mIntervalTimes = 0;
//    int mNativeAdType = AdSlot.TYPE_INTERACTION_AD;
//
//    public AdConfig setAdParentView(ViewGroup adParentView) {
//        this.adParentView = adParentView;
//        return this;
//    }
//
//    public AdConfig setNativeType(int nativeAdType) {
//        this.mNativeAdType = nativeAdType;
//        return this;
//    }
//
//    public AdConfig setAdForbidTime(int adForbidTime) {
//        this.adForbidTime = adForbidTime;
//        return this;
//    }
//
//    public AdConfig setAdListener(AdLoadListener listener) {
//        mAdLoadListener = listener;
//        return this;
//    }
//
//    public AdConfig setWidth(int width) {
//        this.width = width;
//        return this;
//    }
//
//    public AdConfig setHeight(int height) {
//        this.height = height;
//        return this;
//    }
//
//    public AdConfig setAdId(String adId) {
//        this.adId = adId;
//        return this;
//    }
//
//    public AdConfig setAdType(int adType) {
//        this.adType = adType;
//        return this;
//    }
//
//    public static String AD_SPLASH = "https://jhs.m.taobao.com/;";
//
//    public static String AD_INSERT = "https://main.m.taobao.com/;"
//            + "https://www.tmall.com/;"
//            + "https://detail.m.tmall.com/;"
//            + "https://m.jd.com/?;"
//            + "https://www.zhihu.com/?;"
//            + "https://m.qidian.com/;"
//            + "https://m.toutiao.com/;";
//
//
//    public static boolean isShowWebTopAd(String url) {
//        if(mAdCount < mInitPageFinishCount){
//            return false;
//        }
//        if(mIntervalTimes < COUNT_INTERVAL_WEB){
//            return false;
//        }
//        String adWhiteList = SharedPreferencesUtils.getAdWhiteList();
//        return isShowAd(adWhiteList, url);
//    }
//
//    public static boolean isShowWebBottomAd(String url) {
//        mAdCount++;
//        COUNT_INTERVAL_WEB = SharedPreferencesUtils.getADInterval();
//        String adBottomWhiteList = SharedPreferencesUtils.getAdBottomWhiteList();
//        String adWhiteList = SharedPreferencesUtils.getAdWhiteList();
//        if (isShowAd(adBottomWhiteList, url) && !isShowAd(adWhiteList, url) && mIntervalTimes > COUNT_INTERVAL_WEB && mAdCount > mInitPageFinishCount) {
//            mIntervalTimes = 0;
//            return true;
//        }
//        mIntervalTimes++;
//        return false;
//    }
//
//    public static boolean isShowInsertAd(String url) {
//        return isShowAd(AD_INSERT, url);
//    }
//
//    public static boolean isShowSplashAd(String url) {
//        return isShowAd(AD_SPLASH, url);
//    }
//
//    private static boolean isShowAd(String matchurl, String url) {
//        String[] contentUrl = matchurl.split(";");
//        for (String preUlr : contentUrl) {
//            if (url.contains(preUlr)) {
//                return true;
//            }
//        }
//        return false;
//    }
//}

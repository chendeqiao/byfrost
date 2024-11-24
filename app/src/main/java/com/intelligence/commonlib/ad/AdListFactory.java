package com.intelligence.commonlib.ad;

//import com.bytedance.sdk.openadsdk.AdSlot;
//import com.bytedance.sdk.openadsdk.TTAdConstant;
//import com.bytedance.sdk.openadsdk.TTAdDislike;
//import com.bytedance.sdk.openadsdk.TTAdNative;
//import com.bytedance.sdk.openadsdk.TTAdSdk;
//import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
//import com.bytedance.sdk.openadsdk.TTImage;
//import com.bytedance.sdk.openadsdk.TTNativeAd;
//import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
//import com.bytedance.sdk.openadsdk.preload.geckox.model.UpdatePackage;
//import com.yunxin.commonlib.data.AdSwitchData;
//import com.yunxin.commonlib.tools.ScreenUtils;


//public class AdListFactory {
//    private static AdListFactory instance;
//
//    //信息流广告
//    public static final String AD_FEED_STREAM_ID = "945988921";
//    //顶部banner广告 640*100
//    public static final String AD_BANNER_TOP_ID = "945988852";
//    //底部banner广告 640*150
//    public static final String AD_BANNER_BOTTOM_ID = "945989367";
//    //插屏广告  3*2
//    public static final String AD_INSERT_SCREEN_SAMLL_ID = "945988865";
//    //插屏广告 2*3
//    public static final String AD_INSERT_SCREEN_BIG_ID = "945988895";
//    //feed流顶部广告
//    public static final String AD_BANNER_LIST_HEADER_ID = "945991059";
//    //导航banner广告
//    public static final String AD_BANNER_NAVIGATION_ID = "945991107";
//    //热搜词广告
//    public static final String AD_BANNER_HOTWORD_10 = "945991232";
//    public static final String AD_BANNER_HOTWORD_20 = "945991248";
//    public static final String AD_BANNER_HOTWORD_30 = "945991272";
//    public static final String AD_BANNER_HOTWORD_40 = "945991295";
//    public static final String AD_BANNER_HOTWORD_50 = "945991325";
//    public static final String AD_NEW_INSERT_SCREEN = "945996643";
//
//
//    public static final int AD_FEED_STREAM = 1000;
//    public static final int AD_BANNER = 1001;
//    public static final int AD_INSERT = 1003;
//    public static final int AD_NEW_INSERT = 1005;
//    public static final int AD_SPLASH = 1004;
//
//    public static final int AD_FORBID_TIME_BOTTOM = 60;
//    public static final int AD_FORBID_TIME_TOP = 60;
//    public static final int AD_FORBID_TIME_INSERT = 60;
//
//    private AdListFactory() {
//    }
//
//    public static AdListFactory getInstance() {
//        if (instance == null)
//            instance = new AdListFactory();
//        return instance;
//    }
//    private AdSwitchData mAdSwitchData;
//    private boolean checkoutAdSwitch(String adId){
//        if(TextUtils.isEmpty(adId)){
//            return false;
//        }
//        if (mAdSwitchData == null) {
//            try {
//                mAdSwitchData = new AdSwitchData();
//                mAdSwitchData.parseJSON(new JSONObject(SharedPreferencesUtils.getAdConfig()));
//            } catch (Exception e) {
//                return false;
//            }
//        }
//        if(mAdSwitchData == null){
//            return false;
//        }
//        if (mAdSwitchData.getTotalAdSwith() == 0) {
//            return false;
//        }
//        switch (adId){
//            case AD_BANNER_TOP_ID:
//                if(mAdSwitchData.getWebviewTopAdSwith() == 1){
//                    return true;
//                }
//                break;
//            case AD_BANNER_BOTTOM_ID:
//                if(mAdSwitchData.getWebviewBottomAdSwith() == 1){
//                    return true;
//                }
//                break;
//            case AD_BANNER_NAVIGATION_ID:
//                if(mAdSwitchData.getNavigationAdSwith() == 1){
//                    return true;
//                }
//                break;
//            case AD_BANNER_HOTWORD_50:
//                if(mAdSwitchData.getHotwordAdSwith() == 1){
//                    return true;
//                }
//                break;
//        }
//        return false;
//    }
//
//    public void generateAdView(final Activity cto, final AdConfig adConfig) {
//        if (adConfig == null || cto == null) {
//            return;
//        }
//        if(!checkoutAdSwitch(adConfig.adId)){
//            return;
//        }
//        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(cto);
//        switch (adConfig.adType) {
//            case AD_BANNER:
//                if (System.currentTimeMillis() - (long) SharedPreferencesUtils.get(SharedPreferencesUtils.AD_CLOSE_TIME_KEY + adConfig.adId, 0l) < 1000 * adConfig.adForbidTime) {
//                    return;
//                }
//
//                AdSlot adSlot = new AdSlot.Builder()
//                        .setCodeId(adConfig.adId)
//                        .setExpressViewAcceptedSize(adConfig.width, adConfig.height)
//                        .setSupportDeepLink(true)
//                        .setAdCount(adConfig.adCount) //请求广告数量为1到3条
//                        .setOrientation(adConfig.orientation)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
//                        .build();
//                mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
//                    @Override
//                    public void onError(int code, String message) {
//                    }
//
//                    @Override
//                    public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
//                        if (ads.get(0) == null) {
//                            return;
//                        }
//                        TTNativeExpressAd mTTAd = ads.get(0);
//                        mTTAd.setSlideIntervalTime(5 * 1000);
//                        mTTAd.setDislikeDialog(null);
//                        mTTAd.setDislikeCallback(cto, new TTAdDislike.DislikeInteractionCallback() {
//                            @Override
//                            public void onShow() {
//                            }
//
//                            @Override
//                            public void onSelected(int i, String s) {
//                                if (adConfig.adParentView != null) {
//                                    adConfig.adParentView.removeAllViews();
//                                    adConfig.adParentView.setVisibility(View.GONE);
//                                    SharedPreferencesUtils.put(SharedPreferencesUtils.AD_CLOSE_TIME_KEY + adConfig.adId, System.currentTimeMillis());
//                                }
//                            }
//                            @Override
//                            public void onCancel() {
//                            }
//
//                            @Override
//                            public void onRefuse() {
//                            }
//                        });
//                        mTTAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
//                            @Override
//                            public void onAdClicked(View view, int type) {
//                                Log.e("TTAdManagerHolder", "onAdClicked" + type);
//                                if (adConfig.adParentView != null) {
//                                    adConfig.adParentView.postDelayed(new Runnable() {
//                                                                          @Override
//                                                                          public void run() {
//                                                                              generateAdView(cto, adConfig);
//                                                                          }
//                                                                      }
//                                            , 1500);
//                                } else {
//                                    generateAdView(cto, adConfig);
//                                }
//                            }
//
//                            @Override
//                            public void onAdShow(View view, int type) {
//                                Log.e("TTAdManagerHolder", "onAdShow" + type);
//                            }
//
//                            @Override
//                            public void onRenderFail(View view, String msg, int code) {
//                                Log.e("TTAdManagerHolder", "onRenderFail" + msg);
//                            }
//
//                            @Override
//                            public void onRenderSuccess(View view, float width, float height) {
//                                Log.e("TTAdManagerHolder", "onRenderSuccess");
//                                adConfig.mAdLoadListener.setView(view);
//                            }
//                        });
//                        mTTAd.render();
//                    }
//                });
//                break;
//            case AD_FEED_STREAM:
//                break;
//            case AD_INSERT:
//                adSlot = new AdSlot.Builder()
//                        .setCodeId(adConfig.adId)
//                        .setExpressViewAcceptedSize(adConfig.width, adConfig.height)
//                        .setAdCount(adConfig.adCount) //请求广告数量为1到3条
//                        .build();
//                mTTAdNative.loadInteractionExpressAd(adSlot,new TTAdNative.NativeExpressAdListener(){
//                    @Override
//                    public void onError(int i, String s) {
//
//                    }
//
//                    @Override
//                    public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
//                        if (ads == null || ads.size() == 0) {
//                            return;
//                        }
//
//                        TTNativeExpressAd mTTInterstitialExpressAd = ads.get(0);
//                        mTTInterstitialExpressAd.setSlideIntervalTime(30 * 1000);
////                        mTTInterstitialExpressAd.setExpressInteractionListener(mInterstitialExpressAdInteractionListener);
//                        mTTInterstitialExpressAd.render();
//                    }
//                });
//                break;
//            case AD_SPLASH:
//                break;
//            case AD_NEW_INSERT:
////                mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
////                    @Override
////                    public void onError(int i, String s) {
////                        Log.e("TTAdManagerHolder", i+"loadFullScreenVideoAd" + s);
////                    }
////
////                    @Override
////                    public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
////                        Log.e("TTAdManagerHolder", "onFullScreenVideoAdLoad");
////                        if (ttFullScreenVideoAd != null) {
////                            ttFullScreenVideoAd.showFullScreenVideoAd(cto, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
////                        }
////                    }
////
////                    @Override
////                    public void onFullScreenVideoCached() {
////                        Log.e("TTAdManagerHolder", "onAdShonFullScreenVideoCachedow");
////                    }
////                });
//                break;
//        }
//    }
//
//    public void preloadAd(Activity context){
//        //导航页面广告
////        AdConfig adConfig = new AdConfig();
////        adConfig.setAdListener(null).setHeight(50)
////                .setWidth(ScreenUtils.getScreenWidthDP(context) - 40)
////                .setAdId(AdListFactory.AD_BANNER_NAVIGATION_ID)
////                .setAdType(AdListFactory.AD_BANNER).setAdForbidTime(AdListFactory.AD_FORBID_TIME_TOP);
////        AdListFactory.getInstance().generateAdView(context, adConfig);
//    }
//}

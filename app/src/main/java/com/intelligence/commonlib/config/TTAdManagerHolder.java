package com.intelligence.commonlib.config;//package com.yunxin.commonlib.config;
//
//import android.content.Context;
//
//import com.bytedance.sdk.openadsdk.TTAdConfig;
//import com.bytedance.sdk.openadsdk.TTAdConstant;
//import com.bytedance.sdk.openadsdk.TTAdManager;
//import com.bytedance.sdk.openadsdk.TTAdSdk;
//
//
///**
// * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
// */
//public class TTAdManagerHolder {
//
//    private static final String TAG = "TTAdManagerHolder";
//    private static final String APPID = "5146057";
//
//    private static boolean sInit;
//
//
//    public static TTAdManager get() {
//        if (!sInit) {
//            throw new RuntimeException("TTAdSdk is not init, please check.");
//        }
//        return TTAdSdk.getAdManager();
//    }
//
//    public static void init(final Context context) {
//        doInit(context);
//    }
//
//    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
//    private static void doInit(Context context) {
//        if (!sInit) {
//            TTAdSdk.init(context, buildConfig(context));
//            sInit = true;
//        }
//    }
//
//    private static TTAdConfig buildConfig(Context context) {
//        return new TTAdConfig.Builder()
//                .appId(APPID)
//                .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
//                .allowShowNotify(true) //是否允许sdk展示通知栏提示
//                .allowShowPageWhenScreenLock(true) // 锁屏下穿山甲SDK不会再出落地页，此API已废弃，调用没有任何效果
//                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI,
//                        TTAdConstant.NETWORK_STATE_3G,
//                        TTAdConstant.NETWORK_STATE_4G) //允许直接下载的网络状态集合
//                .supportMultiProcess(true)//是否支持多进程
//                .needClearTaskReset().build();
//    }
//}

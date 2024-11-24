package com.intelligence.commonlib.config;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.intelligence.browser.ui.MainActivity;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import org.json.JSONObject;

public class BrowserConfig {
    public static final String BROWSER_AD_SWITCH = "{\"totalAdSwith\":0,\"webviewTopAdSwith\":0,\"webviewBottomAdSwith\":0,\"hotwordAdSwith\":0,\"navigationAdSwith\":0}";

    public static final String BROWSER_AD_WHITE_LIST= "https://www.bilibili.com/;https://m.ixigua.com/;https://m.bilibili.com/;https://m.bilibili.com/;https://www.bilibili.com/;https://news.cctv.com/;https://wap.xinmin.cn/;https://3g.china.com/act;https://m.weibo.cn/;sports.cctv.com;https://m.sohu.com/;https://m.pingduoduo.com/;https://m.pinduoduo.com/;https://3g.163.com/main;https://news.163.com/;https://m.jd.com/?;https://m.jd.com/;https://m.cctv.com/index.shtml;https://h5.m.jd.com/;https://www.zhihu.com/;https://h5.mse.360.cn/newnavi;https://main.m.taobao.com/;https://hbrbshare;http://m.xinhuanet.com/;https://xhpfmapi.zhongguowangshi.com/;";

    public static final String BROWSER_AD_WHITE_BOTTOM_LIST= "https://m.baidu.com/s;https://m.baidu.com/from;https://sports.sina.cn/;https://m.douban.com/;https://m.toutiaoimg.cn/;https://m.58.com/;.58.com/;https://3g.163.com/;https://image.baidu.com/;https://xyx.hao123.com/;https://m.tiexue.net/;http://m.tiexue.net/;https://m.iqiyi.com/;https://i.ifeng.com/;http://m.people.cn/;sogou.com/;https://m.so.com/;http://cn.bing.com/;https://xw.qq.com/;https://www.bing.com/;https://www.google.com/;https://m.toutiao.com/?W2atIF=1;https://m.toutiao.com/;http://m.xinhuanet.com/;http://www.yidianzixun.com/;http://m.ctrip.com/;v.cctv.com;m.guokr.com;https://chaoshi.m.tmall.com/;https://news.baidu.com/news#/;";

    public static final String BROWSER_HOTWORD_GROUPS =  "{\n" +
            "    \"hotwordmaintop\": \"https://top.baidu.com/board\",\n" +
            "    \"hotwordmainbottom\": \"https://h5.mse.360.cn/24thresou.html#/\",\n" +
            "    \"hotwordsearchpage\": \"https://h5.mse.360.cn/24thresou.html#/\",\n" +
            "    \"hotwordNotification\": \"https://top.baidu.com/board\"\n" +
            "}";


    public static String BYFROST_CONFIG = "byfrostconfig";

    public static void initBrowserConfig(Context context, MainActivity.ConfigCallBack callBack) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task != null && task.isSuccessful()) {
                            try {
                                String versionInfo = mFirebaseRemoteConfig.getString(BYFROST_CONFIG);
                                SharedPreferencesUtils.put(BYFROST_CONFIG,versionInfo);
                                JSONObject jsonObject = new JSONObject(versionInfo);
                                VersionInfo vi = new VersionInfo();
                                vi.updateContent = jsonObject.optString("updatecontent");
                                vi.versionName = jsonObject.optString("versionname");
                                vi.versionCode = jsonObject.optInt("versioncode");
                                if(callBack != null){
                                    callBack.callBack(vi);
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                });
    }

}

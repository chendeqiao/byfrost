package com.intelligence.news.websites;

import android.util.Log;

import com.intelligence.commonlib.net.OkHttpException;
import com.intelligence.commonlib.net.RequestMode;
import com.intelligence.commonlib.net.RequestParams;
import com.intelligence.commonlib.net.ResponseCallback;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.websites.bean.AllWebSiteData;
import com.intelligence.news.websites.bean.WebSiteData;

import org.json.JSONArray;
import org.json.JSONObject;

public class WebsitesHttpRequest {

    public static final int PAGE_BAIDU = 0;
    public static final int PAGE_DOUYIN = 1;

    private ResponseCallback mCallBack;
    private String url = "";

    public void getHotWordApi(final int type, ResponseCallback callback) {
        mCallBack = callback;
//    String url = "";
//    url = HotWordHttpRequest.HOT_WORD_BAIDU;

        RequestParams requestParams = new RequestParams();
        RequestMode.getRequest(url, requestParams, new ResponseCallback() {
            @Override
            public void onSuccess(Object responseObj) {
                if (mCallBack != null) {
                    mCallBack.onSuccess(parseData(responseObj + "", false));
                }
            }

            @Override
            public void onFailure(OkHttpException failuer) {
                if (mCallBack != null) {
                    mCallBack.onFailure(failuer);
                }
            }
        }, null);
    }

    public static DataResult parseData(String data, boolean isCache) {
        DataResult ret = new DataResult();
        try {
            JSONObject root = new JSONObject(data);
            int code = root.getInt("returncode");
            ret.returnCode = code;
            if (code != 0 || !root.has("result")) {
                return ret;
            }
            JSONObject result = root.getJSONObject("result");

            //历史记录
            if (result.has("browserhistory")) {
                JSONArray rcmseries = result.getJSONArray("browserhistory");
                int functionLength = rcmseries != null ? rcmseries.length() : 0;
                for (int i = 0; i < functionLength; i++) {
                    WebSiteData rcmscenes = new WebSiteData();
                    rcmscenes.parseJSON(rcmseries.getJSONObject(i));
                    ret.browserhistory.add(rcmscenes);
                }
            }

          if (result.has("lablesites")) {
            JSONArray rcmseries = result.getJSONArray("lablesites");
            int functionLength = rcmseries != null ? rcmseries.length() : 0;
            for (int i = 0; i < functionLength; i++) {
              WebSiteData rcmscenes = new WebSiteData();
              rcmscenes.parseJSON(rcmseries.getJSONObject(i));
              ret.lablesites.add(rcmscenes);
            }
          }

            if (result.has("toolssites")) {
                JSONArray rcmseries = result.getJSONArray("toolssites");
                int functionLength = rcmseries != null ? rcmseries.length() : 0;
                for (int i = 0; i < functionLength; i++) {
                    WebSiteData rcmscenes = new WebSiteData();
                    rcmscenes.parseJSON(rcmseries.getJSONObject(i));
                    ret.toolssites.add(rcmscenes);
                }
            }

            //网址导航
            if (result.has("websites")) {
                JSONArray function = result.getJSONArray("websites");
                int functionLength = function != null ? function.length() : 0;
                for (int i = 0; i < functionLength; i++) {
                    WebSiteData rcmscenes = new WebSiteData();
                    rcmscenes.parseJSON(function.getJSONObject(i));
                    ret.websites.add(rcmscenes);
                }
            }

            if (result.has("appsites")) {
                JSONArray function = result.getJSONArray("appsites");
                int functionLength = function != null ? function.length() : 0;
                for (int i = 0; i < functionLength; i++) {
                    WebSiteData rcmscenes = new WebSiteData();
                    rcmscenes.parseJSON(function.getJSONObject(i));
                    ret.appsites.add(rcmscenes);
                }
            }

            if (result.has("websitesgroup")) {
                JSONArray function = result.getJSONArray("websitesgroup");
                int functionLength = function != null ? function.length() : 0;
                for (int i = 0; i < functionLength; i++) {
                    AllWebSiteData rcmscenes = new AllWebSiteData();
                    rcmscenes.parseJSON(function.getJSONObject(i));
                    ret.allWebSite.add(rcmscenes);
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }
}

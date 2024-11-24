package com.intelligence.browser.ui.home;

import android.text.TextUtils;
import android.util.Log;

import com.intelligence.commonlib.net.OkHttpException;
import com.intelligence.commonlib.net.RequestMode;
import com.intelligence.commonlib.net.RequestParams;
import com.intelligence.commonlib.net.ResponseCallback;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.news.NetConfig;
import com.intelligence.news.news.mode.DataResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherHttpRequest {
    public static void refreshWeahterData() {
        WeatherHttpRequest hotWordHttpRequest = new WeatherHttpRequest();
        hotWordHttpRequest.getWeatherData(null);
    }

    public static DataResult getData() {
        String cache = "";
        cache = (String) SharedPreferencesUtils.get(SharedPreferencesUtils.WEATHER_DATA, "");
        return parseData(cache, true);
    }

    public void getWeatherData(ResponseCallback callback) {
        String url = NetConfig.URL_WEATHER;
        String cityName = (String) SharedPreferencesUtils.get(SharedPreferencesUtils.WEATHER_CITY, "");
        if (TextUtils.isEmpty(cityName)) {
            return;
        }
        long cachetime = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.WEATHER_UPDATA_TIME, 0l);
        if (System.currentTimeMillis() - cachetime < 2 * 60 * 60 * 1000) {
            callback.onSuccess(parseData(getCache(), true));
            return;
        }
        RequestParams requestParams = new RequestParams();
        requestParams.put("key", NetConfig.NET_KEY);
        requestParams.put("city", cityName);
        RequestMode.getRequest(url, requestParams, new ResponseCallback() {
            @Override
            public void onSuccess(Object responseObj) {
                if (callback != null) {
                    callback.onSuccess(parseData(responseObj + "", false));
                }
            }

            @Override
            public void onFailure(OkHttpException failuer) {
                Log.e("fda","fail"+failuer.getEmsg());
            }
        }, null);
    }

    private String getCache() {
        String cache = (String) SharedPreferencesUtils.get(SharedPreferencesUtils.WEATHER_DATA, "");
        if (!TextUtils.isEmpty(cache)) {
            return cache;
        }
        return "";
    }

    public static DataResult parseData(String data, boolean isCache) {
        DataResult dataResult = new DataResult();
        try {
            JSONObject root = new JSONObject(data);
            int success = root.optInt("code");
            if (success != 200) {
                return dataResult;
            }
            if (!isCache) {
                SharedPreferencesUtils.put(SharedPreferencesUtils.WEATHER_DATA, data);
            }
            ArrayList weatherList = new ArrayList();
            if (root.has("newslist")) {
                JSONArray hotinfoList = root.getJSONArray("newslist");
                for (int i = 0; i < hotinfoList.length(); i++) {
                    JSONObject object = hotinfoList.getJSONObject(i);
                    WeatherData hotWord = new WeatherData();
                    hotWord.parseJSON(object);
                    weatherList.add(hotWord);
                }
                dataResult.newsList = weatherList;
                return dataResult;
            }
        } catch (Exception e) {
        }
        return dataResult;
    }
}

package com.intelligence.news.hotword;

import android.text.TextUtils;

import com.intelligence.commonlib.net.OkHttpException;
import com.intelligence.commonlib.net.RequestMode;
import com.intelligence.commonlib.net.RequestParams;
import com.intelligence.commonlib.net.ResponseCallback;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.news.NetConfig;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.news.mode.HotWordData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class HotWordHttpRequest {

  public static final String CACHE_HOT_WORD_KEY = "cache_hot_word_key_";
  public static final String CACHE_HOT_WORD_TIME_KEY = "cache_hot_word_time_key_";

  private ResponseCallback mCallBack;
  private String url = "";

  public interface RefreshHotWordListener{
    void onRefreshHotWord();
  }

    private static ArrayList<RefreshHotWordListener> mListenerList = new ArrayList<>();

  public static void registerListener(RefreshHotWordListener listener) {
    mListenerList.add(listener);
  }

  public static void removeListener(RefreshHotWordListener listener) {
    mListenerList.remove(listener);
  }

  public static void refreshHotWord() {
    long cachetime = (long) SharedPreferencesUtils.get(CACHE_HOT_WORD_TIME_KEY + 0, 0l);
    if (System.currentTimeMillis() - cachetime < 30 * 60 * 1000) {
      return;
    }
    for (int i = 0; i < 3; i++) {
      HotWordHttpRequest hotWordHttpRequest = new HotWordHttpRequest();
      hotWordHttpRequest.getHotWordApi(i,true, new ResponseCallback() {
        @Override
        public void onSuccess(Object responseObj) {
          for (RefreshHotWordListener listener : mListenerList) {
            listener.onRefreshHotWord();
          }
        }

        @Override
        public void onFailure(OkHttpException failuer) {
        }
      });
    }
  }

  public static DataResult getData(int pageType) {
    String cache = "";
    if (pageType == NetConfig.PAGE_BAIDU) {
      cache = (String) SharedPreferencesUtils.get(HotWordHttpRequest.CACHE_HOT_WORD_KEY + pageType, BrowserInitDataContants.HOT_WORD_BAIDU);
    } else if (pageType == NetConfig.PAGE_DOUYIN) {
      cache = (String) SharedPreferencesUtils.get(HotWordHttpRequest.CACHE_HOT_WORD_KEY + pageType, BrowserInitDataContants.HOT_WORD_DOUYIN);
    } else {
      cache = (String) SharedPreferencesUtils.get(HotWordHttpRequest.CACHE_HOT_WORD_KEY + pageType, BrowserInitDataContants.HOT_WORD_WEIBO);
    }
    return parseData(cache, pageType, true);
  }

  public void getHotWordApi(final int type, boolean isRequestNet, ResponseCallback callback) {
    mCallBack = callback;
    String url = "";
    if (type == NetConfig.PAGE_BAIDU) {
      url = NetConfig.HOT_WORD_BAIDU;
    } else if (type == NetConfig.PAGE_DOUYIN) {
      url = NetConfig.HOT_WORD_DOUYIN;
    } else {
      url = NetConfig.HOT_WORD_WEIBO;
    }

    RequestParams requestParams = new RequestParams();
    requestParams.put("key", NetConfig.NET_KEY);
    long cachetime = (long) SharedPreferencesUtils.get(CACHE_HOT_WORD_TIME_KEY + type, 0l);
    if (!isRequestNet) {
      String cache = getCache(type);
      if (!TextUtils.isEmpty(cache)) {
        if (mCallBack != null) {
          mCallBack.onSuccess(parseData(cache + "", type, true));
        }
        return;
      }
    } else if (System.currentTimeMillis() - cachetime > 30 * 60 * 1000)
      RequestMode.getRequest(url, requestParams, new ResponseCallback() {
        @Override
        public void onSuccess(Object responseObj) {
          if (mCallBack != null) {
            mCallBack.onSuccess(parseData(responseObj + "", type, false));
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

  private String getCache(int pageType) {
    String cache = (String) SharedPreferencesUtils.get(CACHE_HOT_WORD_KEY + pageType, "");
    if (!TextUtils.isEmpty(cache)) {
      return cache;
    }
    return "";
  }

  public  static DataResult parseData(String data, int pageType, boolean isCache) {
    DataResult dataResult = new DataResult();
    try {
      JSONObject root = new JSONObject(data);
      int success = root.optInt("code");
      if (success != 200) {
        return dataResult;
      }
      if (!isCache) {
        SharedPreferencesUtils.put(CACHE_HOT_WORD_KEY + pageType, data);
        SharedPreferencesUtils.put(CACHE_HOT_WORD_TIME_KEY + pageType, System.currentTimeMillis());
      }
      ArrayList hotwordList = new ArrayList();
      if (root.has("newslist")) {
        JSONArray hotinfoList = root.getJSONArray("newslist");
        for (int i = 0; i < hotinfoList.length(); i++) {
          JSONObject object = hotinfoList.getJSONObject(i);
          HotWordData hotWord = new HotWordData();
          hotWord.pageType = pageType;
          hotWord.parseJSON(object);
          hotwordList.add(hotWord);
        }
        dataResult.newsList = hotwordList;
        return dataResult;
      }
    } catch (Exception e) {
    }
    return dataResult;
  }
}

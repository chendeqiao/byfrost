package com.intelligence.news.news.net;

import com.intelligence.commonlib.net.OkHttpException;
import com.intelligence.commonlib.net.RequestMode;
import com.intelligence.commonlib.net.RequestParams;
import com.intelligence.commonlib.net.ResponseCallback;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.news.NetConfig;
import com.intelligence.news.hotword.BrowserInitDataContants;
import com.intelligence.news.news.channel.CardFactory;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.news.mode.NewsData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewsRequest {

  public static final String CACHE_NEWS_KEY = "cache_news_";

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

  public static void refreshNews(int type) {
    NewsRequest hotWordHttpRequest = new NewsRequest();
    hotWordHttpRequest.getFeedStream(type, new ResponseCallback() {
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

  public void getFeedStream(final int type, ResponseCallback callback) {
    mCallBack = callback;
    String url = "";
    if (type == NetConfig.NEWS_BULLETIN) {
      url = NetConfig.URL_NEWS_BULLETIN;
    } else if (type == NetConfig.NEWS_WORLD) {
      url = NetConfig.URL_NEWS_WORD;
    } else {
      url = NetConfig.HOT_WORD_WEIBO;
    }

    RequestParams requestParams = new RequestParams();
    requestParams.put("num", "50");
    requestParams.put("key", NetConfig.NET_KEY);
    String cache = getCache(type);
//    if (!TextUtils.isEmpty(cache)) {
//      if (mCallBack != null) {
//        mCallBack.onSuccess(parseData(cache + "", type, true));
//      }
//      return;
//    }
    RequestMode.getRequest(url, requestParams, new ResponseCallback() {
      @Override
      public void onSuccess(Object responseObj) {
        if (mCallBack != null) {
          mCallBack.onSuccess(parseData(responseObj + "", type,false));
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
    String cache = (String) SharedPreferencesUtils.get(CACHE_NEWS_KEY + pageType, BrowserInitDataContants.BROWSER_NEW_CACHE);
    return cache;
  }

  public DataResult parseData(String data, int pageType, boolean isCache) {
    DataResult dataResult = new DataResult();
    try {
      JSONObject root = new JSONObject(data);
      int success = root.optInt("code");
      if (success != 200) {
        return dataResult;
      }
      if (!isCache) {
        SharedPreferencesUtils.put(CACHE_NEWS_KEY + pageType, data);
      }
      ArrayList newsData = new ArrayList();
      if (root.has("newslist")) {
        JSONArray hotinfoList = root.getJSONArray("newslist");
        for (int i = 0; i < hotinfoList.length(); i++) {
          JSONObject object = hotinfoList.getJSONObject(i);
          NewsData newData = new NewsData();
          newData.parseJSON(object);
            if ((i == 0 || i == 1) && (newData.cardType == CardFactory.CARD_IMAGE)) {
                newData.cardType = CardFactory.CARD_TEXT_IMAGE;
            }
          newsData.add(newData);
        }
        dataResult.newsList = newsData;
        return dataResult;
      }
    } catch (Exception e) {
    }
    return dataResult;
  }
}

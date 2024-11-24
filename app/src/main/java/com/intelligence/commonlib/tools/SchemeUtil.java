package com.intelligence.commonlib.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.intelligence.commonlib.data.HotwordsUrlData;

import org.json.JSONObject;

import java.net.URLEncoder;

public class SchemeUtil {
    public static final String BROWSER_SCHEME_PATH = "yunxin://browser/";
//    首页scheme: yunxin://browser/home?
//    精选scheme: yunxin://browser/news?
//    导航scheme: yunxin://browser/navigation?
//    搜索scheme: yunxin://browser/search?
//    二维码scheme: yunxin://browser/qrcode?
//    天气scheme: yunxin://browser/weather?
//    日历scheme: yunxin://browser/date?
//    笑话scheme: yunxin://browser/joke?
//    历史scheme: yunxin://browser/history?
//    书签历史scheme: yunxin://browser/bookmark?
//    下载scheme: yunxin://browser/download?


    public static final int BROWSER_SOURCE_KEY_HOME = 101;
    public static final int BROWSER_SOURCE_KEY_SEARCH = 102;
    public static final int BROWSER_SOURCE_KEY_SETTING = 103;
    public static final int BROWSER_SOURCE_KEY_NAVIGATION = 105;

    public static final String BROWSER_SCHEME_PATH_HOME = BROWSER_SCHEME_PATH + "home?";
    public static final String BROWSER_SCHEME_PATH_NEWS = BROWSER_SCHEME_PATH + "news?";
    public static final String BROWSER_SCHEME_PATH_NAVIGATION = BROWSER_SCHEME_PATH + "navigation?";
    public static final String BROWSER_SCHEME_PATH_SEARCH = BROWSER_SCHEME_PATH + "search?";
    public static final String BROWSER_SCHEME_PATH_QRCODE = BROWSER_SCHEME_PATH + "qrcode?";
    public static final String BROWSER_SCHEME_PATH_WEATHER = BROWSER_SCHEME_PATH + "weather?";
    public static final String BROWSER_SCHEME_PATH_DATE = BROWSER_SCHEME_PATH + "date?";
    public static final String BROWSER_SCHEME_PATH_JOKE = BROWSER_SCHEME_PATH + "joke?";
    public static final String BROWSER_SCHEME_PATH_HISTORY = BROWSER_SCHEME_PATH + "history?";
    public static final String BROWSER_SCHEME_PATH_BOOKMARK = BROWSER_SCHEME_PATH + "bookmark?";
    public static final String BROWSER_SCHEME_PATH_DOWN = BROWSER_SCHEME_PATH + "download?";
    public static final String BROWSER_SCHEME_PATH_HOT_WORDS = BROWSER_SCHEME_PATH + "hotsearch?";
    public static final String BROWSER_SCHEME_PATH_SETTING = BROWSER_SCHEME_PATH_HOME + "&page=" + BROWSER_SOURCE_KEY_SETTING;


    public static boolean jumpToScheme(Context context, String url) {
        try {
            turnToDetail(context, url);
        } catch (Exception e) {
            //scheme is error.
        }
        return true;
    }

    public static boolean openHistoryPage(Context context) {
        try {
            invokeActivity(context, BROWSER_SCHEME_PATH_HISTORY + "&page=1");
        } catch (Exception e) {
        }
        return true;
    }

    public static boolean openHistoryPage(Context context,int index) {
        try {
            invokeActivity(context, BROWSER_SCHEME_PATH_HISTORY + "&page_index="+index);
        } catch (Exception e) {
        }
        return true;
    }

    public static boolean openWebView(Context context, String url) {
        try {
            if (null != context && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(url.trim())) {
                if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                    url = "yunxin://browser/home?url=" + url;
                } else if (!url.contains(BROWSER_SCHEME_PATH)) {
                    url = "yunxin://browser/home?url=" + url;
                }
                invokeActivity(context, url);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //scheme is error.
        }
        return true;
    }

    public static boolean openNewWebView(Context context, String url) {
        try {
            try {
                url = URLEncoder.encode(url);
                if (null != context && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(url.trim())) {
                    if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                        url = "yunxin://browser/home?url=" + url + "&newtab=true";
                    } else if (!url.contains(BROWSER_SCHEME_PATH)) {
                        url = "yunxin://browser/home?url=" + url + "&newtab=true";
                    }
                    invokeActivity(context, url);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                //scheme is error.
            }
        } catch (Exception e) {
            //scheme is error.
        }
        return true;
    }

    public static void jumpHotWorsPage(Context context, int type) {
        try {
            String json = SharedPreferencesUtils.getHotWordGroups();
            JSONObject jsonObject = new JSONObject(json);
            HotwordsUrlData hotwordsUrlData = new HotwordsUrlData();
            hotwordsUrlData.parseJSON(jsonObject);
            String hotwordUrl = "";
            if (type == 0) {
                hotwordUrl = SchemeUtil.BROWSER_SCHEME_PATH_HOT_WORDS + "&page=0";
                ;
            } else if (type == 1) {
                hotwordUrl = SchemeUtil.BROWSER_SCHEME_PATH_HOT_WORDS + "&page=1";
            } else if (type == 2) {
                hotwordUrl = SchemeUtil.BROWSER_SCHEME_PATH_HOT_WORDS + "&page=2";
            } else if (type == 3) {
                hotwordUrl = hotwordsUrlData.getHotwordNotification();
            } else {
                hotwordUrl = hotwordsUrlData.getHotwordmaintop();
            }
            if (!TextUtils.isEmpty(hotwordUrl)) {
                SchemeUtil.jumpToScheme(context, hotwordUrl);
            }
        } catch (Exception e) {
        }
    }

    public static boolean jumpToWord(Context context, String word) {
        try {
            turnToDetail(context, word);
        } catch (Exception e) {
            //scheme is error.
        }
        return true;
    }

    public static boolean startHotSearchDetail(Context context, String url) {
        if (null != context) {
            invokeActivity(context, "yunxin://browser/hotsearch?url=" + url);
            return true;
        } else {
            return false;
        }
    }

    private static boolean turnToDetail(Context context, String url) {
        if (null != context && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(url.trim())) {
            if (url.contains(BROWSER_SCHEME_PATH)) {
                invokeActivity(context, url);
            } else if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                url = URLEncoder.encode(url);
                invokeActivity(context, "yunxin://browser/home?url=" + url);
            } else {
                url = URLEncoder.encode(url);
                invokeActivity(context, "yunxin://browser/home?url=" + url);
            }
            return true;
        } else {
            return false;
        }
    }

    private static void invokeActivity(@NonNull Context context, @NonNull String url) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static String hideLocalUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        try {
            if (url.contains(BROWSER_SCHEME_PATH)) {
                Uri uri = Uri.parse(url);
                return uri.getQueryParameter("url");
            } else if (url.contains("file://") && url.contains("files/")){
                Uri uri = Uri.parse(url);
                String path = uri.getPath();
                return path.substring(path.lastIndexOf("files/") + 6);
            }
        } catch (Exception e) {
            return url;
        }
        return url;
    }


}

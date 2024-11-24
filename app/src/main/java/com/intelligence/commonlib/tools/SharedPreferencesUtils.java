package com.intelligence.commonlib.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intelligence.browser.R;
import com.intelligence.browser.settings.multilanguage.LanguageCountry;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.config.BrowserConfig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class SharedPreferencesUtils {
    /**
     * 保存在手机里的文件名
     */
    public static final String FILE_NAME = "browser_data";

    public static final String ACCESS_TOKEN = "token";

    public static final String FIVE_STAR_DIALOG_IS_SHOWED = "five_star_dialog_is_showed";//是否显示过五星弹框
    public static final String FIVE_STAR_OPEN_COUNT = "five_star_open_count";//是否显示过五星弹框
    public static final String FIVE_STAR_LAST_TIME = "five_star_last_time";//是否显示过五星弹框

    public static final String IS_OPEN_DEVERLO_PEROPTIONS = "is_open_deverlo_peroptions";//是否开启开发者选项

    public static final String IS_OPEN_NEWS_TODAY = "is_open_news";//是否打开导航
    public static final String IS_OPEN_EDIT = "is_open_edit";//是否打开导航
    public static final String TIME_HOT_CURRENT_CLICK = "time_hot_current_click";//是否打开导航
    public static final String IS_OPEN_SETTING = "is_open_setting";//是否打开导航
    public static final String IS_OPEN_HOTWORD_TODAY = "is_open_hot_word";//是否打开热搜页面
    public static final String IS_SHOW_HEADER_HOTWORD = "is_show_header_hot_word";//是否打开首页热词
    public static final String IS_SHOW_STATUS_BAR = "is_show_status_bar";//是否显示时间栏
    public static final String SEARCH_WORD_LAST_PASTE = "search_word_last_paste";

    public static final String WEATHER_DATA = "weather_data";
    public static final String WEATHER_CITY = "weather_city";
    public static final String WEATHER_UPDATA_TIME = "weather_update_time";
    public static final String SETTING_NOTIFY_BAR_SHOW = "setting_notify_bar_show";
    public static final String SETTING_SELECT_LANGUAGE_SHOW = "setting_select_language_show";
    public static final String SETTING_AD_HOME_PAGE_TIME = "setting_notify_home_page_show_time";
    public static final String SETTING_DEFAULT_HOME_PAGE_TIME = "setting_default_home_page_show_time";

    //
    public static final String AD_CLOSE_TIME_KEY = "ad_close_time_key";
    public static final String AD_CLOSE_TIME_CURRENT_KEY = "ad_close_time_current_key";

    public static final String BROWSER_NAVIGATION_CACHE = "browser_navigation_cache";
    public static final String BROWSER_SHOW_AD= "browser_show_ad";
    public static final String BROWSER_RECOMMEND_URL= "BROWSER_RECOMMEND_URL";
    public static final String BROWSER_AD_WHITE_LIST= "browser_ad_white_list";
    public static final String BROWSER_AD_WHITE_BOTTOM_LIST= "browser_ad_white_bottom_list";
    public static final String BROWSER_VERSION_CODE = "browser_version_code";

    public static final String BROWSER_UPDATE_SHOWED = "browser_update_showed";
    public static final String BROWSER_UPDATE_SHOWED_COUNT = "browser_update_showed_count";
    public static final String BROWSER_FEEDBACK_TIME = "browser_feedback_time";
    public static final String AD_INTERVAL = "ad_interval";
    public static final String PPIRVATE_PRODUCT_AGREEMENT= "private_product_agreenment";
    public static final String BROWSER_APP_DOWN_URL = "browser_APP_DOWN_URL";
    public static final String BROWSER_HOTWORDS_GROUPS = "browser_hotwords_groups";
    public static final String BROWSER_WEBVIEW_FINISH_COUNT = "browser_webview_finish_count";

    private static final String LANGUAGE_SELECTED = "language_selected";
    private static final String COUNTRY_SELECTED = "country_selected";

    /**
     * 保存数据
     *
     * @param key key
     * @param obj value
     */
    public static void put(String key, Object obj) {
        try {
            SharedPreferences sp = Global.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            if (obj instanceof Boolean) {
                editor.putBoolean(key, (Boolean) obj);
            } else if (obj instanceof Float) {
                editor.putFloat(key, (Float) obj);
            } else if (obj instanceof Integer) {
                editor.putInt(key, (Integer) obj);
            } else if (obj instanceof Long) {
                editor.putLong(key, (Long) obj);
            } else {
                editor.putString(key, (String) obj);
            }
            editor.apply();
        }catch (Exception e){
            // to do nothing.
        }
    }

    public static String getDefaultSearchEngine(Context cto) {
//        if (!getRecommonWebsites()) {
//            return "liangzi";
//        } else {
        String searchEngines = cto.getResources().getString(R.string.default_search_engine_value);
        return searchEngines;
//        }
    }
    public static String getNavigationCache(String defaultCache) {
        return (String) get(BROWSER_NAVIGATION_CACHE, defaultCache);
    }

    public static void putNavigationCache(String cache) {
        put(BROWSER_NAVIGATION_CACHE, cache);
    }

    public static void setAdConfig(String showAd) {
        put(BROWSER_SHOW_AD, showAd);
    }

    public static String getAdConfig() {
        return (String) get(BROWSER_SHOW_AD, BrowserConfig.BROWSER_AD_SWITCH);
    }

    public static void setRecommonWebsites(int value) {
        put(BROWSER_RECOMMEND_URL, value);
    }

//    public static boolean getRecommonWebsites(){
//        return (int) get(BROWSER_RECOMMEND_URL,0) == 1;
//    }

    public static boolean getRecommonWebsites() {
        return !BrowserApplication.getInstance().isSimpleVersion();
    }

    public static void setAdInterval(int interval) {
        put(AD_INTERVAL, interval);
    }

    public static int getADInterval() {
        return (int) get(AD_INTERVAL, 5);
    }

    public static void setPrivateProduct(boolean isAgreen) {
        put(PPIRVATE_PRODUCT_AGREEMENT, isAgreen);
    }

    public static boolean getPrivateProduct() {
        return (boolean) get(PPIRVATE_PRODUCT_AGREEMENT, false);
    }

    public static void setAdWhiteList(String adWhiteList){
        put(BROWSER_AD_WHITE_LIST, adWhiteList);
    }
    public static String getAdWhiteList() {
        return (String) get(BROWSER_AD_WHITE_LIST, BrowserConfig.BROWSER_AD_WHITE_LIST);
    }

    public static void setAdBottomWhiteList(String adWhiteList){
        put(BROWSER_AD_WHITE_BOTTOM_LIST, adWhiteList);
    }
    public static String getAdBottomWhiteList() {
        return (String) get(BROWSER_AD_WHITE_BOTTOM_LIST, BrowserConfig.BROWSER_AD_WHITE_BOTTOM_LIST);
    }

    public static void setNewVersion(int versionCode) {
        put(BROWSER_VERSION_CODE, versionCode);
    }

    public static int getVersionCode() {
        return (int)get(BROWSER_VERSION_CODE, 0);
    }

    public static void setAppDownUrl(String downUrl) {
        put(BROWSER_APP_DOWN_URL, downUrl);
    }

    public static String getAppDownUrl() {
        return (String)get(BROWSER_APP_DOWN_URL, "");
    }

    public static void setHotwordGroups(String downUrl) {
        put(BROWSER_HOTWORDS_GROUPS, downUrl);
    }

    public static String getHotWordGroups() {
        return (String)get(BROWSER_HOTWORDS_GROUPS, BrowserConfig.BROWSER_HOTWORD_GROUPS);
    }

    public static int getWebviewOpenCount() {
        return (int) get(SharedPreferencesUtils.BROWSER_WEBVIEW_FINISH_COUNT, 0);
    }

    /**
     * 获取指定的数据
     */
    public static Object get(String key, Object defaultObj) {
        try {
            SharedPreferences sp = Global.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            Object obj = null;
            if (defaultObj instanceof Boolean) {
                obj = sp.getBoolean(key, (Boolean) defaultObj);
            } else if (defaultObj instanceof Float) {
                obj = sp.getFloat(key, (Float) defaultObj);
            } else if (defaultObj instanceof Integer) {
                obj = sp.getInt(key, (Integer) defaultObj);
            } else if (defaultObj instanceof Long) {
                obj = sp.getLong(key, (Long) defaultObj);
            } else {
                obj = sp.getString(key, (String) defaultObj);
            }
            return obj == null ? defaultObj : obj;
        } catch (Exception e) {
            return defaultObj;
        }
    }

    /**
     * 删除指定的数据
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * 返回所有的键值对
     */
    public static Map<String, ?> getAll() {
        SharedPreferences sp = Global.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        return map;
    }

    /**
     * 清除所有数据
     */
    public static void clear() {
        SharedPreferences sp = Global.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 检查是否存在此key对应的数据
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    public static void saveArrayListToPreferences(String key, ArrayList<String> list) {
        SharedPreferences prefs = Global.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 将 ArrayList 转换为 String
        Gson gson = new Gson();
        String json = gson.toJson(list);

        // 保存到 SharedPreferences
        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<String> getArrayListFromPreferences(String key) {
        SharedPreferences prefs = Global.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        // 从 SharedPreferences 中获取 JSON 字符串
        String json = prefs.getString(key, null);

        // 如果有值则将 JSON 转回 ArrayList
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>(); // 如果没有值则返回空的 ArrayList
        }
    }

    public static String getStringValue(String key, String defValue) {
        return (String) get(key, defValue);
    }

    public static LanguageCountry getLanguageSelected(Context context){
        String language = getStringValue(LANGUAGE_SELECTED, LanguageCountry.LANGUAGE_OPTION_DEFAULT);
        String country = getStringValue(COUNTRY_SELECTED, LanguageCountry.COUNTRY_OPTION_DEFAULT);
        if (language.equalsIgnoreCase(LanguageCountry.LANGUAGE_OPTION_DEFAULT)) {
            language = context.getResources().getConfiguration().locale.getLanguage();
        }
        if (country.equalsIgnoreCase(LanguageCountry.COUNTRY_OPTION_DEFAULT)) {
            country = context.getResources().getConfiguration().locale.getCountry();
        }
        return new LanguageCountry(language, country);
    }

}

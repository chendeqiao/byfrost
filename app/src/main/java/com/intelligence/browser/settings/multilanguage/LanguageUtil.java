package com.intelligence.browser.settings.multilanguage;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.util.Locale;

public class LanguageUtil {

//    public static String getCurrentLanguage() {
//        return getCurrentLanguage(BaseApplication.getContext());
//    }

    public static String getCurrentSingleLanguage(){
        Context context = BrowserApplication.getInstance();
        LanguageCountry language = SharedPreferencesUtils.getLanguageSelected(context);
        if(language != null){
            return language.getLanguage();
        }

        return "";
    }
    /**
     * 获取当前语言
     * @param context
     * @return
     */
//    public static String getCurrentLanguage(Context context) {
//        LanguageCountry language = CommonConfigSharedPref.getInstanse(context).getLanguageSelected(context);
//        String lang = language.getLanguage();
//        String country = language.getCountry();
//
//        if(lang != null){
//            lang = lang.toLowerCase(Locale.ENGLISH);
//        }
//        if(country != null){
//            lang = (TextUtils.isEmpty(lang) ? "" : lang) + (TextUtils.isEmpty(country) ? "":("-"+country.toLowerCase(Locale.ENGLISH)));
//        }
//
//        if(TextUtils.isEmpty(lang)){
//            lang = "en";
//        }
//        return lang;
//    }

    public static String getMIGLanguageMark(Context context){
        LanguageCountry language = SharedPreferencesUtils.getLanguageSelected(context);
        String lang = language.getLanguage();
        String country = language.getCountry();

        if(lang != null){
            lang = lang.toLowerCase(Locale.ENGLISH);
        }
        if(country != null){
            lang = (TextUtils.isEmpty(lang) ? "" : lang) + (TextUtils.isEmpty(country) ? "":("-r"+country.toUpperCase(Locale.ENGLISH)));
        }

        if(TextUtils.isEmpty(lang)){
            lang = "en";
        }
        return lang;
    }

    public static String getLanguage(Context context){
        LanguageCountry language = SharedPreferencesUtils.getLanguageSelected(context);
        return language.getLanguage();
    }

    public static String getCountry(Context context){
        LanguageCountry language = SharedPreferencesUtils.getLanguageSelected(context);
        return language.getCountry();
    }

    //切换语言
    public static void toChangeLanguage(String language,String country,Context context){
        Locale locale = new Locale(language,country);
        Resources resources = context.getResources();
        if(resources == null){
            return;
        }

        DisplayMetrics dm = resources.getDisplayMetrics();

        Configuration config =resources.getConfiguration();
        if(config == null){
            return;
        }
        config.locale =locale;
        config.setLayoutDirection(locale);      //阿拉伯语适配
        resources.updateConfiguration(config,dm);
    }

    public static String getLanguageName(Context context){
        LanguageCountry language = SharedPreferencesUtils.getLanguageSelected(context);
        return language.getLanguageName(context);
    }
}

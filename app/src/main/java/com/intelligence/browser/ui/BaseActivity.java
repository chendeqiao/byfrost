package com.intelligence.browser.ui;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.intelligence.browser.R;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.settings.multilanguage.LanguageSettingActivity;
import com.intelligence.browser.settings.multilanguage.LanguageUtil;
import com.intelligence.browser.utils.Constants;
import com.intelligence.commonlib.tools.AppBarThemeHelper;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.util.Locale;

public class BaseActivity extends FragmentActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    public static final String SHARED_LANGUAGE = "language";
    public static final String SHARED_COUNTRY = "country";
    public static final String SHARED_FILE_NAME = "language file";
    public static String TRANSLATE_PAGE = "notify_translate_page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setCurrentLanguage();
            AppBarThemeHelper.setStatusBarLightMode(this);
            registerDataReceiver();
            if (!BrowserSettings.getInstance().getShowStatusBar()) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
                        .FLAG_FULLSCREEN);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            if (BrowserSettings.getInstance().getNightMode()) {
                float value = BrowserSettings.getInstance().getBrightness();
                value = value == DisplayUtil.DEFAULT_BRIGHTNESS
                        ? DisplayUtil.getSystemBrightness((Activity) this) : value;
                DisplayUtil.setScreenBrightness((Activity) this, value);
            } else {
                DisplayUtil.setScreenBrightness(this, (Float) SharedPreferencesUtils.get(Constants
                        .SCREEN_BRIGHTNESS, DisplayUtil.DEFAULT_BRIGHTNESS));
            }
        } catch (Exception e) {
        }
    }

    public void registerDataReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TRANSLATE_PAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(TRANSLATE_PAGE));
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action.equals(TRANSLATE_PAGE)) {
                    translatePage();
                }
            } catch (Exception e) {
            }
        }
    };

    public void setCurrentLanguage() {
        try {
            SharedPreferences sharePre = getSharedPreferences(LanguageSettingActivity.SHARED_FILE_NAME, MODE_PRIVATE);
            String language = sharePre.getString(LanguageSettingActivity.SHARED_LANGUAGE, Locale.getDefault().getLanguage());
            String country = sharePre.getString(LanguageSettingActivity.SHARED_COUNTRY, Locale.getDefault().getCountry());
            LanguageUtil.toChangeLanguage(language, country, this);
        } catch (Exception e) {
        }
    }


    private void translatePage() {
        setCurrentLanguage();
        recreate();
    }

    public BaseActivity() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void back(View view) {
        onBackPressed();
    }

}

package com.intelligence.browser.ui;


import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.utils.Constants;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.lang.reflect.Method;

public class BaseBlackActivity extends FragmentActivity {
    private static final String TAG = BaseBlackActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
//            AppBarThemeHelper.setStatusBarLightMode(this);
//            if (!BrowserSettings.getInstance().getShowStatusBar()) {
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
//                        .FLAG_FULLSCREEN);
//            } else {
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//            }
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11及以上，使用 setDecorFitsSystemWindows
                window.setDecorFitsSystemWindows(false);
            } else {
                // Android 10及以下，使用 FLAG_LAYOUT_NO_LIMITS
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 设置状态栏透明
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);

                // 设置状态栏图标和字体颜色
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 浅色字体 (白色)
                    window.getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    );
                } else {
                    // 对于 Android 6.0 以下无深浅色切换，默认透明即可
                    window.getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    );
                }
            }

            if ("Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
                Class<?> clazz = window.getClass();
                try {
                    int flag = 0x00000100; // MIUI系统标志
                    Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                    extraFlagField.invoke(window, flag, flag);
                } catch (Exception ignored) {}
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

    public BaseBlackActivity() {
    }

    public void back(View view) {
        onBackPressed();
    }

}

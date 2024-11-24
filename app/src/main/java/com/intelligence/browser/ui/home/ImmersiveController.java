package com.intelligence.browser.ui.home;

import androidx.core.content.ContextCompat;

import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.R;
import com.intelligence.browser.webview.TabControl;
import com.intelligence.browser.base.UI;
import com.intelligence.commonlib.tools.AppBarThemeHelper;
import com.intelligence.commonlib.tools.SystemTintBarUtils;

public class ImmersiveController {
    public static final int CHILD_DEFAULT = -1;
    public static final int CHILD_TABMODE_INCOGNITO = 0;
    public static final int CHILD_TABMODE_NORMAL = 1;


    private static ImmersiveController sInstance;
    private BrowserPhoneUi mUi;
    private TabControl mTabControl;
    private int mWebViewStatusBarColor;

    private ImmersiveController() {
    }

    public static void init(BrowserPhoneUi ui, TabControl tabControl) {
        if (sInstance == null) {
            sInstance = new ImmersiveController();
        }
        sInstance.bind(ui, tabControl);
    }

    /**
     * 用于刷新PhoneUi和TabControl，防止进程没有销毁的情况下，browserActivity执行onDestroy后，
     * 重现打开浏览器PhoneUi和TabControl没有重新赋值
     */
    private void bind(BrowserPhoneUi ui, TabControl tabControl) {
        mUi = ui;
        mTabControl = tabControl;
        mWebViewStatusBarColor = ContextCompat.getColor(mUi.getActivity(), R.color.status_bar_webview);
    }

    public static final ImmersiveController getInstance() {
        if (sInstance == null) {
            throw new RuntimeException(" ImmersiveController uninitialized .");
        }
        return sInstance;
    }

    public void changeStatus() {
        // This is used to prevent changing the navigation bar style when app switch to video fullscreen mode
        if (mUi.getVideoFullScreenState()) {
            return;
        }
        this.changeStatus(CHILD_DEFAULT);
    }

    public void changeStatus(int childStatus) {
        UI.ComboHomeViews status = mUi.getComboStatus();
        switch (status) {
            case VIEW_HIDE_NATIVE_PAGER:
                SystemTintBarUtils.setSystemBarColor(mUi.getActivity(), R.color.white);
                AppBarThemeHelper.setStatusBarLightMode(mUi.getActivity());
                break;
            case VIEW_NAV_SCREEN:
                SystemTintBarUtils.setSystemBarColor(mUi.getActivity(), R.color.black);
                AppBarThemeHelper.setStatusBarDarkMode(mUi.getActivity());
                break;
            case VIEW_NATIVE_PAGER:
                SystemTintBarUtils.setSystemBarColor(mUi.getActivity(), R.color.white);
                mWebViewStatusBarColor = ContextCompat.getColor(mUi.getActivity(), R.color.white);
                AppBarThemeHelper.setStatusBarLightMode(mUi.getActivity());
                break;
            case VIEW_WEBVIEW:
//                SystemTintBarUtils.setSystemBarColorByValue(mUi.getActivity(), mWebViewStatusBarColor);
//                AppBarThemeHelper.setStatusBarLightMode(mUi.getActivity());
                SystemTintBarUtils.setSystemBarColor(mUi.getActivity(), R.color.white);
                mWebViewStatusBarColor = ContextCompat.getColor(mUi.getActivity(), R.color.white);
                AppBarThemeHelper.setStatusBarLightMode(mUi.getActivity());
                break;
            default:
        }
    }

    public void setWebViewStatusBarColor(int color) {
        mWebViewStatusBarColor = color;
        changeStatus();
    }

}

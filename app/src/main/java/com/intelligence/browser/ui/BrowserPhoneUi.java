/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligence.browser.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.ui.update.FiveStarDialog;
import com.intelligence.browser.utils.BrowserHandler;
import com.intelligence.browser.ui.home.ImmersiveController;
import com.intelligence.browser.ui.home.OnSearchUrl;
import com.intelligence.browser.ui.home.WebViewStatusChange;
import com.intelligence.browser.ui.update.UpdateDialog;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.webview.TabControl;
import com.intelligence.browser.webview.TabNavScreen;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.NetworkUtils;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

public class BrowserPhoneUi extends BaseUi implements View.OnClickListener, TabControl.OnTabCountChangeListener,
        OnSearchUrl, PopupMenu.OnMenuItemClickListener {

    private static final String LOGTAG = "BrowserPhoneUi";
    private static final int MSG_INIT_NAVSCREEN = 100;
    private static final long ONE_DAY_TIME = 24 * 60 * 60 * 1000l;//一天是的时间戳
    private static final long DEFAULT_DAYS = 7;
    public static final String BROWSER_LAST_TIME_URL = "browser_last_url";
    private TabNavScreen mTabNavScreen;

    private int mActionBarHeight;
    boolean mShowNav = false;
    private ComboHomeViews mComboStatus = ComboHomeViews.VIEW_HIDE_NATIVE_PAGER;
    private AnimScreen mAnimScreen;
    private FiveStarDialog mStarDialog;
    private UpdateDialog mUpdateDialog;

    /**
     * @param browser
     * @param controller
     */
    public BrowserPhoneUi(Activity browser, UiController controller) {
        super(browser, controller);
        setUseQuickControls(BrowserSettings.getInstance().useQuickControls());
        TypedValue heightValue = new TypedValue();
        browser.getTheme().resolveAttribute(
                android.R.attr.actionBarSize, heightValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(heightValue.data,
                browser.getResources().getDisplayMetrics());
        mUiController.getTabControl().registerTabChangeListener(this);
        ImmersiveController.init(this, mTabControl);
        mBrowserMainPageController.registerSearchListener(this);
        if (isShowFiveStarDialog()) {
            BrowserHandler.getInstance().handlerPostDelayed(new Runnable() {
                @Override
                public void run() {
                    showFiveStarDialog();
                }
            }, 2000);
        }
        final String url = (String) SharedPreferencesUtils.get(BROWSER_LAST_TIME_URL, "");
        if(!TextUtils.isEmpty(url)) {
            mBrowserLastUrlTips.setVisibility(View.VISIBLE);
            mBrowserBottomArrow.setVisibility(View.GONE);
            BrowserHandler.getInstance().handlerPostDelayed(new Runnable() {
                @Override
                public void run() {
                    hintLastTimeTips(true);
                }
            }, 5000);
        }
        mBrowserRestoreBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintLastTimeTips(false);
                onSelect(url, false);
            }
        });
        mmBrowserLastUrlTipsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintLastTimeTips(false);
            }
        });

        mCatralate.setOnClickListener(this);
        mWeather.setOnClickListener(this);
        mTrastalte.setOnClickListener(this);
        mRili.setOnClickListener(this);
        mRili.postDelayed(new Runnable() {
            @Override
            public void run() {
                showUpdateDialog();
            }
        },1500);

    }

    @Override
    public void onResume() {
        super.onResume();
        // 如没有任何的可显示的网页，则显示主页
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            mUiController.openTabToHomePage();
        } else if (current.isNativePage()
                && (mComboStatus == ComboHomeViews.VIEW_HIDE_NATIVE_PAGER || mComboStatus == ComboHomeViews
                .VIEW_NATIVE_PAGER)) {
            if (mBrowserMainPageController.getInitStatus() == BrowserMainPageController.STATUS_EMPTY) {
                mBrowserMainPageController.initRootView();
            }
            mUiController.loadNativePage(current);
        }
        if (!BrowserSettings.getInstance().useTempExitFullscreen()) {
            mUiController.setFullscreen(BrowserSettings.getInstance().useFullscreen());
        }
    }

    @Override
    public void onPageLoadFinished(Tab tab) {
        super.onPageLoadFinished(tab);
//        initAd(tab.getUrl());
        adHintVisible(true,tab.getUrl());
    }

    @Override
    public void onPageLoadStarted(Tab tab) {
        super.onPageLoadStarted(tab);
        adHintVisible(false,tab.getUrl());
//        requestAD();
    }

    private void requestAD() {
//        AdConfig adConfig = new AdConfig();
//        adConfig.setAdListener(new AdLoadListener() {
//            @Override
//            public void setView(View view) {
//                mBrowserWebTopAd.removeAllViews();
//                mBrowserWebTopAd.addView(view);
//            }
//        }).setHeight(50)
//                .setAdParentView(mBrowserWebTopAd)
//                .setWidth(ScreenUtils.getScreenWidthDP(getActivity()))
//                .setAdId(AdListFactory.AD_BANNER_TOP_ID)
//                .setAdType(AdListFactory.AD_BANNER).setAdForbidTime(AdListFactory.AD_FORBID_TIME_TOP);
//        AdListFactory.getInstance().generateAdView(getActivity(), adConfig);
//
//
//        AdConfig bottomAdConfig = new AdConfig();
//        bottomAdConfig.setAdListener(new AdLoadListener() {
//            @Override
//            public void setView(View view) {
//                mBrowserWebBottomAd.removeAllViews();
//                mBrowserWebBottomAd.addView(view);
//            }
//        }).setHeight(60)
//                .setAdParentView(mBrowserWebBottomAd)
//                .setWidth(ScreenUtils.getScreenWidthDP(getActivity()))
//                .setAdId(AdListFactory.AD_BANNER_BOTTOM_ID)
//                .setAdType(AdListFactory.AD_BANNER).setAdForbidTime(AdListFactory.AD_FORBID_TIME_BOTTOM);
//        AdListFactory.getInstance().generateAdView(getActivity(), bottomAdConfig);
    }

    private void initAd(String url) {
//        AdConfig insertAdConfig = new AdConfig();
//        insertAdConfig.setAdListener(new AdLoadListener() {
//            @Override
//            public void setView(View view) {
////                mBrowserInsertAd.addView(view);
////                if (isWebShowing() && !isLoading() && AdConfig.isShowInsertAd(url)) {
////                    mBrowserInsertAd.setVisibility(View.VISIBLE);
////                }
//            }
//        }).setHeight(ScreenUtils.getScreenHeightDP(getActivity()))
//                .setWidth(ScreenUtils.getScreenWidthDP(getActivity()))
//                .setAdId(AdListFactory.AD_INSERT_SCREEN_BIG_ID)
//                .setAdParentView(mBrowserInsertAd)
//                .setAdType(AdListFactory.AD_INSERT);
//        AdListFactory.getInstance().generateAdView(getActivity(), insertAdConfig);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBrowserMainPageController.onPause();
        if (mBrowserLastUrlTips != null) {
            mBrowserLastUrlTips.setVisibility(View.GONE);
        }
        if (mStarDialog != null && mStarDialog.isShowing()) {
            mStarDialog.dismiss();
        }
        if(mUpdateDialog != null && mUpdateDialog.isShowing()){
            mUpdateDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onBackKey() {
        if (showingNavScreen()) {
            Tab currTab = mUiController.getTabControl().getCurrentTab();
             if (currTab != null && currTab.isNativePage()) {
                panelSwitchHome(mUiController.getTabControl().getTabPosition(currTab), true);
                return true;
            } else if (currTab != null) {
                currTab.resume();
            }

            mComboStatus = ComboHomeViews.VIEW_WEBVIEW;
            ImmersiveController.getInstance().changeStatus();
            mTabNavScreen.close(mUiController.getTabControl().getTabPosition(currTab));
            return true;
        }
        isCanScrollWebPage(false);
        if (mBrowserMainPageController.onBackKey()) {
            return true;
        }


        return super.onBackKey();
    }

    public boolean showingNavScreen() {
        return mTabNavScreen != null && mTabNavScreen.getVisibility() == View.VISIBLE;
    }

    public boolean showingNavScreenForExit() {
        return mTabNavScreen != null && mShowNav;
    }


    @Override
    public boolean dispatchKey(int code, KeyEvent event) {
        return super.dispatchKey(code, event);
    }

    @Override
    public void onProgressChanged(Tab tab) {
        super.onProgressChanged(tab);
    }

    @Override
    public void setActiveTab(final Tab tab) {
        super.setActiveTab(tab);
        //if at Nav screen show, detach tab like what showNavScreen() do.
        if (mShowNav) {
            detachTab(mActiveTab);
        }

        PreBrowserWebView view = tab.getWebView();
        // TabControl.setCurrentTab has been called before this,
        // so the tab is guaranteed to have a webview
        if (view == null) {
            return;
        }
        updateLockIconToLatest(tab);
    }

    // menu handling callbacks

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuState(mActiveTab, menu);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }

    @Override
    public void updateMenuState(Tab tab, Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onContextMenuCreated(Menu menu) {
        // byfrost DEL:
        //hideTitleBar();
    }

    @Override
    public void onContextMenuClosed(Menu menu, boolean inLoad) {
        // byfrost DEL:
        //if (inLoad) {
        //    showTitleBar();
        //}
    }

    // action mode callbacks

    @Override
    public void onActionModeStarted(ActionMode mode) {
        //this is system code show actionbar need saved, now is hide.
//        if (!isEditingUrl()) {
//            hideTitleBar();
//        } else {
//            mTitleBar.animate().translationY(mActionBarHeight);
//        }
    }

    @Override
    public void onActionModeFinished(boolean inLoad) {
    }

    @Override
    public boolean isWebShowing() {
        return super.isWebShowing() && mComboStatus == ComboHomeViews.VIEW_WEBVIEW;
    }

    @Override
    public void showWeb(boolean animate) {
        super.showWeb(animate);
        hideNavScreen(mUiController.getTabControl().getCurrentPosition(), animate);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    void showNavScreen() {
        mShowNav = true;
        mUiController.setBlockEvents(true);
        final Tab currTab = mTabControl.getCurrentTab();
        if (currTab != null) {
            if (currTab.isNativePage()) {
                wrapViewPagerScreen(currTab);
            }
            currTab.capture();
        }

        //  SystemTintBarUtils.setSystemBarColor(mActivity, R.color.browser_background_end_color);
        if (mTabNavScreen == null) {
            mTabNavScreen = new TabNavScreen(mActivity, mUiController, this);
            mCustomViewContainer.addView(mTabNavScreen, COVER_SCREEN_PARAMS);
        } else {
            mTabNavScreen.setVisibility(View.VISIBLE);
            mTabNavScreen.setAlpha(1f);
            if (mUiController.getTabControl().isIncognitoShowing()) {
                mTabNavScreen.showIncognitoTabMode();
            } else {
                mTabNavScreen.showNormalTabMode();
            }
            mTabNavScreen.setShowNavScreenAnimating(true);
            mTabNavScreen.refreshAdapter();
        }
        mTabNavScreen.setBlockEvents(false);
        if (mAnimScreen == null) {
            mAnimScreen = new AnimScreen(mActivity);
        }
        FrameLayout currContainer = null;
        if (currTab != null && currTab.isNativePage()) {
            currContainer = (FrameLayout) getHomeContainer();
        } else {
            currContainer = mContentView;
        }
        if (currTab != null) {
            Bitmap capture = currTab.getScreenshot();
            if (capture == null && !currTab.getCaptureSuccess()) {
                capture = mUiController.getTabControl().getHomeCapture();
            }
            mAnimScreen.set(capture);
        }
        if (mAnimScreen.mMain.getParent() == null) {
            mCustomViewContainer.addView(mAnimScreen.mMain, COVER_SCREEN_PARAMS);
        }
        mCustomViewContainer.setVisibility(View.VISIBLE);
        mCustomViewContainer.bringToFront();
        int fromLeft = 0;
        int fromTop = 0;
        int fromRight = getMainContent().getWidth();
        int fromBottom = getMainContent().getHeight();
        int width = mActivity.getResources().getDimensionPixelSize(R.dimen.tab_thumbnail_width);
        int height = mActivity.getResources().getDimensionPixelSize(R.dimen.tab_thumbnail_height);
        int ntth = mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_titleheight);
        int position = mTabControl.getCurrentPosition();
        if (position > 0) {
            width = (int) (width * 1.05);
            height = (int) (height * 1.05);
            ntth = (int) (ntth * 1.05);
        }
        int toLeft = (getMainContent().getWidth() - width) / 2;
        int toTop = (getMainContent().getHeight() - mActivity.getResources().getDimensionPixelSize(R.dimen.toolbar_height) + ntth - height) / 2;
        if (mActivity.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            toTop += mActivity.getResources().getDimensionPixelSize(R.dimen.navscreen_tab_views_offset);
            if (position > 0) {
                toTop += mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_height) * 1.05/ 2;
            }
        } else {
            if (position > 0) {
                toLeft += mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_width) * 1.05/ 2;
            }
        }
        int toRight = toLeft + width;
        int toBottom = toTop + height;
        int captureWidth = DisplayUtil.getScreenWidth(mActivity) < DisplayUtil.getScreenHeight(mActivity) ? DisplayUtil.getScreenWidth(mActivity) : DisplayUtil.getScreenHeight(mActivity);
        captureWidth /= 2;
        float scaleFactor = width / (float) (captureWidth);
        float scaleStart = getMainContent().getWidth() / (float) (captureWidth);
        mAnimScreen.setScaleFactor(scaleStart);
        mAnimScreen.mMain.layout(0, 0, getMainContent().getWidth(),
                getMainContent().getHeight());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(fromRight - fromLeft, fromBottom - fromTop);
        layoutParams.setMargins(0, 0, 0, 0);
        mAnimScreen.mContent.setLayoutParams(layoutParams);
        layoutParams = new RelativeLayout.LayoutParams(fromRight - fromLeft, ntth);
        layoutParams.setMargins(0, 0 - ntth, 0, 0);
        mAnimScreen.mTitle.setLayoutParams(layoutParams);
        float scaleX = width / (float) (getMainContent().getWidth() < getMainContent().getHeight() ? getMainContent().getWidth() : getMainContent().getHeight());
        float scaleY = ntth / mActivity.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        mToolbar.updateToolBarVisibility(true, false);
        if (currTab != null && currTab.isPrivateBrowsingEnabled()) {
//            mAnimScreen.mTitle.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.nav_tab_title_incognito_bg));
//            mAnimScreen.mTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        } else {
//            mAnimScreen.mTitle.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.nav_tab_title_normal_bg));
//            mAnimScreen.mTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.normal_text_color));
        }
        currContainer.setVisibility(View.GONE);
        detachTab(mActiveTab);
        AnimatorSet inanim = new AnimatorSet();
        ObjectAnimator tx = ObjectAnimator.ofInt(mAnimScreen.mContent, "left",
                fromLeft, toLeft);
        ObjectAnimator ty;
        ty = ObjectAnimator.ofInt(mAnimScreen.mContent, "top",
                fromTop, toTop);
        ObjectAnimator tr = ObjectAnimator.ofInt(mAnimScreen.mContent, "right",
                fromRight, toRight);
        ObjectAnimator tb = ObjectAnimator.ofInt(mAnimScreen.mContent, "bottom",
                fromBottom, toBottom);
        ObjectAnimator sf = ObjectAnimator.ofFloat(mAnimScreen, "scaleFactor",
                scaleStart, scaleFactor);
        ObjectAnimator ttx = ObjectAnimator.ofInt(mAnimScreen.mTitle, "left",
                fromLeft, toLeft);
        ObjectAnimator tty = ObjectAnimator.ofInt(mAnimScreen.mTitle, "top",
                fromTop - ntth, toTop - ntth);
        ObjectAnimator ttr = ObjectAnimator.ofInt(mAnimScreen.mTitle, "right",
                fromRight, toRight);
        ObjectAnimator ttb;
        ttb = ObjectAnimator.ofInt(mAnimScreen.mTitle, "bottom",
                fromTop, toTop);
        ObjectAnimator sx = ObjectAnimator.ofFloat(mAnimScreen.mTitle, "scaleX",
                1f, scaleX);
        ObjectAnimator sy = ObjectAnimator.ofFloat(mAnimScreen.mTitle, "scaleY",
                1f, scaleY);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mAnimScreen.mTitle, "alpha",
                0f, 1f);
        alpha.setDuration(1);
        ValueAnimator ntabbar = ValueAnimator.ofFloat(0f, 1f);
        ntabbar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (currTab != null && currTab.isPrivateBrowsingEnabled()) {
                    mTabNavScreen.getTabBar().setBackgroundColor(com.intelligence.browser.utils.AnimationUtils.getColor((float) valueAnimator.getAnimatedValue(),
                            mActivity.getResources().getColor(R.color.incognito_bg_color), mActivity.getResources().getColor(R.color.navscreen_backgroud_color)));
                } else {
                    mTabNavScreen.getTabBar().setBackgroundColor(com.intelligence.browser.utils.AnimationUtils.getColor((float) valueAnimator.getAnimatedValue(),
                            mActivity.getResources().getColor(R.color.toolbar_background_color), mActivity.getResources().getColor(R.color.navscreen_backgroud_color)));
                }
            }
        });
        inanim.playTogether(tx, ty, tr, tb, sf, ttx, tty, ttr, ttb, ntabbar);
        inanim.setDuration(mActivity.getResources().getInteger(R.integer.tab_animation_duration));
        inanim.setInterpolator(new FastOutSlowInInterpolator());
        AnimatorSet combo = new AnimatorSet();
        if (currTab != null && currTab.isNativePage()) {
            combo.playSequentially(alpha, inanim);
        } else {
            combo = inanim;
        }
        combo.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator anim) {
                if (mAnimScreen != null && mAnimScreen.mMain.getParent() != null) {
                    ((ViewGroup) mAnimScreen.mMain.getParent()).removeView(mAnimScreen.mMain);
                }
                finishAnimationIn();
                mTabNavScreen.setShowNavScreenAnimating(false);
                mUiController.setBlockEvents(false);
            }
        });
        combo.start();
    }

    private void finishAnimationIn() {
        if (showingNavScreen()) {
            // notify accessibility manager about the screen change
            mTabNavScreen.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
    }

    public void hideNavScreen(int position, boolean animate) {
        mShowNav = false;
        final Tab tab = mUiController.getTabControl().getTab(position);
        if (!showingNavScreen()) {
            mToolbar.updateToolBarVisibility();
            return;
        }
        // SystemTintBarUtils.setSystemBarColor(mActivity);
        if ((tab == null) || !animate) {
            if (tab != null && !tab.isNativePage()) {
                if (tab != null) {
                    setActiveTab(tab);
                } else if (mTabControl.getTabCount() > 0) {
                /*
                如果当前的TabController中tab都是HomeTab的情况会出现getCurrentTab()==null
                 */
                    if (mTabControl.getCurrentTab() == null ||
                            mTabControl.getCurrentTab().isNativePage()) {
                        return;
                    }
                    // use a fallback tab
                    setActiveTab(mTabControl.getCurrentTab());
                }
            }
            mContentView.setVisibility(View.VISIBLE);
            mUiController.setActiveTab(tab);
            finishAnimateOut();
            mToolbar.updateToolBarVisibility();
            return;
        }
        View tabview = mTabNavScreen.getTabView(tab);
        mTabNavScreen.setBlockEvents(true);
        mUiController.setBlockEvents(true);
        mUiController.setActiveTab(tab);
        final FrameLayout currContainer;
        if (tab.isNativePage()) {
            currContainer = (FrameLayout) getHomeContainer();
        } else {
            currContainer = mContentView;
        }
        if (mAnimScreen == null) {
            mAnimScreen = new AnimScreen(mActivity);
        } else {
            mAnimScreen.mMain.setAlpha(1f);
        }

        Bitmap capture = tab.getScreenshot();
        if (capture == null && !tab.getCaptureSuccess()) {
            capture = mUiController.getTabControl().getHomeCapture();
        }
        mAnimScreen.set(capture);
        if (mAnimScreen.mMain.getParent() == null) {
            mCustomViewContainer.addView(mAnimScreen.mMain, COVER_SCREEN_PARAMS);
        }
        mCustomViewContainer.bringToFront();
        mToolbar.updateToolBarVisibility(true, false);
        int toLeft = 0;
        int toTop = 0;
        int toRight = getMainContent().getWidth();
        int width = (tabview == null ? getMainContent().getWidth() : tabview.getWidth()) - mActivity.getResources().getDimensionPixelSize(R.dimen.tab_card_item_padding) * 2;
        int ntth = mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_titleheight);
        int height = (tabview == null ? getMainContent().getHeight() : tabview.getHeight()) - mActivity.getResources().getDimensionPixelSize(R.dimen.tab_card_item_padding) * 2 - ntth;
        int fromLeft = (tabview == null ? 0 : tabview.getLeft()) + mActivity.getResources().getDimensionPixelSize(R.dimen.tab_card_item_padding);
        int fromTop = (tabview == null ? getMainContent().getHeight() : tabview.getTop()) + ntth + mActivity.getResources().getDimensionPixelSize(R.dimen.tab_card_item_padding);
        int fromRight = fromLeft + width;
        int fromBottom = fromTop + height;
        int captureWidth = DisplayUtil.getScreenWidth(mActivity) < DisplayUtil.getScreenHeight(mActivity) ? DisplayUtil.getScreenWidth(mActivity) : DisplayUtil.getScreenHeight(mActivity);
        captureWidth /= 2;
        float scaleEnd = getMainContent().getWidth() / (float) (captureWidth);
        float scaleFactor = (float) width / (captureWidth);
        mAnimScreen.setScaleFactor(scaleFactor);
        int toBottom = toTop + getMainContent().getHeight();
        mAnimScreen.mMain.layout(toLeft, toTop, getMainContent().getWidth(),
                toBottom);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(fromLeft, fromTop, fromRight, fromBottom);
        mAnimScreen.mContent.setLayoutParams(layoutParams);
        layoutParams = new RelativeLayout.LayoutParams(width, ntth);
        layoutParams.setMargins(fromLeft, fromTop - ntth, fromRight, fromTop);
        mAnimScreen.mTitle.setLayoutParams(layoutParams);
        if (tab.isPrivateBrowsingEnabled()) {
//            mAnimScreen.mTitle.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.nav_tab_title_incognito_bg));
//            mAnimScreen.mTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
        } else {
//            mAnimScreen.mTitle.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.nav_tab_title_normal_bg));
//            mAnimScreen.mTitle.setTextColor(ContextCompat.getColor(mActivity, R.color.normal_text_color));
        }
        AnimatorSet set1 = new AnimatorSet();
        ObjectAnimator l = ObjectAnimator.ofInt(mAnimScreen.mContent, "left",
                fromLeft, toLeft);
        ObjectAnimator t;
        t = ObjectAnimator.ofInt(mAnimScreen.mContent, "top",
                fromTop, toTop);
        ObjectAnimator r = ObjectAnimator.ofInt(mAnimScreen.mContent, "right",
                fromRight, toRight);
        ObjectAnimator b = ObjectAnimator.ofInt(mAnimScreen.mContent, "bottom",
                fromBottom, toBottom);
        ObjectAnimator scale = ObjectAnimator.ofFloat(mAnimScreen, "scaleFactor",
                scaleFactor, scaleEnd);
        ObjectAnimator ttx = ObjectAnimator.ofInt(mAnimScreen.mTitle, "left",
                fromLeft, toLeft);
        ObjectAnimator tty = ObjectAnimator.ofInt(mAnimScreen.mTitle, "top",
                fromTop - ntth, toTop);
        ObjectAnimator ttr = ObjectAnimator.ofInt(mAnimScreen.mTitle, "right",
                fromRight, toRight);
        ObjectAnimator ttb;
        ttb = ObjectAnimator.ofInt(mAnimScreen.mTitle, "bottom",
                fromTop, toTop);

        mAnimScreen.mTitle.setAlpha(1);
        if (tab.isNativePage()) {
            mAnimScreen.mTitle.setText(R.string.home_page);
        } else {
            mAnimScreen.mTitle.setText(tab.getUrl());
        }
        if (mTabNavScreen.getTabBar().getTop() < fromBottom) {
            ValueAnimator ntabbar = ValueAnimator.ofFloat(1f, 0f);
            ntabbar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    if (tab.isPrivateBrowsingEnabled()) {
                        mTabNavScreen.getTabBar().setBackgroundColor(com.intelligence.browser.utils.AnimationUtils.getColor((float) valueAnimator.getAnimatedValue(),
                                mActivity.getResources().getColor(R.color.incognito_bg_color), mActivity.getResources().getColor(R.color.navscreen_backgroud_color)));
                    } else {
                        mTabNavScreen.getTabBar().setBackgroundColor(com.intelligence.browser.utils.AnimationUtils.getColor((float) valueAnimator.getAnimatedValue(),
                                mActivity.getResources().getColor(R.color.toolbar_background_color), mActivity.getResources().getColor(R.color.navscreen_backgroud_color)));
                    }
                }
            });
            ntabbar.setDuration(mActivity.getResources().getInteger(R.integer.navscreen_toolbar_hide));
            ntabbar.start();
        }
        set1.playTogether(l, t, r, b, scale, ttx, tty, ttr, ttb);
        set1.setDuration(mActivity.getResources().getInteger(R.integer.tab_animation_duration));
        set1.setInterpolator(new DecelerateInterpolator());
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                currContainer.setVisibility(View.VISIBLE);
                if (mAnimScreen != null && mAnimScreen.mMain.getParent() != null) {
                    ((ViewGroup) mAnimScreen.mMain.getParent()).removeView(mAnimScreen.mMain);
                }
                mAnimScreen = null;
                finishAnimateOut();
                mUiController.setBlockEvents(false);
            }
        });
        set1.start();
    }

    public void createNewTabWithNavScreen(final boolean incognito) {
        mShowNav = false;
        if (!showingNavScreen()) {
            mToolbar.updateToolBarVisibility();
            return;
        }
        mTabNavScreen.setBlockEvents(true);
        mUiController.setBlockEvents(true);
        final FrameLayout currContainer = (FrameLayout) getHomeContainer();
        currContainer.setVisibility(View.VISIBLE);
        if (mAnimScreen == null) {
            mAnimScreen = new AnimScreen(mActivity);
        } else {
            mAnimScreen.mMain.setAlpha(1f);
        }
        mAnimScreen.set(mUiController.getTabControl().getHomeCapture());
        if (mAnimScreen.mMain.getParent() == null) {
            mCustomViewContainer.addView(mAnimScreen.mMain, COVER_SCREEN_PARAMS);
        }
        mCustomViewContainer.bringToFront();
        final Tab homeTab = openNewTab(incognito);
        mTabControl.setCurrentTab(homeTab);
        mToolbar.updateToolBarVisibility(true, false);
        mToolbar.setToolbarStyle(mTabControl.isIncognitoShowing(), true);
        int toLeft = 0;
        int toTop = 0;
        int toRight = getMainContent().getWidth();
        int width = mActivity.getResources().getDimensionPixelSize(R.dimen.tab_thumbnail_width) / 3;
        int ntth = mActivity.getResources().getDimensionPixelSize(R.dimen.nav_tab_titleheight);
        int height = mActivity.getResources().getDimensionPixelSize(R.dimen.tab_thumbnail_height) / 3;
        int fromLeft = getMainContent().getWidth()/2 - ScreenUtils.dpToPxInt(mActivity,50);
        int fromRight = fromLeft + width;
        int fromBottom = getMainContent().getHeight();
        int fromTop = fromBottom - height;
        int captureWidth = DisplayUtil.getScreenWidth(mActivity) < DisplayUtil.getScreenHeight(mActivity) ? DisplayUtil.getScreenWidth(mActivity) : DisplayUtil.getScreenHeight(mActivity);
        captureWidth /= 2;
        float scaleEnd = getMainContent().getWidth() / (float) (captureWidth);
        float scaleFactor = (float) width / (captureWidth);
        mAnimScreen.setScaleFactor(scaleFactor);
        int toBottom = toTop + getMainContent().getHeight();
        mAnimScreen.mMain.layout(toLeft, toTop, getMainContent().getWidth(),
                toBottom);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(fromLeft, fromTop, fromRight, fromBottom);
        mAnimScreen.mContent.setLayoutParams(layoutParams);
        mAnimScreen.mTitle.setVisibility(View.GONE);
        AnimatorSet set1 = new AnimatorSet();
        ObjectAnimator l = ObjectAnimator.ofInt(mAnimScreen.mContent, "left",
                fromLeft, toLeft);
        ObjectAnimator t;
        t = ObjectAnimator.ofInt(mAnimScreen.mContent, "top",
                fromTop, toTop);
        ObjectAnimator r = ObjectAnimator.ofInt(mAnimScreen.mContent, "right",
                fromRight, toRight);
        ObjectAnimator b = ObjectAnimator.ofInt(mAnimScreen.mContent, "bottom",
                fromBottom, toBottom);
        ObjectAnimator scale = ObjectAnimator.ofFloat(mAnimScreen, "scaleFactor",
                scaleFactor, scaleEnd);
//        ObjectAnimator alphaA = ObjectAnimator.ofFloat(mAnimScreen.mMain, "alpha",
//                0f, 1f);
        set1.playTogether(l, t, r, b, scale);
        set1.setDuration(mActivity.getResources().getInteger(R.integer.tab_animation_duration));
        set1.setInterpolator(new FastOutSlowInInterpolator());

        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                if (mAnimScreen != null && mAnimScreen.mMain.getParent() != null) {
                    ((ViewGroup) mAnimScreen.mMain.getParent()).removeView(mAnimScreen.mMain);
                }
                mAnimScreen = null;
                finishAnimateOut();
                if (homeTab != null) {
                    panelSwitchHome(mUiController.getTabControl().getTabPosition(homeTab), true);
                }
                mUiController.setBlockEvents(false);
            }
        });
        set1.start();

    }

    private void finishAnimateOut() {
        mTabControl.setOnThumbnailUpdatedListener(null);
        mTabNavScreen.setVisibility(View.GONE);
        mCustomViewContainer.setAlpha(1f);
        mCustomViewContainer.setVisibility(View.GONE);
    }

    public Tab openNewTab(boolean incognito) {
        Tab tab;
        if (!mUiController.getTabControl().canCreateNewTab()) {
            showMaxTabsWarning();
            return null;
        }

        mUiController.setBlockEvents(true);
        if (!incognito) {
            tab = mUiController.openTab(BrowserSettings.getInstance().getHomePage(), false, false, true);
        } else {
            tab = mUiController.openTab(BrowserSettings.getInstance().getHomePage(), true, false, true);
        }
        mUiController.setBlockEvents(false);
        if (tab != null) {
            tab.setNativePage(true);
        }
        return tab;
    }


    @Override
    public boolean needsRestoreAllTabs() {
        return false;
    }

    public void toggleNavScreen() {
        if (!showingNavScreen()) {
            showNavScreen();
        } else {
            hideNavScreen(mUiController.getTabControl().getCurrentPosition(), false);
        }
    }

    /**
     * 切换不同的面板模式
     *
     * @param status BaseUi.VIEW_NATIVE_PAGER,BaseUi.VIEW_NAV_SCREEN,BaseUi.VIEW_WEBVIEW
     */
    public void panelSwitch(ComboHomeViews status, int position, boolean mAnimating) {
        Tab tab;
        boolean updateToolbarStyle = true;
        switch (status) {
            case VIEW_NAV_SCREEN:
                tab = mUiController.getCurrentTab();
                if (tab != null) {
                    tab.stop();
                }
                hideViewPager();
                if (!showingNavScreen()) {
                    showNavScreen();
                }
                updateToolbarStyle = false;
                mComboStatus = ComboHomeViews.VIEW_NAV_SCREEN;
                ImmersiveController.getInstance().changeStatus();
                isCanScrollWebPage(false);
                setArrowShow(false);
                break;
            case VIEW_WEBVIEW:
                isAreadryShowScroll = false;
                hideViewPager();
                hideNavScreen(position, mAnimating);
                updateToolbarStyle = false;
                mToolbar.setToolbarStyle(mTabControl.isIncognitoShowing(), false);
                try {
                    mToolbar.setWebView(mTabControl.getCurrentTab().getWebView().getBaseWebView());
                    mToolbar.updateToolbarBtnState();
                } catch (Exception e) {
                }
                if (mComboStatus == ComboHomeViews.VIEW_NATIVE_PAGER) {
                    attachTab(mUiController.getCurrentTab());
                }
                tab = mUiController.getCurrentTab();
                if (tab != null) {
                    tab.setNativePage(false);
                    mComboStatus = ComboHomeViews.VIEW_WEBVIEW;
                    tab.resume();
                }
                isCanScrollWebPage(false);
                setArrowShow(false);
                break;
            default:
                /**
                 * VIEW_HIDE_NATIVE_PAGER　的作用:
                 * resume前处于主界面时，当从其他的Activity切换过来的时候，没有切换到网页
                 */
                hideViewPager();
                mComboStatus = ComboHomeViews.VIEW_HIDE_NATIVE_PAGER;
                ImmersiveController.getInstance().changeStatus();
                isCanScrollWebPage(false);
                break;
        }
        if (updateToolbarStyle) {
            mToolbar.setToolbarStyle(mTabControl.isIncognitoShowing(), mTabControl.getCurrentTab().isNativePage());
            mToolbar.updateToolBarVisibility();
            mUiController.updateToolBarItemState();
        }
       /* try {
            updateStatusBarState(!BrowserSettings.getInstance().useTempExitFullscreen() && BrowserSettings
                    .getInstance().useFullscreen() && !mTabControl.getCurrentTab().isNativePage());
        } catch (Exception e) {
        }*/
    }

    /**
     * 打开主页的hometab(包装了主页的tab)
     *
     * @param position
     * @param mAnimating
     */
    public void panelSwitchHome(int position, boolean mAnimating) {
        final Tab homeTab = mUiController.getTabControl().getTab(position);
        if (homeTab != null) {
            PreBrowserWebView webView = homeTab.getWebView();
            if (webView != null && webView.isWebViewCreate()) {
                homeTab.stop();
                homeTab.getMainBrowserWebView().stopLoading();
                homeTab.getMainBrowserWebView().clearFocus();
                detachTab(homeTab);
            }
            homeTab.setNativePage(true);
        }

        /**
         * 切换到ViewPage时，需要隐藏navbar,目的时使其失去焦点
         */
        showViewPage(homeTab);
        mTabControl.setCurrentTab(homeTab);
        boolean incognitoShowing = mTabControl.isIncognitoShowing();
        mToolbar.setToolbarStyle(mTabControl.isIncognitoShowing(), true);
        mBrowserMainPageController.onIncognito(incognitoShowing);
        mToolbar.switchHome();
        mUiController.updateToolBarItemState();
        //   mUiController.notifyPageChanged();

        mComboStatus = ComboHomeViews.VIEW_NATIVE_PAGER;

        ImmersiveController.getInstance().changeStatus();
//        updateStatusBarState(false);
        hideNavScreen(position, mAnimating);
        checkShowScrollTips();
        initScreenStyle();
        restoreScreenStyle();
    }

    private void checkShowScrollTips() {
        boolean canGoForward = mUiController.canGoForward();
        isCanScrollWebPage(canGoForward);
    }

    @Override
    public void showAndOpenUrl(String url, boolean isNewTab) {
        if (!isNewTab) {
            Tab tab = mTabControl.getCurrentTab();
            if (tab != null && tab.isNativePage()) {
                onSelect(url, false);
            }
        } else {
            hideViewPager();
            hideNavScreen(mTabControl.getCurrentPosition(), false);
        }
    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_icon:
                mUiController.startVoiceRecognizer();
                break;
            case R.id.browser_tools_catralate:
                SchemeUtil.jumpToScheme(mActivity,"https://m.123cha.com/jsq/");
                mMainToolsView.setVisibility(View.GONE);
                break;
            case R.id.browser_tools_traslate:
                SchemeUtil.jumpToWord(mActivity,"https://fanyi.baidu.com");
                mMainToolsView.setVisibility(View.GONE);
                break;
            case R.id.browser_tools_rili:
                SchemeUtil.jumpToWord(mActivity,"日历");
                mMainToolsView.setVisibility(View.GONE);
                break;
            case R.id.browser_tools_weather:
                SchemeUtil.jumpToWord(mActivity,"天气");
                mMainToolsView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onVoiceResult(String result) {
        super.onVoiceResult(result);
        onSelect(result, false);
    }

    @Override
    public void onSelect(String url, boolean isInputUrl) {
        onSelect(url, isInputUrl, "");
    }

    public void onSelect(String url, boolean isInputUrl, String inputWord) {
        Tab t = mTabControl.getCurrentTab();
        if (t == null) return;
        if (t.isNativePage()) {
            mTabControl.recreateWebView(t);
        }
        if (isInputUrl) {
            t.setWebViewStatusChange(new WebViewStatusChange(inputWord));
        }
        t.setNativePage(false);
        mUiController.openTab(t, url);
    }

    @Override
    public void onSelectIncognito(String url) {
        Tab t = mUiController.openTab(url, false, false, false);
    }

    @Override
    public void onTabCountUpdate(int tabCount) {
        if (mTabNavScreen != null) {
            mTabNavScreen.onTabCountUpdate(tabCount);
        }
    }

    @Override
    public void onQrUrl(String url) {
        onSelect(url, false);
    }

    public void openViewPage() {
        if (!mUiController.getTabControl().canCreateNewTab()) {
            showMaxTabsWarning();
            return;
        }

        mUiController.setBlockEvents(true);
        mUiController.openTabToHomePage();
        mUiController.setBlockEvents(false);
    }

    public ComboHomeViews getComboStatus() {
        return this.mComboStatus;
    }

    static class AnimScreen {

        private View mMain;
        private ImageView mContent;
        private TextView mTitle;
        @Keep
        private float mScale;

        public AnimScreen(Context ctx) {
            mMain = LayoutInflater.from(ctx).inflate(R.layout.browser_anim_screen,
                    null);
            mTitle = mMain.findViewById(R.id.title_anim);
            mContent = mMain.findViewById(R.id.content);
            mContent.setScaleType(ImageView.ScaleType.MATRIX);
            mContent.setImageMatrix(new Matrix());
            mScale = 1.0f;
            setScaleFactor(getScaleFactor());
        }

        public void set(Bitmap image) {
            mContent.setImageBitmap(image);
        }

        @Keep
        private void setScaleFactor(float sf) {
            mScale = sf;
            Matrix m = new Matrix();
            m.postScale(sf, sf);
            mContent.setImageMatrix(m);
        }

        @Keep
        private float getScaleFactor() {
            return mScale;
        }

    }

    private boolean isShowFiveStarDialog() {
        boolean showedStatus = (boolean) SharedPreferencesUtils.get(SharedPreferencesUtils.FIVE_STAR_DIALOG_IS_SHOWED, false);
        if(showedStatus){
            return false;
        }
        int openCount = (int)SharedPreferencesUtils.get(SharedPreferencesUtils.FIVE_STAR_OPEN_COUNT, 0);
        long lastTime = (long)SharedPreferencesUtils.get(SharedPreferencesUtils.FIVE_STAR_LAST_TIME, 0l);
        if (openCount > 1 && !DateUtils.isToday(lastTime)) {
            return true;
        }
        if(!DateUtils.isToday(lastTime)) {
            openCount++;
            SharedPreferencesUtils.put(SharedPreferencesUtils.FIVE_STAR_OPEN_COUNT, openCount);
        }
        return false;
    }

    private void showFiveStarDialog() {
        if (mComboStatus != ComboHomeViews.VIEW_NATIVE_PAGER) {
            return;
        }
        mStarDialog = new FiveStarDialog(mActivity);
        SharedPreferencesUtils.put(SharedPreferencesUtils.FIVE_STAR_DIALOG_IS_SHOWED, true);
        mStarDialog.setCancelable(false);
        mStarDialog.show();
    }

    private void showUpdateDialog() {
        if (checkUpdate()) {
            mUpdateDialog = new UpdateDialog(mActivity);
            mUpdateDialog.setCancelable(false);
            mUpdateDialog.show();
            SharedPreferencesUtils.put(SharedPreferencesUtils.BROWSER_UPDATE_SHOWED, System.currentTimeMillis());
        }
    }

    private boolean checkUpdate() {
        int updateShowCount = (int) SharedPreferencesUtils.get(SharedPreferencesUtils.BROWSER_UPDATE_SHOWED_COUNT, 0);
        long updateShowTime = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.BROWSER_UPDATE_SHOWED, 0l);
        if (DeviceInfoUtils.getAppVersionCode(getActivity()) < SharedPreferencesUtils.getVersionCode()
                && NetworkUtils.isWifiConnect()
                && updateShowCount < 3
                && !DateUtils.isToday(updateShowTime)) {
            SharedPreferencesUtils.put(SharedPreferencesUtils.BROWSER_UPDATE_SHOWED_COUNT, updateShowCount++);
            return true;
        }
        return false;
    }
}

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.intelligence.browser.ui.webview.WebviewVideoPlayerLayer;
import com.intelligence.browser.webview.BaseWebView;
import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.ui.activity.BrowserSearchActivity;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.home.ToolBar;
import com.intelligence.browser.webview.Controller;
import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.webview.Tab.SecurityState;
import com.intelligence.browser.base.FullscreenListener;
import com.intelligence.browser.base.UI;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.downloads.BrowserNetworkStateNotifier;
import com.intelligence.browser.ui.activity.BrowserComboActivity;
import com.intelligence.browser.ui.view.BrowserErrorConsoleView;
import com.intelligence.browser.ui.webview.WebviewFullScreenLayerWebview;
import com.intelligence.browser.ui.webview.JsInterfaceInject;
import com.intelligence.browser.ui.home.ImmersiveController;
import com.intelligence.browser.ui.search.SearchEngines;
import com.intelligence.browser.ui.search.SelectWebSitePopWindow;
import com.intelligence.browser.utils.LazyTaskHandler;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.ScrollFrameLayout;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.browser.webview.TabControl;
import com.intelligence.commonlib.baseUi.AdvertiseFrameLayout;
import com.intelligence.commonlib.tools.AppBarThemeHelper;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.SystemTintBarUtils;
import com.intelligence.news.NetConfig;
import com.intelligence.news.hotword.HotWordHttpRequest;
import com.intelligence.news.news.cards.HotWordCardView;
import com.intelligence.news.news.mode.NewsData;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.List;
import java.util.Map;

public abstract class BaseUi implements UI, FullscreenListener, SearchEngines.IDefaultEngineIconUpdateListener,
        ScrollFrameLayout.IScrollListener {

    private static final String LOGTAG = "BaseUi";
    public static final String INPUT_SEARCH_URL = "url";
    public static final String INPUT_SEARCH_INCOGNITO = "incognito";

    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);

    Activity mActivity;
    UiController mUiController;
    TabControl mTabControl;
    protected Tab mActiveTab;
    private InputMethodManager mInputManager;

    private Drawable mLockIconSecure;
    private Drawable mLockIconMixed;
    protected Drawable mGenericFavicon;

    protected FrameLayout mContentView;
    protected FrameLayout mCustomViewContainer;
    protected FrameLayout mFullscreenContainer;
    protected WebviewVideoPlayerLayer mToolLayer;
    protected ScrollFrameLayout mMainContent;
    private FrameLayout mHomepageContainer;

    protected final BrowserMainPageController mBrowserMainPageController;

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private LinearLayout mErrorConsoleContainer = null;
    private int mOriginalOrientation;

    private UrlBarAutoShowManager mUrlBarAutoShowManager;

    private Toast mStopToast;

    private Map<PreBrowserWebView, JsInterfaceInject> mJsObjectMap = null;

    // the default <video> poster
    private Bitmap mDefaultVideoPoster;
    // the video progress view
    private View mVideoProgressView;

    protected ToolBar mToolbar;
    private boolean mActivityPaused;
    protected boolean mUseQuickControls;
    private boolean mBlockFocusAnimations;
    private final static int mFullScreenImmersiveSetting =
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | //隐藏底部NavigationBar
            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private boolean mVideoFullScreen = false;
    private int mLazyLayout = 0;
    private int mKeyboardHeight = 0;
    private SwipeRefreshLayout mSwipeRefresh;
    protected View mBrowserLastUrlTips;
    protected View mBrowserRestoreBotton;
    protected View mmBrowserLastUrlTipsClose;
    protected View mBrowserBottomArrow;
    protected AdvertiseFrameLayout mBrowserWebTopAd;
    protected AdvertiseFrameLayout mBrowserWebBottomAd;
    protected FrameLayout mBrowserInsertAd;

    protected View mMainToolsView;
    protected View mCatralate;
    protected View mTrastalte;
    protected View mRili;
    protected View mWeather;
    private View mGoWebView;
    private View mWebViewRootLayout;
    public BaseUi(Activity browser, UiController controller) {
        mBrowserMainPageController = controller.getViewPageController();
        mActivity = browser;
        mUiController = controller;
        mUiController.registerFullscreenListener(this);
        mTabControl = controller.getTabControl();
        Resources res = mActivity.getResources();
        mInputManager = (InputMethodManager)
                browser.getSystemService(Activity.INPUT_METHOD_SERVICE);
        mLockIconSecure = res.getDrawable(R.drawable.browser_ic_secure_holo_dark);
        mLockIconMixed = res.getDrawable(R.drawable.browser_ic_secure_partial_holo_dark);
        FrameLayout frameLayout = mActivity.getWindow()
                .getDecorView().findViewById(android.R.id.content);
        LayoutInflater.from(mActivity)
                .inflate(R.layout.browser_custom_screen, frameLayout);
        mMainContent = frameLayout.findViewById(R.id.custom_screen);
        mGoWebView = frameLayout.findViewById(R.id.browser_go_webview);
        mGoWebView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUiController != null) {
                    mUiController.goForward();
                }
            }
        });
        mCatralate = frameLayout.findViewById(R.id.browser_tools_catralate);
        mTrastalte = frameLayout.findViewById(R.id.browser_tools_traslate);
        mRili = frameLayout.findViewById(R.id.browser_tools_rili);
        mWeather = frameLayout.findViewById(R.id.browser_tools_weather);
        mMainToolsView = frameLayout.findViewById(R.id.browser_tools_bottom_rootview);
        mMainToolsView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMainToolsView.setVisibility(View.GONE);
            }
        });
        mBrowserWebTopAd = frameLayout.findViewById(R.id.browser_top_ad);
        mBrowserWebTopAd.setClosePosition(0);
        mBrowserWebBottomAd = frameLayout.findViewById(R.id.browser_bottom_ad);
        mBrowserWebBottomAd.setClosePosition(1);
        mBrowserInsertAd = frameLayout.findViewById(R.id.browser_splash_ad);
        mBrowserBottomArrow =  frameLayout.findViewById(R.id.browser_bottom_arrow);

        mBrowserBottomArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SchemeUtil.jumpToScheme(getActivity(),SchemeUtil.BROWSER_SCHEME_PATH_NAVIGATION);
            }
        });
        mMainContent.setUiController(mUiController);
        mHomepageContainer = frameLayout.findViewById(R.id.homepage_container);
        mContentView = frameLayout.findViewById(
                R.id.main_content);
        mCustomViewContainer = frameLayout.findViewById(
                R.id.fullscreen_custom_content);
        mErrorConsoleContainer = frameLayout
                .findViewById(R.id.error_console);
        mBrowserLastUrlTips =  frameLayout.findViewById(R.id.browser_last_url_tips);
        mWebViewRootLayout =  frameLayout.findViewById(R.id.vertical_layout);
        mBrowserRestoreBotton=  frameLayout.findViewById(R.id.restore_webpage_botton);
        mmBrowserLastUrlTipsClose =  frameLayout.findViewById(R.id.browser_last_url_tips_close);
        mSwipeRefresh = frameLayout.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefresh.setColorSchemeResources(R.color.badge_red_color);
        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefresh.setRefreshing(false);
                        if(mUiController != null){
                            mUiController.refreshWebview();
                        }
                    }
                },1000);

            }
        });
        mGenericFavicon = res.getDrawable(
                R.drawable.browser_toolbar_webview_home);
        mToolbar = frameLayout.findViewById(R.id.bottom_bar).findViewById(R.id.tool_bar);
        mToolbar.setUicontroller(mUiController);
        mUrlBarAutoShowManager = new UrlBarAutoShowManager(this);
        mJsObjectMap = new ArrayMap<>();
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ++mLazyLayout;
                handleSpeWebInputHeight(frameLayout);
                // Execute Lazy Tasks after 3 layout.
                if (mLazyLayout == 2) {
                    LazyTaskHandler.executeLazyTask();
                }
            }
        });
        SearchEngines.getInstance(mActivity).registerIconUpdateListener(this);

        if (mMainContent != null) {
            mMainContent.registerScrollListener(this);
        }
        HotWordHttpRequest.refreshHotWord();
//        initHotword(frameLayout);
    }

    private void handleSpeWebInputHeight(View frameLayout) {
        try {
            Rect rect = new Rect();
            frameLayout.getWindowVisibleDisplayFrame(rect);

            // 屏幕的总高度
            int screenHeight = frameLayout.getRootView().getHeight();

            // 计算出键盘高度
            int keypadHeight = screenHeight - rect.bottom;
            if (keypadHeight == 0) {
                restoreWebViewHeight();
            } else {
                if (mUiController != null && mUiController.getCurrentTab() != null
                        && !TextUtils.isEmpty(mUiController.getCurrentTab().getUrl())
                        && mUiController.getCurrentTab().getUrl().toLowerCase().contains("chatgpt")) {
                    mContentView.getLayoutParams().height = ScreenUtils.getScreenHeight(getActivity()) - keypadHeight;
                    mContentView.requestLayout();
                } else {
                    restoreWebViewHeight();
                }
            }
        } catch (Exception e) {
            restoreWebViewHeight();
        }
    }

    private void restoreWebViewHeight(){
        if (mContentView != null) {
            changeWebViewHeight();
        }
    }

    private HotWordCardView mHotword;

    private void initHotword(View view){
        mHotword = view.findViewById(R.id.mainpager_hotword);
        NewsData newsData = new NewsData();
        newsData.mediaType =  NetConfig.PAGE_WEIBO;
        mHotword.updateData(newsData);
    }

    public boolean isAreadryShowScroll = false;
    public void isCanScrollWebPage(boolean isShowGoWebview) {
//        if(BrowserSettings.getInstance().getSwipeState()){
//            if (mGoWebView != null) {
//                mGoWebView.setVisibility( View.GONE);
//            }
//            return;
//        }
        if(isAreadryShowScroll){
            if (mGoWebView != null) {
                mGoWebView.setVisibility( View.GONE);
            }
            return;
        }
        if(isShowGoWebview) {
            isAreadryShowScroll = isShowGoWebview;
        }
        if (mGoWebView != null) {
            mGoWebView.setVisibility(isShowGoWebview ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void toobarunfold(float isOpen) {
//        ViewGroup.MarginLayoutParams layout = (ViewGroup.MarginLayoutParams) mBrowserWebBottomAd.getLayoutParams();
//        layout.bottomMargin =  (int) (ScreenUtils.dpToPxInt(getActivity(), 24) + ((1 - isOpen)*ScreenUtils.dpToPxInt(getActivity(), 24)));
//        mBrowserWebBottomAd.setLayoutParams(layout);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {

    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return false;
    }

    @Override
    public void onQrUrl(String url) {

    }

    @Override
    public void onSelectIncognito(String url) {

    }

    @Override
    public void showAndOpenUrl(String url, boolean isNewTab) {

    }

    @Override
    public void openViewPage() {

    }

    public View getMainContent() {
        return mMainContent;
    }

    public View getHomePageContainer() {
        return mHomepageContainer;
    }

    private void cancelStopToast() {
        if (mStopToast != null) {
            mStopToast.cancel();
            mStopToast = null;
        }
    }

    public ViewGroup getHomeContainer() {
        return mHomepageContainer;
    }

    @Override
    public void onStop() {
        cancelStopToast();
        mActivityPaused = true;
    }

    protected boolean isFullVideo;

    public void onPause() {
        initScreenStyle();
        restoreScreenStyle();
    }

    protected void initScreenStyle(){
        if (mToolLayer != null) {
            isFullVideo = true;
        }
        isFullVideo = isLandscape();
    }

    public void onResume() {
        if(mMainContent != null){
            mMainContent.onResume();
        }
        mActivityPaused = false;
        // check if we exited without setting active tab
        // b: 5188145
        final Tab ct = mTabControl.getCurrentTab();
        if (ct != null && !ct.isNativePage()) {
            setActiveTab(ct);
        }
        mBrowserMainPageController.onResume();
        if (mToolbar != null) {
            restoreScreenStyle();
            mToolbar.onResume();
        }
        isCanScrollWebPage(false);
    }

    protected void restoreScreenStyle(){
        if (isFullVideo && mToolbar != null) {
            ScreenUtils.setLandscape(getActivity());
            mToolbar.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        onHideCustomView();
                        updateStatusBarState(!BrowserSettings.getInstance().getShowStatusBar());
                    }catch (Exception e){
                    }
                }
            });
        }
    }
    private boolean isLandscape() {
        int phoneHeight = DisplayUtil.getScreenHeight(mActivity);
        int phoneWidth = DisplayUtil.getScreenWidth(mActivity);
        return ScreenUtils.isLandscape(getActivity()) || phoneWidth > phoneHeight;
    }

    protected boolean isActivityPaused() {
        return mActivityPaused;
    }

    public void changeWebViewHeight() {
        changeWebViewHeight(0);
//        mSwipeRefresh.setEnabled(true);
    }

    public void changeWebViewIsTop(boolean isTop) {
//        mSwipeRefresh.setEnabled(isTop);
    }

    public void changeWebViewHeight(int keyHeight) {
        if (mToolbar == null) {
            return;
        }
        int phoneHeight = DisplayUtil.getScreenHeight(mActivity);
        int phoneWidth = DisplayUtil.getScreenWidth(mActivity);
        int state = mToolbar.getToolbarState();
        int toolbarHeight = mActivity.getResources().getDimensionPixelOffset(R.dimen.bottom_toolbar_height);
        int toolbarFoldHeight = mActivity.getResources().getDimensionPixelOffset(R.dimen.bottom_toolbar_scroll_animator_distance);
        LayoutParams params = mContentView.getLayoutParams();

        int height = phoneHeight;

//        if (BrowserSettings.getInstance().getShowStatusBar()) {
//            height -= DisplayUtil.getStatusBarHeight(mActivity);
//        }

        if (state == ToolBar.STATE_DOWNING || state == ToolBar.STATE_DOWN) {
            height -= toolbarHeight;
        } else if (state == ToolBar.STATE_UPPING || state == ToolBar.STATE_UP) {
            height -= toolbarFoldHeight;
        }
        params.height -= keyHeight;
        if (params.height == height) {
            return;
        }
        params.height = height;
        params.width = phoneWidth;
        mContentView.setLayoutParams(params);
        mContentView.requestLayout();
    }

    public Activity getActivity() {
        return mActivity;
    }

    @Override
    public boolean onBackKey() {
        if (mSelectSearchEngine != null && mSelectSearchEngine.isShow()) {
            mSelectSearchEngine.dismiss();
            return true;
        }
        if (mCustomView != null) {
            mUiController.hideCustomView();
            return true;
        }
        return false;
    }

    @Override
    public void onNetworkToggle(boolean up) {
    }

    @Override
    public boolean onMenuKey() {
        return false;
    }

    @Override
    public void setUseQuickControls(boolean useQuickControls) {
        mUseQuickControls = useQuickControls;
        updateUrlBarAutoShowManagerTarget();
    }

    // Tab callbacks
    @Override
    public void onTabDataChanged(Tab tab) {
        setFavicon(tab);
        updateLockIconToLatest(tab);
        updateNavigationState(tab);
        onProgressChanged(tab);
        mUiController.updateToolBarItemState();
        if (mToolbar != null) {
            mToolbar.setUrlTitle(tab);
        }
    }

    @Override
    public void onProgressChanged(Tab tab) {
        int progress = tab.getLoadProgress();
        if (tab.inForeground()) {
            if (mToolbar != null) {
                mToolbar.setProgress(progress);
            }
        }

    }

    @Override
    public void bookmarkedStatusHasChanged(Tab tab) {
        if (tab.inForeground()) {
            boolean isBookmark = tab.isBookmarkedSite();
        }
    }

    @Override
    public void onPageStopped(Tab tab) {
        cancelStopToast();
        if (tab.inForeground()) {
            if (mStopToast == null) {
                mStopToast = ToastUtil.toastShow(mActivity, R.string.stopping, Toast.LENGTH_SHORT);
            }
            mStopToast.show();
        }
    }

    @Override
    public boolean needsRestoreAllTabs() {
        return true;
    }

    @Override

    public void addTab(Tab tab) {
    }

    @Override
    public void setActiveTab(final Tab tab) {
        if (tab == null) return;
        // block unnecessary focus change animations during tab switch
        mBlockFocusAnimations = true;
        if ((tab != mActiveTab) && (mActiveTab != null)) {
            removeTabFromContentView(mActiveTab);
            PreBrowserWebView web = mActiveTab.getWebView();
            if (web != null) {
                web.setOnTouchListener(null);
            }
        }
        mActiveTab = tab;
        PreBrowserWebView web = mActiveTab.getWebView();
//        updateUrlBarAutoShowManagerTarget();
        attachTabToContentView(tab);
        if (!tab.isNativePage() && tab.getTopWindow() != null) {
            tab.getTopWindow().requestFocus();
        }
        setShouldShowErrorConsole(tab, mUiController.shouldShowErrorConsole());
        onTabDataChanged(tab);
        onProgressChanged(tab);
        mBlockFocusAnimations = false;
    }

    protected void updateUrlBarAutoShowManagerTarget() {
        PreBrowserWebView web = mActiveTab != null ? mActiveTab.getWebView() : null;
        if (!BrowserSettings.getInstance().useFullscreen()) {
            mUrlBarAutoShowManager.setTarget(null);
            return;
        }
        if (!mUseQuickControls && web != null) {
            mUrlBarAutoShowManager.setTarget(web);
        } else {
            mUrlBarAutoShowManager.setTarget(null);
        }
    }

    Tab getActiveTab() {
        return mActiveTab;
    }

    @Override
    public void updateTabs(List<Tab> tabs) {
    }

    @Override
    public void removeTab(Tab tab) {
        if (mActiveTab == tab) {
            removeTabFromContentView(tab);
            mActiveTab = null;
        }
    }

    @Override
    public void detachTab(Tab tab) {
        removeTabFromContentView(tab);
    }

    @Override
    public void attachTab(Tab tab) {
        attachTabToContentView(tab);
    }

    protected void attachTabToContentView(Tab tab) {
        if ((tab == null) || (tab.getWebView() == null)) {
            return;
        }
        View container = tab.getViewContainer();
        PreBrowserWebView mainView = tab.getWebView();

        // Attach the WebView to the container and then attach the
        // container to the content view.
        FrameLayout wrapper =
                container.findViewById(R.id.webview_wrapper);
        ViewGroup parent = (ViewGroup) mainView.getParent();
        if (parent != wrapper) {
            if (parent != null) {
                parent.removeView(mainView);
            }
            wrapper.addView(mainView);
        }

        parent = (ViewGroup) container.getParent();
        if (parent != mContentView) {
            if (parent != null) {
                parent.removeView(container);
            }
            mContentView.addView(container, COVER_SCREEN_PARAMS);
        }
        mContentView.setVisibility(View.VISIBLE);
        mUiController.attachSubWindow(tab);
    }

    private void removeTabFromContentView(Tab tab) {
        if (tab == null) return; // 如果tab==null情况不需要要执行移除
        // Remove the container that contains the main WebView.
        PreBrowserWebView mainView = tab.getWebView();
        View container = tab.getViewContainer();
        if (container != null) {
            FrameLayout wrapper =
                    container.findViewById(R.id.webview_wrapper);
            if (wrapper != null && mainView != null) {
                wrapper.removeView(mainView);
            }
            if (mContentView != null) {
                mContentView.removeView(container);
            }
        }
        // Remove the container from the content and then remove the
        // WebView from the container. This will trigger a focus change
        // needed by WebView.
        mUiController.endActionMode();
        mUiController.removeSubWindow(tab);
        BrowserErrorConsoleView errorConsole = tab.getErrorConsole(false);
        if (mErrorConsoleContainer != null && errorConsole != null) {
            mErrorConsoleContainer.removeView(errorConsole);
        }
    }

    @Override
    public void onSetWebView(Tab tab, PreBrowserWebView webView) {
        View container = tab.getViewContainer();
        mSwipeRefresh.setEnabled(false);
        if (container == null) {
            // The tab consists of a container view, which contains the main
            // WebView, as well as any other UI elements associated with the tab.
            container = mActivity.getLayoutInflater().inflate(R.layout.browser_tab_view,
                    mContentView, false);
            tab.setViewContainer(container);
        }
        if (tab.getWebView() != webView) {
            // Just remove the old one.
            FrameLayout wrapper =
                    container.findViewById(R.id.webview_wrapper);
            wrapper.removeView(tab.getWebView());
        }
    }

    /**
     * create a sub window container and webview for the tab
     * Note: this methods operates through side-effects for now
     * it sets both the subView and subViewContainer for the given tab
     *
     * @param tab     tab to create the sub window for
     * @param subView webview to be set as a subwindow for the tab
     */
    @Override
    public void createSubWindow(Tab tab, PreBrowserWebView subView) {
        View subViewContainer = mActivity.getLayoutInflater().inflate(
                R.layout.browser_subwindow_view, null);
        ViewGroup inner = subViewContainer
                .findViewById(R.id.inner_container);
        inner.addView(subView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        final ImageButton cancel = subViewContainer
                .findViewById(R.id.subwindow_close);
        final PreBrowserWebView cancelSubView = subView;
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseWebView webView = cancelSubView.getBaseWebView();
                if (webView != null && webView.getWebChromeClient() != null) {
                    webView.getWebChromeClient().onCloseWindow(webView);
                }
            }
        });
        tab.setSubWebView(subView);
        tab.setSubViewContainer(subViewContainer);
    }

    /**
     * Remove the sub window from the content view.
     */
    @Override
    public void removeSubWindow(View subviewContainer) {
        mContentView.removeView(subviewContainer);
        mUiController.endActionMode();
    }

    /**
     * Attach the sub window to the content view.
     */
    @Override
    public void attachSubWindow(View container) {
        if (container.getParent() != null) {
            // already attached, remove first
            ((ViewGroup) container.getParent()).removeView(container);
        }
        mContentView.addView(container, COVER_SCREEN_PARAMS);
    }

    protected void refreshWebView() {
        PreBrowserWebView web = getWebView();
        if (web != null) {
            web.invalidate();
        }
    }

    public View getContentView() {
        return mContentView;
    }

    @Override
    public void showComboView(ComboViews startingView, Bundle extras) {
        Intent intent = new Intent(mActivity, BrowserComboActivity.class);
        intent.putExtra(BrowserComboActivity.EXTRA_INITIAL_VIEW, startingView.name());
        intent.putExtra(BrowserComboActivity.EXTRA_COMBO_ARGS, extras);
        Tab t = getActiveTab();
        if (t != null) {
            intent.putExtra(BrowserComboActivity.EXTRA_CURRENT_URL, t.getUrl());
        }
        mActivity.startActivityForResult(intent, Controller.COMBO_VIEW);
    }

    @Override
    public void showCustomView(View view, int requestedOrientation,
                               WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        mVideoFullScreen = true;
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        mOriginalOrientation = mActivity.getRequestedOrientation();
        compositeFullScreenLayer(view);

        mCustomView = view;
        updateStatusBarState(true);
        if (getWebView() != null) {
            getWebView().setVisibility(View.INVISIBLE);
        }
        mCustomViewCallback = callback;
        int videoType = BrowserSettings.getInstance().getUserVideo();
        if (videoType == 0 || videoType == 1) {
            ScreenUtils.setLandscape(getActivity());
        }
    }

    @Override
    public void onHideCustomView() {
        mVideoFullScreen = false;
        ScreenUtils.setPortrait(getActivity());
        if (getWebView() != null) {
            getWebView().setVisibility(View.VISIBLE);
        }
        if (mCustomView == null)
            return;

        updateStatusBarState(!BrowserSettings.getInstance().getShowStatusBar()); //视频播放返回需重置全屏状态
        removeFullScreenLayer();
    }

    @Override
    public boolean isCustomViewShowing() {
        return mCustomView != null;
    }

    protected void dismissIME() {
        if (mInputManager.isActive()) {
            mInputManager.hideSoftInputFromWindow(mContentView.getWindowToken(),
                    0);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        changeWebViewHeight(0);
    }

    @Override
    public boolean isWebShowing() {
        return mCustomView == null;
    }

    // -------------------------------------------------------------------------

    protected void updateNavigationState(Tab tab) {
    }

    /**
     * Update the lock icon to correspond to our latest state.
     */
    protected void updateLockIconToLatest(Tab t) {
        if (t != null && t.inForeground()) {
            updateLockIconImage(t.getSecurityState());
        }
    }

    /**
     * Updates the lock-icon image in the title-bar.
     */
    private void updateLockIconImage(SecurityState securityState) {
        Drawable d = null;
        if (securityState == SecurityState.SECURITY_STATE_SECURE) {
            d = mLockIconSecure;
        } else if (securityState == SecurityState.SECURITY_STATE_MIXED
                || securityState == SecurityState.SECURITY_STATE_BAD_CERTIFICATE) {
            // TODO: It would be good to have different icons for insecure vs mixed content.
            // See http://b/5403800
            d = mLockIconMixed;
        }
    }

    // Set the favicon in the title bar.
    protected void setFavicon(Tab tab) {
        if (tab.inForeground()) {
            Bitmap icon = tab.getFavicon();
            // this version not need show icon
//            mNavigationBar.setFavicon(icon);
        }
    }

    @Override
    public void onActionModeFinished(boolean inLoad) {
    }

    // active tabs page

    public void showActiveTabsPage() {
    }

    /**
     * Remove the active tabs page.
     */
    public void removeActiveTabsPage() {
    }

    // menu handling callbacks

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void updateMenuState(Tab tab, Menu menu) {
    }

    @Override
    public void onOptionsMenuOpened() {
    }

    @Override
    public void onExtendedMenuOpened() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onOptionsMenuClosed(boolean inLoad) {
    }

    @Override
    public void onExtendedMenuClosed(boolean inLoad) {
    }

    @Override
    public void onContextMenuCreated(Menu menu) {
    }

    @Override
    public void onContextMenuClosed(Menu menu, boolean inLoad) {
    }

    // error console

    @Override
    public void setShouldShowErrorConsole(Tab tab, boolean flag) {
        if (tab == null) return;
        BrowserErrorConsoleView errorConsole = tab.getErrorConsole(true);
        if (flag) {
            // Setting the show state of the console will cause it's the layout
            // to be inflated.
            if (errorConsole.numberOfErrors() > 0) {
                errorConsole.showConsole(BrowserErrorConsoleView.SHOW_MINIMIZED);
            } else {
                errorConsole.showConsole(BrowserErrorConsoleView.SHOW_NONE);
            }
            if (errorConsole.getParent() != null) {
                mErrorConsoleContainer.removeView(errorConsole);
            }
            // Now we can add it to the main view.
            mErrorConsoleContainer.addView(errorConsole,
                    new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT));
        } else {
            mErrorConsoleContainer.removeView(errorConsole);
        }
    }

    // -------------------------------------------------------------------------
    // Helper function for WebChromeClient
    // -------------------------------------------------------------------------

    @Override
    public Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = BitmapFactory.decodeResource(
                    mActivity.getResources(), R.drawable.browser_default_video_poster);
        }
        return mDefaultVideoPoster;
    }

    @Override
    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            mVideoProgressView = inflater.inflate(
                    R.layout.browser_video_loading_progress, null);
        }
        return mVideoProgressView;
    }

    @Override
    public void showMaxTabsWarning() {
        ToastUtil.showShortToast(mActivity, R.string.max_tabs_warning);
    }

    public PreBrowserWebView getWebView() {
        if (mActiveTab != null) {
            return mActiveTab.getWebView();
        } else {
            return null;
        }
    }

    public BaseWebView getBrowserWebView() {
        if (mActiveTab == null) {
            return null;
        }
        return mActiveTab.getMainBrowserWebView();
    }

    protected Menu getMenu() {
        return null;
        //MenuBuilder menu = new MenuBuilder(mActivity);
        //mActivity.getMenuInflater().inflate(R.menu.browser, menu);
        //return menu;
    }

    @Override
    public void onFullscreenChange(boolean enabled) {
//        updateStatusBarState(true);
    }

    public void updateStatusBarState(boolean enabled) {
        Window win = mActivity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        if (enabled) {
            winParams.flags |= bits;
            //全屏
            winParams.flags |= bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(mFullScreenImmersiveSetting);
            } else {
                if (mCustomView != null) {
                    mContentView.setSystemUiVisibility(mFullScreenImmersiveSetting);
                }
            }
        } else {
            winParams.flags &= ~bits;
            if (mCustomView != null) {
                mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                if (mCustomView != null) {
                    mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        }
        win.setAttributes(winParams);
    }

    public Drawable getFaviconDrawable(Bitmap icon) {
        Drawable[] array = new Drawable[3];
        array[0] = new PaintDrawable(Color.BLACK);
        PaintDrawable p = new PaintDrawable(Color.WHITE);
        array[1] = p;
        if (icon == null) {
            array[2] = mGenericFavicon;
        } else {
            array[2] = new BitmapDrawable(icon);
        }
        LayerDrawable d = new LayerDrawable(array);
        d.setLayerInset(1, 1, 1, 1, 1);
        d.setLayerInset(2, 2, 2, 2, 2);
        return d;
    }

    public boolean isLoading() {
        return mTabControl.getCurrentTab() != null && mTabControl.getCurrentTab().inPageLoad();
    }

    public boolean isShowErrorPage() {
        return mTabControl.getCurrentTab() != null && mTabControl.getCurrentTab().isShowErrorPage();
    }


    @Override
    public void showWeb(boolean animate) {
        mUiController.hideCustomView();
    }

    public boolean setBookMarksStyle(){
        if(mUiController != null) {
          return mUiController.setBookMarksStyle();
        }
        return false;
    }

    public void changeScreenSize(){
        mOriginalOrientation = mActivity.getRequestedOrientation();
        if(mOriginalOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            ScreenUtils.setPortrait(getActivity());
        }else {
            ScreenUtils.setLandscape(getActivity());
        }
    }

    public boolean canAddBookmark(){
        if(mUiController != null) {
          return mUiController.canAddBookmark();
        }
        return false;
    }

    static class FullscreenHolder extends FrameLayout implements WebviewVideoPlayerLayer.Listener {
        private boolean mLockerState = false;

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (mLockerState && (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                    || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
                return true;
            }
            return super.dispatchKeyEvent(event);
        }

        @Override
        public void setLockerState(boolean lockerstate) {
            mLockerState = lockerstate;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            // dispatch to the last child first, then one by one to the other child
            if (this.getChildCount() != 0) {
                getChildAt(this.getChildCount() - 1).dispatchTouchEvent(ev);
            }
            return super.dispatchTouchEvent(ev);
        }
    }

    public void setContentViewMarginTop(int margin) {
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) mContentView.getLayoutParams();
        if (params.topMargin != margin) {
            params.topMargin = margin;
            mContentView.setLayoutParams(params);
        }
    }

    @Override
    public boolean blockFocusAnimations() {
        return mBlockFocusAnimations;
    }

    @Override
    public void onVoiceResult(String result) {
    }

    protected boolean showingViewPage() {
        return mBrowserMainPageController.isVisible();
    }

    protected void hideViewPager() {
        if (mBrowserMainPageController.getInitStatus() != BrowserMainPageController.STATUS_EMPTY) {
            mBrowserMainPageController.hideViewPager();
        }
    }

    /**
     * 在 {@link #showViewPage(Tab)} 的基础上添加了控制主页显示顶端的参数
     *
     * @param tab
     * @see {ViewPageController#onResume}
     */
    protected void showViewPage(Tab tab) {
        mBrowserMainPageController.switchTab(tab);
        mBrowserMainPageController.showViewPager();
        mBrowserMainPageController.onResumeIfNeed();
    }

    /**
     * 自动切换主界面与社区
     * 调用该方法前需要确认当前tab是home
     */
    public void togglePageSwitch() {
    }

    protected void wrapViewPagerScreen(Tab tab) {
        mBrowserMainPageController.wrapScreenshot(tab);
    }

    private SelectWebSitePopWindow mSelectSearchEngine;

    public void changeSearchEngine() {
        if (mBrowserMainPageController != null) {
            mBrowserMainPageController.updateDefaultSearchEngine();
        }
    }

    private boolean mIsShowCardTips;
    @Override
    public void showCardTipsView(boolean isShow) {
        mIsShowCardTips = isShow;
        setArrowShow(mIsShowArrow);
        if (mBrowserMainPageController != null) {
            mBrowserMainPageController.showCardTips(isShow);
        }
    }


    @Override
    public void openSelectWebSiteView(View view,WebSiteData webSiteData) {
        if (mMainContent == null) {
            return;
        }
        if (mSelectSearchEngine != null) {
            dimissSearchEngine();
        }

        mSelectSearchEngine = new SelectWebSitePopWindow(getActivity(), webSiteData,new SelectWebSiteListener() {
            @Override
            public void selectWebSite(WebSiteData webSiteData) {

            }
        });
        mSelectSearchEngine.show(view);
    }

    public void dimissSearchEngine() {
        if (mSelectSearchEngine != null) {
            mSelectSearchEngine.dismiss();
        }
    }

    protected void compositeFullScreenLayer(View view) {
//        SystemTintBarUtils.cancelSystemBarImmersive(mActivity); // reset window style
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(mActivity);
        // set focus on this view for handling the key event
        mFullscreenContainer.setFocusable(true);
        mFullscreenContainer.setFocusableInTouchMode(true);
        mFullscreenContainer.requestFocus();
        mToolLayer = new WebviewFullScreenLayerWebview(this);
        mToolLayer.setPreView(view);
        mToolLayer.setListener((WebviewVideoPlayerLayer.Listener) mFullscreenContainer);

        BrowserNetworkStateNotifier.getInstance().addEventListener((BrowserNetworkStateNotifier.NetworkStateChangedListener) mToolLayer);

        PreBrowserWebView webview = getWebView();
        if (webview != null) {
            JsInterfaceInject jsobject = mJsObjectMap.get(getWebView());
            if (jsobject != null) {
                jsobject.setListener((WebviewVideoPlayerLayer.MediaInfoListener) mToolLayer);
                mToolLayer.beginFullScreen();
            }
        }
        mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        mFullscreenContainer.addView(mToolLayer.getLayer(), COVER_SCREEN_PARAMS);
        mFullscreenContainer.setKeepScreenOn(true);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
    }

    protected void removeFullScreenLayer() {
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        mToolLayer.endFullScreen();
        mCustomViewCallback.onCustomViewHidden(); // call this first, because remove view will cause it destroyed
        if (mToolLayer != null && mToolLayer.getLayer() != null) {
            mFullscreenContainer.removeView(mToolLayer.getLayer());
        }
        if (mCustomView != null) {
            mFullscreenContainer.removeView(mCustomView);
        }
        if (mFullscreenContainer != null) {
            mFullscreenContainer.setKeepScreenOn(false);
        }
        decor.removeView(mFullscreenContainer);
        BrowserNetworkStateNotifier.getInstance().removeEventListener((BrowserNetworkStateNotifier.NetworkStateChangedListener) mToolLayer);
        mToolLayer = null;
        mCustomView = null;
        mFullscreenContainer = null;
        // Restore the status bar immersive style
        ImmersiveController.getInstance().changeStatus();
    }

    @Override
    public boolean dispatchKey(int code, KeyEvent event) {
        return mToolLayer != null && mToolLayer.dispatchKey(code, event);
    }

    public void loadJsObject(JsInterfaceInject jsObject, boolean force) {
        PreBrowserWebView webview = getWebView();
        if (webview == null) {
            return;
        }

        if (mJsObjectMap != null) {
            if (!mJsObjectMap.containsKey(webview)) {
                mJsObjectMap.put(webview, jsObject);
            } else if (force) {
                mJsObjectMap.remove(webview);
                mJsObjectMap.put(webview, jsObject);
            }
        }
    }

    public void removeJsObjectRef(Tab tab) {
        if (tab == null || mJsObjectMap == null) {
            return;
        }

        PreBrowserWebView webview = tab.getWebView();
        if (webview == null) {
            return;
        }

        mJsObjectMap.remove(webview);
    }

    public void shareCurrentPage() {
        mUiController.shareCurrentPage();
    }

    public UiController getController() {
        return mUiController;
    }

    public boolean getVideoFullScreenState() {
        return mVideoFullScreen;
    }

    @Override
    public void openSearchInputView(String url) {
        Intent intent = new Intent(mActivity, BrowserSearchActivity.class);
        intent.putExtra(INPUT_SEARCH_URL, url);
        boolean isPrivateBrowsing = false;
        if (mTabControl != null && mTabControl.getCurrentTab() != null) {
            isPrivateBrowsing = mTabControl.getCurrentTab().isPrivateBrowsingEnabled();
        }
        intent.putExtra(INPUT_SEARCH_INCOGNITO, isPrivateBrowsing);
        mActivity.startActivityForResult(intent, Controller.URL_SEARCH);
        mActivity.overridePendingTransition(R.anim.browser_url_search_input_enter, R.anim.browser_url_search_input_exit);
    }

    @Override
    public void showWebViewPage(boolean isShowWebView) {
        if(isShowWebView){
            hintLastTimeTips(false);
        }
        if (mIsFirstEdit) {
            mIsFirstEdit = false;
            return;
        }
        if (!isShowWebView && !mIsShowCardTips) {
            mBrowserMainPageController.notifyCardTips();
        }
        setArrowShow(!isShowWebView);
        if(!isShowWebView) {
            adHintVisible(false,mTabControl.getCurrentTab().getUrl());
        }
//        if(!isShowWebView) {
//            restoreScreenStyle();
//        }
    }

    private boolean mIsShowArrow;
    protected void setArrowShow(boolean isShow) {
        mIsShowArrow = isShow;
        if (mBrowserBottomArrow != null) {
            mBrowserBottomArrow.setVisibility(isShow && !mIsShowCardTips ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showTools(boolean isShow) {
        if (SharedPreferencesUtils.getRecommonWebsites()) {
            if (mMainToolsView != null) {
                mMainToolsView.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        } else {
            SchemeUtil.openHistoryPage(getActivity());
        }
    }

    @Override
    public void adHintVisible(boolean isHintVisible,String url){
//        if (isHintVisible && AdConfig.isShowWebTopAd(url) && isWebShowing() && !isLoading()  && !isShowErrorPage()) {
//            mBrowserWebTopAd.setVisibility(View.VISIBLE);
//        } else {
//            mBrowserWebTopAd.setVisibility(View.GONE);
//        }
//
//        if (isHintVisible && AdConfig.isShowWebBottomAd(url) && isWebShowing() && !isLoading()  && !isShowErrorPage()) {
//            mBrowserWebBottomAd.setVisibility(View.VISIBLE);
//        } else {
//            mBrowserWebBottomAd.setVisibility(View.GONE);
//        }
    }

    @Override
    public void isRecommendEdit(boolean isEdit) {
        if(isEdit) {
            SystemTintBarUtils.setSystemBarColorByValue(getActivity(), R.color.navigation_place_bg);
            AppBarThemeHelper.setStatusBarDarkMode(getActivity());
        }else {
            ImmersiveController.getInstance().setWebViewStatusBarColor(R.color.white);
            AppBarThemeHelper.setStatusBarLightMode(getActivity());
        }

        if(mBrowserMainPageController != null){
            mBrowserMainPageController.isRecommendEdit(isEdit);
        }

        if (mIsFirstEnty) {
            mIsFirstEnty = false;
        }
    }

    private boolean mIsFirstEnty = true;
    private boolean mIsFirstEdit = true;

    protected void hintLastTimeTips(boolean needAnim) {
        if(mBrowserLastUrlTips != null){
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mBrowserLastUrlTips.setVisibility(View.GONE);
                    setArrowShow(true);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if(needAnim && mBrowserLastUrlTips.getVisibility() == View.VISIBLE) {
                mBrowserLastUrlTips.startAnimation(alphaAnimation);
            }else {
                mBrowserLastUrlTips.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onIncognito(boolean isIncognito) {
        if (mTabControl != null && mTabControl.getCurrentTab() != null) {
            mTabControl.setIncognitoShowing(isIncognito);
        }
        if (mBrowserMainPageController != null) {
            mBrowserMainPageController.onIncognito(isIncognito);
        }
        if (mToolbar != null) {
            mToolbar.setToolbarStyle(isIncognito, mTabControl.getCurrentTab().isNativePage());
        }
    }

    public void onPageLoadStarted(Tab tab) {
        if (mToolbar != null) {
            mToolbar.onPageLoadStarted(tab);
        }
    }

    public void onPageLoadFinished(Tab tab) {
        if (mToolbar != null) {
            mToolbar.onPageLoadFinished(tab);
        }
    }

    public void onPageLoadStopped(Tab tab) {
        if (mToolbar != null) {
            mToolbar.onPageLoadStopped(tab);
        }
    }

    public boolean getIsSearchResultPage() {
        return mToolbar != null && mToolbar.getIsSearchResultPage();
    }


    @Override
    public void updateDefaultEngineIcon() {
        if (mBrowserMainPageController != null) {

            mBrowserMainPageController.updateDefaultSearchEngine();
        }
    }

    @Override
    public void onDestroy() {
        SearchEngines.getInstance(mActivity).unregisterIconUpdateListener(this);
    }

    @Override
    public void onToolbarStateChanged() {
        if (mKeyboardHeight == 0) {
            changeWebViewHeight();
        }
    }

    public ToolBar getToolbar() {
        return mToolbar;
    }

    public interface SelectSearchEngineListener {
        void updateSearchEngine();
    }

    public interface SelectWebSiteListener {
        void selectWebSite(WebSiteData webSiteData);
    }
}

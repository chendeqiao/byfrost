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

package com.intelligence.browser.webview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.FileChooserParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.intelligence.browser.controller.BackgroundHandler;
import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.MainActivity;
import com.intelligence.browser.settings.PreferenceKeys;
import com.intelligence.browser.R;
import com.intelligence.browser.controller.UploadHandler;
import com.intelligence.browser.controller.UrlHandler;
import com.intelligence.browser.manager.CrashRecoveryHandler;
import com.intelligence.browser.manager.IntentHandler;
import com.intelligence.browser.manager.IntentHandler.UrlData;
import com.intelligence.browser.base.UI;
import com.intelligence.browser.base.UI.ComboHomeViews;
import com.intelligence.browser.base.UI.ComboViews;
import com.intelligence.browser.base.ActivityController;
import com.intelligence.browser.base.FullscreenListener;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.base.WebViewController;
import com.intelligence.browser.base.WebViewFactory;
import com.intelligence.browser.historybookmark.BrowserAddBookmarkPage;
import com.intelligence.browser.historybookmark.Bookmarks;
import com.intelligence.browser.historybookmark.BrowserBookmarksPage;
import com.intelligence.browser.database.BrowserHelper;
import com.intelligence.browser.downloads.DownloadHandler;
import com.intelligence.browser.manager.BrowserWebViewFactory;
import com.intelligence.browser.manager.NetworkStateHandler;
import com.intelligence.browser.manager.PageDialogsHandler;
import com.intelligence.browser.manager.WallpaperHandler;
import com.intelligence.browser.ui.WebViewTimersControl;
import com.intelligence.browser.ui.activity.BrowserComboActivity;
import com.intelligence.browser.ui.media.VideoActivity;
import com.intelligence.browser.ui.webview.ImageJavascriptInterface;
import com.intelligence.browser.ui.webview.JsInterfaceInject;
import com.intelligence.browser.ui.home.OnSearchUrl;
import com.intelligence.browser.ui.media.PhotoViewPagerActivity;
import com.intelligence.browser.settings.BrowserSettingActivity;
import com.intelligence.browser.settings.BrowserClearDataPreferencesFragment;
import com.intelligence.browser.settings.SettingsPreferenecesFragment;
import com.intelligence.browser.database.provider.BrowserContract;
import com.intelligence.browser.database.provider.BrowserContract.Images;
import com.intelligence.browser.database.provider.BrowserProvider2.Thumbnails;
import com.intelligence.browser.database.provider.SnapshotProvider;
import com.intelligence.browser.ui.setting.BottomSettingplaneDialogFragment;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.browser.utils.ClickUtil;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.utils.DefaultBrowserSetUtils;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.utils.ThumbnailUtil;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.ContextMenuDialog;
import com.intelligence.browser.view.BrowserFindPopDialog;
import com.intelligence.browser.view.SharePageDialog;
import com.intelligence.browser.view.ToastPopView;
import com.intelligence.browser.ui.widget.BrowserMultiselectDialog;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.commonlib.tools.BuildUtil;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.commonlib.tools.UrlUtils;
import com.intelligence.commonlib.tools.WebAddress;
import com.intelligence.commonlib.download.util.FileUtils;
import com.intelligence.commonlib.download.util.StringUtils;
import com.intelligence.componentlib.dialog.BaseDialogFragment;
import com.intelligence.news.websites.bean.WebSiteData;
import com.intelligence.qr.BrowserCaptureActivity;
import com.intelligence.qr.BrowserIntentIntegrator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Controller
        implements WebViewController, UiController, ActivityController {

    private static final String LOGTAG = "Controller";
    private static final String SEND_APP_ID_EXTRA =
            "android.speech.extras.SEND_APPLICATION_ID_EXTRA";
    private static final String INCOGNITO_URI = "browser:incognito";
    public static final String QR_CODE = "QrCode";

    private static final String VOICE_PACKGE = "com.google.android.googlequicksearchbox";
    private static final String GOOGLE_PLAY_DETAILS_LINK = "http://play.google.com/store/apps/details?id=";
    public static final String URL_SEARCH_RESULT_URL = "url";
    public static final String URL_SEARCH_RESULT_IS_INPUT = "isInputUrl";
    public static final String URL_SEARCH_RESULT_INPUT_WORD = "inputUrl";
    public static final int USER_AGENT_COMPUTER = 3;
    public static final int USER_AGENT_DEFAULT = 0;

    // public message ids
    public final static int LOAD_URL = 1001;
    public final static int STOP_LOAD = 1002;

    // Message Ids
    private static final int FOCUS_NODE_HREF = 102;
    private static final int RELEASE_WAKELOCK = 107;

    static final int UPDATE_BOOKMARK_THUMBNAIL = 108;

    private static final int OPEN_BOOKMARKS = 201;

    private static final int EMPTY_MENU = -1;
    public static final int HOME_PAGE_MENU = -2;

    // activity requestCode
    public final static int COMBO_VIEW = 1;
    public final static int PREFERENCES_PAGE = 3;
    public  final static int FILE_SELECTED = 4;
    public final static int VOICE_RESULT = 6;
    public final static int QRCODE_RESULT = 8;
    public final static int URL_SEARCH = 7;
    public static final String STATE = "state";

    private final static int WAKELOCK_TIMEOUT = 5 * 60 * 1000; // 5 minutes

    private final static int TIMES = 2 * 1000;//Click the back button twice the time interval

    // A bitmap that is re-used in createScreenshot as scratch space
    private static Bitmap sThumbnailBitmap;

    private FragmentActivity mActivity;
    private UI mUi;
    private TabControl mTabControl;
    private BrowserSettings mSettings;
    private WebViewFactory mFactory;

    private WakeLock mWakeLock;

    private UrlHandler mUrlHandler;
    private UploadHandler mUploadHandler;
    private IntentHandler mIntentHandler;
    private PageDialogsHandler mPageDialogsHandler;
    private NetworkStateHandler mNetworkHandler;
    private boolean mShouldShowErrorConsole;

    private SystemAllowGeolocationOrigins mSystemAllowGeolocationOrigins;

    private boolean mMenuIsDown;

    // For select and find, we keep track of the ActionMode so that
    // finish() can be called as desired.
    private ActionMode mActionMode;

    /**
     * Only meaningful when mOptionsMenuOpen is true.  This variable keeps track
     * of whether the configuration has changed.  The first onMenuOpened call
     * after a configuration change is simply a reopening of the same menu
     * (i.e. mIconView did not change).
     */
    private boolean mConfigChanged;

    /**
     * Keeps track of whether the options menu is open. This is important in
     * determining whether to show or hide the title bar overlay
     */
    private boolean mOptionsMenuOpen;

    /**
     * Whether or not the options menu is in its bigger, popup menu form. When
     * true, we want the title bar overlay to be gone. When false, we do not.
     * Only meaningful if mOptionsMenuOpen is true.
     */
    private boolean mExtendedMenuOpen;

    private boolean mActivityPaused = true;
    private boolean mLoadStopped;

    private Handler mHandler;
    // Checks to see when the bookmarks database has changed, and updates the
    // Tabs' notion of whether they represent bookmarked sites.
    private ContentObserver mBookmarksObserver;
    private CrashRecoveryHandler mCrashRecoveryHandler;

    private boolean mBlockEvents;

    private String mVoiceResult;

    //Used to refresh button changes
    private boolean mWasInPageLoad = false;

    private BottomSettingplaneDialogFragment mBottomSetting;

    private BrowserMultiselectDialog mExitDialog;

    private ArrayList<FullscreenListener> mFullscreenUiList;
    private BrowserMainPageController mBrowserMainPageController;

    private long mOneTime = 0; //Time record for the first time, click the back button
    private Tab mParentTab;

    private ToastPopView mToastPopView;

    public Controller(FragmentActivity browser) {
        mActivity = browser;
        mSettings = BrowserSettings.getInstance();
        mTabControl = new TabControl(this);
        mSettings.setController(this);
        mCrashRecoveryHandler = CrashRecoveryHandler.initialize(this);
        mCrashRecoveryHandler.preloadCrashState();
        mFactory = new BrowserWebViewFactory(browser);

        mUrlHandler = new UrlHandler(this);
        mIntentHandler = new IntentHandler(mActivity, this);
        mPageDialogsHandler = new PageDialogsHandler(mActivity, this);

        startHandler();
        mBookmarksObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                int size = mTabControl.getTabCount();
                for (int i = 0; i < size; i++) {
                    mTabControl.getTab(i).updateBookmarkedStatus();
                }
            }
        };
        browser.getContentResolver().registerContentObserver(
                BrowserContract.Bookmarks.CONTENT_URI, true, mBookmarksObserver);

        mNetworkHandler = new NetworkStateHandler(mActivity, this);
        // Start watching the default geolocation permissions
        mSystemAllowGeolocationOrigins =
                new SystemAllowGeolocationOrigins(mActivity.getApplicationContext());
        mSystemAllowGeolocationOrigins.start();
        mBrowserMainPageController = new BrowserMainPageController(this, mActivity);
    }

    @Override
    public BrowserMainPageController getViewPageController() {
        return mBrowserMainPageController;
    }

    @Override
    public void start(final Intent intent) {
        // mCrashRecoverHandler has any previously saved state.
        mCrashRecoveryHandler.startRecovery(intent);
    }

    public void doStart(final Bundle icicle, final Intent intent) {
        // Unless the last browser usage was within 24 hours, destroy any
        // remaining incognito tabs.
        if(isOutSiteOpenApp(getActivity(),intent)){
            return;
        }
        Calendar lastActiveDate = icicle != null ?
                (Calendar) icicle.getSerializable("lastActiveDate") : null;
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        List<Tab> tabs = mTabControl.getTabs();
        final boolean restoreIncognitoTabs = false; //Don't restore Incognito Tab
//        !(lastActiveDate == null
//                || lastActiveDate.before(yesterday)
//                || lastActiveDate.after(today));

        // Find out if we will restore any state and remember the tab.
        final long currentTabId =
                mTabControl.canRestoreState(icicle, restoreIncognitoTabs);
        BrowserSettings.getInstance().setRestoreTabsOnStartup(false);
        if (currentTabId == -1) {
            // Not able to restore so we go ahead and clear session cookies.  We
            // must do this before trying to login the user as we don't want to
            // clear any session cookies set during login.
            CookieManager.getInstance().removeSessionCookie();
            BackgroundHandler.execute(new PruneThumbnails(mActivity, null));

            if (intent == null) {
                // This won't happen under common scenarios. The icicle is
                // not null, but there aren't any tabs to restore.
                // 如没有任何Intent调用的浏览器打开网页，则直接显示主页，不显示默认网页
                if (tabs.size() == 0) {
                    openTabToHomePage();
                }
                return;
            } else {
                final Bundle extra = intent.getExtras();
                // Create an initial tab.
                // If the intent is ACTION_VIEW and data is not null, the Browser is
                // invoked to view the content by another application. In this case,
                // the tab will be close when exit.
                UrlData urlData = IntentHandler.getUrlDataFromIntent(intent);
                Tab t = null;
                if (urlData.isEmpty()) {
                    final String action = intent.getAction();

                    if (IntentHandler.ACTION_OPEN_NEW_TAB.equals(action)) {
                        openTabToHomePage();
                        return;
                    } else if (IntentHandler.ACTION_OPEN_NEW_INCOGNITO_TAB.equals(action)) {
                        openIncognitoTab();
                        return;
                    }
                    if (intent.getBooleanExtra(IntentHandler.ACTION_OPEN_URL_SEARCH, false)) {
                        mUi.openSearchInputView("");
                        return;
                    }
                    if (intent.getBooleanExtra(IntentHandler.ACTION_OPEN_URL_HOTWORD, false)) {
                        SchemeUtil.jumpHotWorsPage(mActivity, 3);
                        return;
                    }
                    if (intent.getBooleanExtra(IntentHandler.ACTION_OPEN_URL_NAVIGATION, false)) {
                        SchemeUtil.jumpToScheme(getContext(), SchemeUtil.BROWSER_SCHEME_PATH_NEWS);
                        return;
                    }
                    if (intent.getBooleanExtra(IntentHandler.ACTION_OPEN_ADVANCED_PREFERENCES, false)) {
                        BrowserSettingActivity.startPreferenceFragmentForResult(mActivity,
                                SettingsPreferenecesFragment.class.getName(), 100);
                        return;
                    }
                    if (Intent.ACTION_SEARCH.equals(action)
                            || MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)
                            || Intent.ACTION_WEB_SEARCH.equals(action)) {
                        String url = com.intelligence.browser.utils.UrlUtils.filterBySearchEngine(mActivity, intent.getStringExtra(SearchManager
                                .QUERY));
                        urlData = new UrlData(url, null, intent, null, null);
                    }
                }
                if (urlData.isEmpty()) {
                    if (tabs.size() == 0) {
                        openTabToHomePage();
                    }
                    return;
                } else {
                    t = openTab(urlData);
                }
                if (t != null) {
                    t.setAppId(intent.getStringExtra(BrowserHelper.EXTRA_APPLICATION_ID));
                    final PreBrowserWebView webView = t.getWebView();
                    if (extra != null && webView != null) {
                        final int scale = extra.getInt(BrowserHelper.INITIAL_ZOOM_LEVEL, 0);
                        if (scale > 0 && scale <= 1000) {
                            webView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                                @Override
                                public void call(BaseWebView baseWebView) {
                                    baseWebView.setInitialScale(scale);
                                }
                            });

                        }
                    }
                }
            }
            mUi.updateTabs(mTabControl.getTabs());
        } else {
            if (tabs.size() == 0) {
                openTabToHomePage();
            }
            showRecoveryTabsToast(R.string.abnormal_recover_message, icicle, currentTabId, restoreIncognitoTabs);
            mUi.updateTabs(tabs);
            // TabControl.restoreState() will create a new tab even if

            // restoring the state fails.
            setActiveTab(mTabControl.getCurrentTab());
            if (getCurrentTab() != null && !getCurrentTab().isNativePage()) {
                ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_WEBVIEW, mTabControl.getCurrentPosition(), true);
            }
            // Intent is non-null when framework thinks the browser should be
            // launching with a new intent (icicle is null).
            if (intent != null) {
                mIntentHandler.onNewIntent(intent);
            }
        }
        // Read JavaScript flags if it exists.
        String jsFlags = getSettings().getJsEngineFlags();
        if (intent != null
                && MainActivity.ACTION_SHOW_BOOKMARKS.equals(intent.getAction())) {
            bookmarksOrHistoryPicker(ComboViews.Bookmarks);
        }
    }

    private static class PruneThumbnails implements Runnable {
        private Context mContext;
        private List<Long> mIds;

        PruneThumbnails(Context context, List<Long> preserveIds) {
            mContext = context.getApplicationContext();
            mIds = preserveIds;
        }

        @Override
        public void run() {
            ContentResolver cr = mContext.getContentResolver();
            try {
                if (mIds == null || mIds.size() == 0) {
                    deleteThumbnailFiles(cr, null);
                    cr.delete(Thumbnails.CONTENT_URI, null, null);
                } else {
                    int length = mIds.size();
                    StringBuilder where = new StringBuilder();
                    where.append(Thumbnails._ID);
                    where.append(" not in (");
                    for (int i = 0; i < length; i++) {
                        where.append(mIds.get(i));
                        if (i < (length - 1)) {
                            where.append(",");
                        }
                    }
                    where.append(")");
                    deleteThumbnailFiles(cr, where.toString());
                    cr.delete(Thumbnails.CONTENT_URI, where.toString(), null);
                }
            } catch (Exception e) {
                //this is not need handle
            }
        }

        private void deleteThumbnailFiles(ContentResolver cr, String where) {
            Cursor c = null;
            String filename;
            try {
                c = cr.query(Thumbnails.CONTENT_URI, new String[]{Thumbnails.THUMBNAIL}, where, null, null);
                while (c != null && c.moveToNext()) {
                    filename = c.getString(0);
                    if (TextUtils.isEmpty(filename)) {
                        continue;
                    }
                    File f = new File(filename);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            } catch (Exception e) {
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    @Override
    public WebViewFactory getWebViewFactory() {
        return mFactory;
    }

    @Override
    public void onSetWebView(Tab tab, PreBrowserWebView view) {
        mUi.onSetWebView(tab, view);
    }

    @Override
    public void createSubWindow(Tab tab) {
        endActionMode();
        PreBrowserWebView subView = mFactory.createWebView(BrowserSettings.getInstance().noFooterBrowser());
        mUi.createSubWindow(tab, subView);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public Activity getActivity() {
        return mActivity;
    }

    public void setUi(UI ui) {
        mUi = ui;
        mBrowserMainPageController.setUi(mUi);
    }

    public void openUrl(String url) {
        if (mUi instanceof OnSearchUrl) {
            ((OnSearchUrl) mUi).onSelect(url, false);
        }
    }

    @Override
    public BrowserSettings getSettings() {
        return mSettings;
    }

    IntentHandler getIntentHandler() {
        return mIntentHandler;
    }

    @Override
    public UI getUi() {
        return mUi;
    }

    public int getMaxTabs() {
        return mActivity.getResources().getInteger(R.integer.max_tabs);
    }

    @Override
    public TabControl getTabControl() {
        return mTabControl;
    }

    @Override
    public List<Tab> getTabs() {
        return mTabControl.getTabs();
    }

    private static class InnerHandler extends Handler {
        private WeakReference<Controller> mControllerHolder;

        public InnerHandler(Controller controller) {
            this.mControllerHolder = new WeakReference<>(controller);
        }

        @Override
        public void handleMessage(Message msg) {
            Controller controller = mControllerHolder.get();
            if (controller == null) {
                return;
            }
            switch (msg.what) {
                case OPEN_BOOKMARKS:
                    controller.bookmarksOrHistoryPicker(ComboViews.Bookmarks);
                    break;
                case FOCUS_NODE_HREF: {
                    String url = (String) msg.getData().get("url");
                    String title = (String) msg.getData().get("title");
                    String src = (String) msg.getData().get("src");
                    if (TextUtils.isEmpty(url)) url = src; // use image if no anchor
                    if (TextUtils.isEmpty(url)) {
                        break;
                    }
                    Map focusNodeMap = (Map) msg.obj;
                    WebView view = (WebView) focusNodeMap.get("webview");
                    // Only apply the action if the top window did not change.
                    if (controller.getCurrentTopBrowserWebView() != view) {
                        break;
                    }
                    switch (msg.arg1) {
                        case R.id.open_context_menu_id:
                            controller.loadUrlFromContext(url);
                            break;
                        case R.id.view_image_context_menu_id:
                            controller.loadUrlFromContext(src);
                            break;
                        case R.id.copy_link_context_menu_id:
                            controller.copy(url);
                            ToastUtil.showShortToast(controller.getActivity(), R.string.copylink_success);
                            break;
                        case R.id.save_link_context_menu_id:
                            DownloadHandler.onDownloadStart(controller.getActivity(), url, null, null, null, url);
                            break;
                        case R.id.open_newtab_context_menu_id:
                            controller.openTab(url, controller.getCurrentTab(), true, true);
                            break;
                        case R.id.open_newtab_background_context_menu_id:
                            controller.openTab(url, controller.getCurrentTab(), false, true);
                            break;
                        case R.id.image_ad_mark:
                    }
                    break;
                }

                case LOAD_URL:
                    controller.loadUrlFromContext((String) msg.obj);
                    break;

                case STOP_LOAD:
                    controller.stopLoading();
                    break;

                case RELEASE_WAKELOCK:
                    if (controller.mWakeLock != null && controller.mWakeLock.isHeld()) {
                        controller.mWakeLock.release();
                        // if we reach here, Browser should be still in the
                        // background loading after WAKELOCK_TIMEOUT (5-min).
                        // To avoid burning the battery, stop loading.
                        controller.mTabControl.stopAllLoading();
                    }
                    break;

                case UPDATE_BOOKMARK_THUMBNAIL:
                    Tab tab = (Tab) msg.obj;
                    if (tab != null) {
                        controller.updateScreenshot(tab);
                    }
                    break;
            }
        }

    }

    private void startHandler() {
        mHandler = new InnerHandler(this);

    }

    @Override
    public Tab getCurrentTab() {
        return mTabControl.getCurrentTab();
    }

    @Override
    public void shareCurrentPage() {
        shareCurrentPage(mTabControl.getCurrentTab());
    }

    private void shareCurrentPage(Tab tab) {
        if (tab != null) {
            SharePageDialog dialog = new SharePageDialog(mActivity);
            dialog.showShareDialog(mActivity.findViewById(R.id.bottom_bar).findViewById(R.id.tool_bar),
                    tab.getTitle(), tab.getUrl(), ThumbnailUtil.getInstance(mActivity).convertViewToDrawable(tab
                            .getWebView().getBaseWebView(), 0.3f, 0.3f));
        }
    }

    /**
     * Share a page, providing the title, url, favicon, and a screenshot.  Uses
     * an {@link Intent} to launch the Activity chooser.
     *
     * @param c          Context used to launch a new Activity.
     * @param title      Title of the page.  Stored in the Intent with
     *                   {@link Intent#EXTRA_SUBJECT}
     * @param url        URL of the page.  Stored in the Intent with
     *                   {@link Intent#EXTRA_TEXT}
     * @param favicon    Bitmap of the favicon for the page.  Stored in the Intent
     *                   with
     * @param screenshot Bitmap of a screenshot of the page.  Stored in the
     *                   Intent with
     */
    public static void sharePage(Context c, String title, String url,
                          Bitmap favicon, Bitmap screenshot) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        if (TextUtils.isEmpty(title)) {
            send.putExtra(Intent.EXTRA_TEXT, url);
        } else {
            send.putExtra(Intent.EXTRA_TEXT, title + "\n" + url);
        }
        try {
            c.startActivity(Intent.createChooser(send, c.getString(
                    R.string.choosertitle_sharevia)));
        } catch (android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }

    private void copy(CharSequence text) {
        ClipboardManager cm = (ClipboardManager) mActivity
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    // lifecycle
    @Override
    public void onConfigurationChanged(Configuration config) {
        if (mUi != null) {
            mUi.onConfigurationChanged(config);
        }
        if(mActivity != null) {
            mActivity.invalidateOptionsMenu();
        }
    }


    private boolean isOutSiteOpenApp(Context context,Intent intent) {
        try {
            if(intent == null){
                return false;
            }
            String action = intent.getAction();
            Uri data = intent.getData();
            String type = intent.getType();

            if (Intent.ACTION_VIEW.equals(action) && data != null && type != null && (type.startsWith("video/") || type.startsWith("audio/") || type.startsWith("image/"))) {
                if (type.startsWith("video/") || type.startsWith("audio/")) {
                    Intent videoIntent = new Intent(context, VideoActivity.class);
                    videoIntent.setDataAndType(data, type);
                    videoIntent.setAction(action);
                    context.startActivity(videoIntent);
                } else if (type.startsWith("image/")) {
                    Intent videoIntent = new Intent(context, PhotoViewPagerActivity.class);
                    videoIntent.setDataAndType(data, type);
                    videoIntent.setAction(action);
                    context.startActivity(videoIntent);
                }
                return true;
            }
        }catch (Exception e){
        }
        return false;
    }

    @Override
    public void handleNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if(isOutSiteOpenApp(getActivity(),intent)){
            return;
        }
        if (intent.getData() != null) {
            String url = intent.getData().getQueryParameter("page");
            boolean isJumpSetting = !TextUtils.isEmpty(url) && url.equals(SchemeUtil.BROWSER_SOURCE_KEY_SETTING + "");
            if (isJumpSetting) {
                openPreferences();
                return;
            }
        }
        if (intent.getBooleanExtra(DefaultBrowserSetUtils.KEY_DEFAULT_BROWSER_SETTING, false)) {
            //if user open this web to set default browser, just open settings page. modify by Simon Liu.
            Activity activity = getActivity();
            if (DefaultBrowserSetUtils.isThisBrowserSetAsDefault(activity)) {
                ToastUtil.showShortToast(activity, R.string.set_default_success);
            } else {
                ToastUtil.showShortToast(activity, R.string.set_default_failure);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openPreferencesToSetDefaultBrowser();
                    }
                }, 800);//open preferences page after 800 ms.
            }
            return;
        }
        if (!mUi.isWebShowing()) {
            Tab t = getCurrentTab();
            if (t != null && t.isNativePage()) {
                loadNativePage(t);
            } else {
                mUi.showWeb(false);
            }
        }
        mIntentHandler.onNewIntent(intent);
    }

    @Override
    public void onPause() {
        if(mUi != null){
            mUi.onPause();
        }
        if (mExitDialog != null && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        }

        if (mActivityPaused) {
            return;
        }
        mActivityPaused = true;
        mNetworkHandler.onPause();

        //WebView.disablePlatformNotifications();
        if (sThumbnailBitmap != null) {
            sThumbnailBitmap.recycle();
            sThumbnailBitmap = null;
        }
        onCloseMenu(false);
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onStop() {
//        hideCustomView();
        Tab tab = mTabControl.getCurrentTab();
        if (tab != null) {
            tab.stop();
            if (!pauseWebViewTimers(tab)) {
                if (mWakeLock == null) {
                    PowerManager pm = (PowerManager) mActivity
                            .getSystemService(Context.POWER_SERVICE);
                    mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Browser");
                }
                mWakeLock.acquire();
                mHandler.sendMessageDelayed(mHandler
                        .obtainMessage(RELEASE_WAKELOCK), WAKELOCK_TIMEOUT);
            }
        }
        mUi.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save all the tabs
        Bundle saveState = createSaveState();

        // crash recovery manages all save & restore state
        mCrashRecoveryHandler.writeState(saveState);
    }

    /**
     * Save the current state to outState. Does not write the state to
     * disk.
     *
     * @return Bundle containing the current state of all tabs.
     */
    /* package */ public Bundle createSaveState() {
        Bundle saveState = new Bundle();
        mTabControl.saveState(saveState);
        if (!saveState.isEmpty()) {
            // Save time so that we know how old incognito tabs (if any) are.
            saveState.putSerializable("lastActiveDate", Calendar.getInstance());
        }
        return saveState;
    }

    @Override
    public void onResume() {
        if (!mActivityPaused) {
            return;
        }
        mActivityPaused = false;
        Tab current = mTabControl.getCurrentTab();
        if (current != null) {
            current.resume();
            resumeWebViewTimers(current);
        }
        releaseWakeLock();

        mUi.onResume();
        mNetworkHandler.onResume();
        //WebView.enablePlatformNotifications();
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mHandler.removeMessages(RELEASE_WAKELOCK);
            mWakeLock.release();
        }
    }

    /**
     * resume all WebView timers using the WebView instance of the given tab
     *
     * @param tab guaranteed non-null
     */
    private void resumeWebViewTimers(Tab tab) {
        boolean inLoad = tab.inPageLoad();
        PreBrowserWebView w = tab.getWebView();
        if ((!mActivityPaused && !inLoad) || (mActivityPaused && inLoad)) {
            CookieSyncManager.getInstance().startSync();
            if (w != null) {
                w.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                    @Override
                    public void call(BaseWebView baseWebView) {
                        WebViewTimersControl.getInstance().onBrowserActivityResume(baseWebView);
                    }
                });
            }
        }
    }

    /**
     * Pause all WebView timers using the WebView of the given tab
     *
     * @return true if the timers are paused or tab is null
     */
    private boolean pauseWebViewTimers(Tab tab) {
        if (tab == null) {
            return true;
        } else if (!tab.inPageLoad()) {
            CookieSyncManager.getInstance().stopSync();
            PreBrowserWebView currentWebView = getCurrentWebView();
            if (currentWebView != null && currentWebView.isWebViewCreate()) {
                WebViewTimersControl.getInstance().onBrowserActivityPause(currentWebView.getBaseWebView());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        if (mUploadHandler != null && !mUploadHandler.handled()) {
            mUploadHandler.onResult(Activity.RESULT_CANCELED, null);
            mUploadHandler = null;
        }
        if (mTabControl == null) return;
        mUi.onDestroy();
        // Remove the current tab and sub window
        Tab t = mTabControl.getCurrentTab();
        if (t != null) {
            dismissSubWindow(t);
            removeTab(t);
        }
        mActivity.getContentResolver().unregisterContentObserver(mBookmarksObserver);
        // Destroy all the tabs
        mTabControl.destroy();
        // Stop watching the default geolocation permissions
        mSystemAllowGeolocationOrigins.stop();
        mSystemAllowGeolocationOrigins = null;
    }

    protected boolean isActivityPaused() {
        return mActivityPaused;
    }

    @Override
    public void onLowMemory() {
        mTabControl.freeMemory();
    }

    @Override
    public boolean shouldShowErrorConsole() {
        return mShouldShowErrorConsole;
    }

    public  void setShouldShowErrorConsole(boolean show) {
        if (show == mShouldShowErrorConsole) {
            // Nothing to do.
            return;
        }
        mShouldShowErrorConsole = show;
        Tab t = mTabControl.getCurrentTab();
        if (t == null) {
            // There is no current tab so we cannot toggle the error console
            return;
        }
        mUi.setShouldShowErrorConsole(t, show);
    }

    @Override
    public void stopLoading() {
        mLoadStopped = true;
        Tab tab = mTabControl.getCurrentTab();
        PreBrowserWebView w = getCurrentTopWebView();
        if (w == null) return;
        BaseWebView webView = w.getBaseWebView();
        if (webView != null) {
            webView.stopLoading();
            if (TextUtils.isEmpty(webView.getTitle()) && webView.canGoBack()) {
                webView.goBack();
            }
            mUi.onPageStopped(tab);
        }
    }

    boolean didUserStopLoading() {
        return mLoadStopped;
    }

    public boolean startActivityForUrl(String url) {
        return mUrlHandler.startActivityForUrl(getCurrentTab(), url);
    }

    // WebViewController
    @Override
    public void onPageStarted(Tab tab, WebView view, Bitmap favicon) {
        String url = tab.getUrl();
        mIscleanAd = false;
        if (mUrlHandler.startActivityForUrl(tab, url)) {
            return;
        }
//        view.getSettings().setJavaScriptEnabled(false);
        // We've started to load a new page. If there was a pending message
        // to save a screenshot then we will now take the new page and save
        // an incorrect screenshot. Therefore, remove any pending thumbnail
        // messages from the queue.
        mHandler.removeMessages(Controller.UPDATE_BOOKMARK_THUMBNAIL,
                tab);

        // reset sync timer to avoid sync starts during loading a page
        CookieSyncManager.getInstance().resetSync();

        if (!mNetworkHandler.isNetworkUp()) {
            view.setNetworkAvailable(false);
        }

        // when BrowserActivity just starts, onPageStarted may be called before
        // onResume as it is triggered from onCreate. Call resumeWebViewTimers
        // to start the timer. As we won't switch tabs while an activity is in
        // stop state, we can ensure calling resume and stop in pair.
        if (mActivityPaused) {
            resumeWebViewTimers(tab);
        }
        mLoadStopped = false;
        endActionMode();

        mUi.onTabDataChanged(tab);


        ImageJavascriptInterface.initAdLists();
        maybeUpdateFavicon(tab, null, url, favicon);

    }

    public void onCloseMenu(boolean isDoAnimation){
        onCloseMenu(isDoAnimation,0);
    }

    @Override
    public void onCloseMenu(boolean isDoAnimation,int delayTime) {
        dettachFragment(mBottomSetting,delayTime);
    }

    private int mWebViewOpenCount;

    @Override
    public void onPageFinished(Tab tab) {
        mCrashRecoveryHandler.backupState();
        mUi.onTabDataChanged(tab);

        // Performance probe
        //if (false) {
//            Performance.onPageFinished(tab.getUrl());
        //}

        //Performance.tracePageFinished();
//        DeviceInfoUtils.verifyStoragePermissions(getActivity());
        if (mWebViewOpenCount < 100) {
            mWebViewOpenCount = SharedPreferencesUtils.getWebviewOpenCount();
            SharedPreferencesUtils.put(SharedPreferencesUtils.BROWSER_WEBVIEW_FINISH_COUNT, ++mWebViewOpenCount);
        }
    }

    private boolean mIscleanAd;
    @Override
    public void onProgressChanged(Tab tab) {
        int newProgress = tab.getLoadProgress();

        if(newProgress > 90 && !mIscleanAd){
            clearAd();
            mIscleanAd = true;
        }
        if (newProgress == 100) {
            CookieSyncManager.getInstance().sync();
            // onProgressChanged() may continue to be called after the main
            // frame has finished loading, as any remaining sub frames continue
            // to load. We'll only get called once though with newProgress as
            // 100 when everything is loaded. (onPageFinished is called once
            // when the main frame completes loading regardless of the state of
            // any sub frames so calls to onProgressChanges may continue after
            // onPageFinished has executed)
            if (tab.inPageLoad()) {
                mWasInPageLoad = true;
            } else if (mWasInPageLoad) {
                mWasInPageLoad = false;
            }
            if (mActivityPaused && pauseWebViewTimers(tab)) {
                // stop the WebView timer and release the wake lock if it is
                // finished while BrowserActivity is in stop state.
                releaseWakeLock();
            }
        } else {
            // onPageFinished may have already been called but a subframe is
            // still loading
            // updating the progress and
            // update the menu items.
            mWasInPageLoad = tab.inPageLoad();
        }
        mUi.onProgressChanged(tab);
    }

    @Override
    public void onUpdatedSecurityState(Tab tab) {
        mUi.onTabDataChanged(tab);
    }

    @Override
    public void onReceivedTitle(Tab tab, final String title) {
        mUi.onTabDataChanged(tab);
        final String pageUrl = tab.getOriginalUrl();
        if (TextUtils.isEmpty(pageUrl) || pageUrl.length()
                >= SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH) {
            return;
        }
        // Update the title in the history database if not in private browsing mode
        if (!tab.isPrivateBrowsingEnabled()) {
            BrowserDataController.getInstance(mActivity).updateHistoryTitle(pageUrl, title);
        }
    }

    @Override
    public void onFavicon(Tab tab, WebView view, Bitmap icon) {
        mUi.onTabDataChanged(tab);
        maybeUpdateFavicon(tab, view.getOriginalUrl(), view.getUrl(), icon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(Tab tab, WebView view, String url) {
        return mUrlHandler.shouldOverrideUrlLoading(tab, view, url);
    }

    @Override
    public boolean shouldOverrideKeyEvent(KeyEvent event) {
        if (mMenuIsDown) {
            // only check shortcut key when MENU is held
            return mActivity.getWindow().isShortcutKey(event.getKeyCode(),
                    event);
        } else {
            return false;
        }
    }

    @Override
    public boolean onUnhandledKeyEvent(KeyEvent event) {
        if (!isActivityPaused()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                return mActivity.onKeyDown(event.getKeyCode(), event);
            } else {
                return mActivity.onKeyUp(event.getKeyCode(), event);
            }
        }
        return false;
    }

    @Override
    public void doUpdateVisitedHistory(Tab tab, boolean isReload) {
        // Don't save anything in private browsing mode
        if (tab.isPrivateBrowsingEnabled()) return;
        if (!tab.isNativePage()) {
            String url = tab.getOriginalUrl();
            if (TextUtils.isEmpty(url)
                    || url.regionMatches(true, 0, "about:", 0, 6)) {
                return;
            }
            BrowserDataController.getInstance(mActivity).updateVisitedHistory(url);
        } else {
            //Custom tab, the main page always save
        }
        mCrashRecoveryHandler.backupState();
    }

    @Override
    public void getVisitedHistory(final ValueCallback<String[]> callback) {
        AsyncTask<Void, Void, String[]> task =
                new AsyncTask<Void, Void, String[]>() {
                    @Override
                    public String[] doInBackground(Void... unused) {
                        return BrowserHelper.getVisitedHistory(mActivity.getContentResolver());
                    }

                    @Override
                    public void onPostExecute(String[] result) {
                        callback.onReceiveValue(result);
                    }
                };
        task.execute();
    }

    @Override
    public void onReceivedHttpAuthRequest(Tab tab, WebView view,
                                          final HttpAuthHandler handler, final String host,
                                          final String realm) {
        String username = null;
        String password = null;

        boolean reuseHttpAuthUsernamePassword
                = handler.useHttpAuthUsernamePassword();

        if (reuseHttpAuthUsernamePassword && view != null) {
            String[] credentials = view.getHttpAuthUsernamePassword(host, realm);
            if (credentials != null && credentials.length == 2) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        if (username != null && password != null) {
            handler.proceed(username, password);
        } else {
            if (tab.inForeground()/* && !handler.suppressDialog()*/) {
                mPageDialogsHandler.showHttpAuthentication(tab, handler, host, realm);
            } else {
                handler.cancel();
            }
        }
    }

    @Override
    public void openSelectWebSiteView(View view, WebSiteData webSiteData) {
        if(mUi != null){
            mUi.openSelectWebSiteView(view,webSiteData);
        }
    }

    @Override
    public boolean isCanScroll(MotionEvent ev) {
        if(mBrowserMainPageController != null){
         return mBrowserMainPageController.isCanScroll(ev);
        }
        return false;
    }

    @Override
    public void onDownloadStart(String url, String useragent, String mimeType, long contentLength) {
        onDownloadStart(getCurrentTab(), url, useragent, "", mimeType, null, contentLength);
    }

    @Override
    public void onDownloadStart(Tab tab, String url, String userAgent,
                                String contentDisposition, String mimetype, String referer,
                                long contentLength) {
        PreBrowserWebView w = tab.getWebView();
        DownloadHandler.onDownloadStart(mActivity, url, userAgent,
                contentDisposition, mimetype, referer);
        if (w == null) {
            return;
        }
        BaseWebView webView = w.getBaseWebView();
        if (webView != null && webView.copyBackForwardList() != null && webView.copyBackForwardList().getSize() == 0) {
            // This Tab was opened for the sole purpose of downloading a
            // file. Remove it.
            if (tab == mTabControl.getCurrentTab()) {
                // In this case, the Tab is still on top.
                goBack();
            } else {
                // In this case, it is not.
                closeTab(tab);
            }
        }
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return mUi.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
        return mUi.getVideoLoadingProgressView();
    }

    @Override
    public void showSslCertificateOnError(WebView view, SslErrorHandler handler,
                                          SslError error) {
        mPageDialogsHandler.showSSLCertificateOnError(view, handler, error);
    }

    // helper method

    /*
     * Update the favorites icon if the private browsing isn't enabled and the
     * icon is valid.
     */
    private void maybeUpdateFavicon(Tab tab, final String originalUrl,
                                    final String url, Bitmap favicon) {
        if (favicon == null || favicon.isRecycled()) {
            return;
        }
        if (!tab.isPrivateBrowsingEnabled()) {
            Bookmarks.updateFavicon(mActivity
                    .getContentResolver(), originalUrl, url, favicon);
        }
    }

    @Override
    public void bookmarkedStatusHasChanged(Tab tab) {
        // TODO: Switch to using onTabDataChanged after b/3262950 is fixed
        mUi.bookmarkedStatusHasChanged(tab);
    }

    // end WebViewController

    protected void pageUp() {
        PreBrowserWebView currentTopWebView = getCurrentTopWebView();
        BaseWebView webView = currentTopWebView != null ? currentTopWebView.getBaseWebView() : null;
        if (webView != null) {
            webView.pageUp(false);
        }
    }

    protected void pageDown() {
        PreBrowserWebView currentTopWebView = getCurrentTopWebView();
        BaseWebView webView = currentTopWebView != null ? currentTopWebView.getBaseWebView() : null;
        if (webView != null) {
            webView.pageDown(false);
        }
    }

    @Override
    public void editUrl() {
        if (mOptionsMenuOpen) mActivity.closeOptionsMenu();
    }

    @Override
    public void showCustomView(Tab tab, View view, int requestedOrientation,
                               WebChromeClient.CustomViewCallback callback) {
        if (tab.inForeground()) {
            if (mUi.isCustomViewShowing()) {
                callback.onCustomViewHidden();
                return;
            }
            mUi.showCustomView(view, requestedOrientation, callback);
            mActivity.invalidateOptionsMenu();
        }
    }

    @Override
    public void hideCustomView() {
        if (mUi.isCustomViewShowing()) {
            mUi.onHideCustomView();
            mActivity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        switch (requestCode) {
            case PREFERENCES_PAGE:
                if (getCurrentTopWebView() == null) return;
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    String action = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (PreferenceKeys.PREF_PRIVACY_CLEAR_HISTORY.equals(action)) {
                        mTabControl.removeParentChildRelationShips();
                    } else if (PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES.equals(action)
                            && !TextUtils.isEmpty(getCurrentTab().getUrl())) {
                        BaseWebView webView = getCurrentTopWebView().getBaseWebView();
                        DisplayUtil.setScreenBrightness(mActivity, (Float) SharedPreferencesUtils.get(Constants
                                .SCREEN_BRIGHTNESS, 1f));
                        if (webView != null) webView.reload();
                    }
                }
                break;
            case FILE_SELECTED:
                // Chose a file from the file picker.
                if (null == mUploadHandler) break;
                mUploadHandler.onResult(resultCode, intent);
                break;
            case COMBO_VIEW:
                if (intent == null || resultCode != Activity.RESULT_OK) {
                    break;
                }
                mUi.showWeb(false);
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    Tab t = getCurrentTab();
                    Uri uri = intent.getData();
                    if (t != null && t.isNativePage()) {
                        mUi.showAndOpenUrl(uri.toString(), false);
                    } else {
                        loadUrl(t, uri.toString());
                    }

                } else if (intent.hasExtra(BrowserComboActivity.EXTRA_OPEN_ALL)) {
                    String[] urls = intent.getStringArrayExtra(
                            BrowserComboActivity.EXTRA_OPEN_ALL);
                    Tab parent = getCurrentTab();
                    for (String url : urls) {
                        //第三个参数true （修改之前是!mSettings.openInBackground(),mSettings.openInBackground()的默认值是false）
                        parent = openTab(url, parent, true, true);
                    }
                    mUi.showAndOpenUrl(null, true);
                } else if (intent.hasExtra(BrowserComboActivity.EXTRA_OPEN_SNAPSHOT)) {
                    if (mUi instanceof BrowserPhoneUi) {
                        ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_HIDE_NATIVE_PAGER, mTabControl
                                        .getCurrentPosition(),
                                false);
                    }
                    long id = intent.getLongExtra(
                            BrowserComboActivity.EXTRA_OPEN_SNAPSHOT, -1);
                    if (id >= 0) {
                        createNewSnapshotTab(id, true);
                    }
                }
                break;
            case VOICE_RESULT:
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    ArrayList<String> results = intent.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    if (results.size() >= 1) {
                        mVoiceResult = results.get(0);
                    }
                    if (mVoiceResult != null) {
                        mUi.onVoiceResult(mVoiceResult);
                        mVoiceResult = null;
                    }
                }
                break;
            case ActivityUtils.QRCODE_RESULT:
                if (intent != null && Activity.RESULT_OK == resultCode) {
                    mUi.onQrUrl(intent.getStringExtra(QR_CODE));
                }
                break;
            case URL_SEARCH:
                if (intent != null && Activity.RESULT_OK == resultCode) {
                    onSelect(intent.getStringExtra(URL_SEARCH_RESULT_URL), intent.getBooleanExtra
                            (URL_SEARCH_RESULT_IS_INPUT, false), intent.getStringExtra(URL_SEARCH_RESULT_INPUT_WORD));
                }
                break;
            default:
                break;
        }
        if (getCurrentTopWebView() != null)
            getCurrentTopWebView().requestFocus();
    }

    public void onSelect(String url, boolean isInputUrl, String inputWord) {
        ((BrowserPhoneUi) mUi).onSelect(url, isInputUrl, inputWord);
    }

    /**
     * Open the Go page.
     *
     * @param startView true, open starting on the history tab.
     *                  Otherwise, start with the bookmarks tab.
     */
    @Override
    public void bookmarksOrHistoryPicker(ComboViews startView) {
        // clear action mode
        if (isInCustomActionMode()) {
            endActionMode();
        }
        Bundle extras = new Bundle();
        // Disable opening in a new window if we have maxed out the windows
        extras.putBoolean(BrowserBookmarksPage.EXTRA_DISABLE_WINDOW,
                !mTabControl.canCreateNewTab());
        mUi.showComboView(startView, extras);
    }

    // combo view callbacks

    // key handling
    protected void onBackKey() {
        if (!mUi.onBackKey()) {
            PreBrowserWebView subWindow = mTabControl.getCurrentSubWindow();
            if (subWindow != null) {
                BaseWebView webView = subWindow.getBaseWebView();
                if (webView != null && webView.canGoBack()) {
                    goBack();
                } else {
                    dismissSubWindow(mTabControl.getCurrentTab());
                }
            } else {
                goBack();
            }
        }
    }

    public void goBack() {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            /*
             * Instead of finishing the activity, simply push this to the back
             * of the stack and let ActivityManager to choose the foreground
             * activity. As BrowserActivity is singleTask, it will be always the
             * root of the task. So we can use either true or false for
             * moveTaskToBack().
             */
            mActivity.moveTaskToBack(true);
            return;
        }
        if (current.isNativePage()) {
            againQuit();
        } else if (current.canGoBack()) {
            current.goBack();
        } else {
            // Check to see if we are closing a window that was created by
            // another window. If so, we switch back to that window.
            Tab parent = current.getParent();
            if (parent != null) {
                switchToTab(parent);
                // Now we close the other tab
                closeTab(current);
                if (parent.isNativePage()) {
                    loadNativePage(parent);
                }
            } else {
                if (mSettings.getHomePage().equals(Constants.NATIVE_PAGE_URL)) {
                    loadUrl(current, Constants.NATIVE_PAGE_URL);
                } else {
                    againQuit();
                }
            }
        }
    }

    public void goForward() {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            return;
        }
        if (current.isNativePage()) {
            current.setNativePage(false);
            if (current.getWebView() != null) {
                loadUrl(current, current.getUrl());
            }
        } else {
            current.goForward();
        }
    }

    protected boolean onMenuKey() {
        return mUi.onMenuKey();
    }

    // menu handling and state
    // TODO: maybe put into separate handler

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo, float touchX, float touchY) {
        if (!(v instanceof WebView)) {
            return;
        }
        final WebView webview = (WebView) v;
        WebView.HitTestResult result = webview.getHitTestResult();
        if (result == null) {
            return;
        }

        int type = result.getType();
        if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
            return;
        }
        if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
            // let TextView handles context menu
            return;
        }
        ContextMenuDialog dialog = new ContextMenuDialog(mActivity, this, result);
        dialog.show(type, touchX, touchY);

        //update the ui
        mUi.onContextMenuCreated(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }

    public void invalidateOptionsMenu() {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void updateMenuState(Tab tab, Menu menu) {
    }

    @Override
    public void updateToolBarItemState() {
        TextView tabPageNumber = mActivity.findViewById(R.id.page_number_tab_id);
        DisplayUtil.resetTabSwitcherTextSize(tabPageNumber, getTabControl().getTabCount());
    }

    @Override
    public void updateCommomMenuState(BottomSettingplaneDialogFragment commonMenu) {
//        if (commonMenu == null) {
//            return;
//        }
//        Tab current = mTabControl.getCurrentTab();
//        commonMenu.updateCommonMenuState(current);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        if (null == getCurrentTopWebView()) {
            return false;
        }*/
        if (mMenuIsDown) {
            // The shortcut action consumes the MENU. Even if it is still down,
            // it won't trigger the next shortcut action. In the case of the
            // shortcut action triggering a new activity, like Bookmarks, we
            // won't get onKeyUp for MENU. So it is important to reset it here.
            mMenuIsDown = false;
        }
        if (mUi.onOptionsItemSelected(item)) {
            // ui callback handled it
            return true;
        }
        OnClickMenuItem(item.getItemId());
        return false;
    }

    private boolean OnClickMenuItem(int ItemId) {
        switch (ItemId) {
//            case R.id.preferences_menu_id:
            case R.id.browser_setting_item_setting:
                if (!ClickUtil.clickShort(ItemId)) {
                    openPreferences();
                }
                break;
            case R.id.browser_setting_item_search_page:
                findOnPage();
                onCloseMenu(true);
                break;
            case R.id.browser_setting_item_savedpage:
//            case R.id.save_button_id:
                if (getCurrentTab() == null) break;
                if (canSavedOffLine()) {
                    ToastUtil.showShortToastByString(mActivity, mActivity.getString(R.string.prompt_already_saved_off));
                } else {
                    if (!TextUtils.isEmpty(getCurrentTab().getUrl())) {
                        saveOfflinePage(getCurrentTab());
                        if (mHandler != null) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBottomSetting.updateToolbarStyle();
//                                    onCloseMenu(true,500);
                                }
                            }, 150);
                        }
                    } else {
                        ToastUtil.showShortToastByString(mActivity, mActivity.getString(R.string
                                .bookmark_url_not_valid));
                    }
                }
                break;
            case R.id.loaded_share:
                if (getCurrentTab() != null) {
                    shareCurrentPage(getCurrentTab());
                }
                break;
            case R.id.browser_setting_item_bookmark:
                if (!ClickUtil.clickShort(ItemId)) {
                    ActivityUtils.startComboViewActivity(mActivity, Controller.COMBO_VIEW, ItemId, 0);
                    onCloseMenu(false);
                }
                break;
            case R.id.browser_setting_item_history:
                if (!ClickUtil.clickShort(ItemId)) {
                    ActivityUtils.startComboViewActivity(mActivity, Controller.COMBO_VIEW, ItemId, 1);
                    onCloseMenu(false);
                }
                break;
            case R.id.browser_setting_item_download:
                if (!ClickUtil.clickShort(ItemId)) {
                    ActivityUtils.startDownloadActivity(mActivity, 0);
                    onCloseMenu(false);
                }
                onCloseMenu(false);
                break;
            case R.id.browser_setting_item_intelligence_no_picture:
                boolean isNoimage = BrowserSettings.getInstance().loadImages();
                BrowserSettings.getInstance().getPreferences().edit().putBoolean(PreferenceKeys.PREF_LOAD_IMAGES,
                        !isNoimage).apply();
                refreshWebview();
                mBottomSetting.updateToolbarStyle();
                break;
            case R.id.tabswitcher_toolbar:
            case R.id.browser_tabswitcher_toolbar:
                if (mUi instanceof BrowserPhoneUi) {
                    ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_NAV_SCREEN, mTabControl.getCurrentPosition(),
                            false);
                }
                break;
            case R.id.web_view_title_view:
//                if(!SharedPreferencesUtils.getRecommonWebsites()){
//                    return false;
//                }
                if (!ClickUtil.clickShort(ItemId)) {
                    if (getCurrentTab() != null) {
                        if (!TextUtils.isEmpty(getCurrentTab().getTitle())
                                && ((BaseUi) mUi).getIsSearchResultPage()) {
                            mUi.openSearchInputView(getCurrentTab().getTitle());
                        } else {
                            mUi.openSearchInputView(getCurrentTab().getUrl());
                        }
                    }
                }
                break;
            case R.id.browser_setting_item_refresh:
                refreshWebview();
                onCloseMenu(false);
                break;
            case R.id.browser_setting_item_addbookmark:
                setBookMarksStyle();
                break;
            case R.id.browser_setting_item_ua:
                BrowserSettings.getInstance().setUserAgent(BrowserSettings.getInstance().getUserAgent() ==
                        USER_AGENT_COMPUTER ? USER_AGENT_DEFAULT + "" : USER_AGENT_COMPUTER + "");
                PreBrowserWebView preBrowserWebView = getCurrentTab() != null ? getCurrentTab().getWebView() : null;
                if (preBrowserWebView != null) {
                    BaseWebView wv = preBrowserWebView.getBaseWebView();
                    if (wv != null) {
                        wv.reload();
                    }
                    onCloseMenu(false);
                }
                break;
            case R.id.browser_setting_item_clear:
                BrowserSettingActivity.startPreferenceFragmentForResult(mActivity, BrowserClearDataPreferencesFragment.class.getName(), 1001);
                onCloseMenu(false);
                break;

            case R.id.browser_setting_item_textsize:
                break;

            case R.id.browser_setting_item_offline:
                ActivityUtils.startDownloadActivity(getActivity(),1);
                onCloseMenu(false);
                break;

            case R.id.browser_setting_item_no_cache:
                boolean isNoFooter = BrowserSettings.getInstance().noFooterBrowser();
                BrowserSettings.getInstance().getPreferences().edit().putBoolean(PreferenceKeys.PREF_LOAD_NO_FOOTER,
                        !isNoFooter).apply();
                mUi.onIncognito(!isNoFooter);
                mBottomSetting.updateToolbarStyle();
                break;
            case R.id.browser_setting_item_quit:
                onCloseMenu(true);
                showExitDialog();
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean setBookMarksStyle() {
        String title = getCurrentTab().getTitle();
        boolean isAdd = true;
        if (canAddBookmark()) {
            ToastUtil.showShortToast(mActivity, R.string.remove_bookmark);
            removeBookmark(title);
            isAdd = false;
        } else {
//                    ToastUtil.showShortToast(mActivity, R.string.bookmark_saved);
            addBookmark(title);
            isAdd = true;
        }

        try {
            mBottomSetting.updateToolbarStyle();
        } catch (Exception e) {
        }
        return isAdd;
    }

    @Override
    public void refreshWebview() {
        BaseWebView webView = getCurrentTab().getWebView().getBaseWebView();
        if (webView != null) {
            webView.reload();
        }
    }

    @Override
    public void showWebViewPage(boolean isShowWebView) {
        if (mUi != null) {
            mUi.showWebViewPage(isShowWebView);
        }
    }

    @Override
    public void showTools(boolean isShow) {
        if (mUi != null) {
            mUi.showTools(isShow);
        }
    }

    @Override
    public void toobarunfold(float isOpen) {
        if (mUi != null) {
            mUi.toobarunfold(isOpen);
        }
    }

    private void saveOfflinePage(Tab tab) {
        tab.saveViewState();
    }

    @Override
    public void toggleUserAgent() {
        PreBrowserWebView web = getCurrentWebView();
        mSettings.toggleDesktopUseragent(web);
        BaseWebView webView = web != null ? web.getBaseWebView() : null;
        if (webView != null) web.loadUrl(webView.getOriginalUrl());
    }

    @Override
    public void findOnPage() {
        showFindDialog();
    }

    private Boolean mFindPopShow = false;
    private BrowserFindPopDialog mFindDialog;

    public Boolean getFindPopShow() {
        return mFindPopShow;
    }

    public void setFindPopShow(Boolean mFindPopShow) {
        this.mFindPopShow = mFindPopShow;
    }


    private void showFindDialog() {
        if (mFindPopShow) {
            return;
        }

        if (mFindDialog == null) {
            mFindDialog = new BrowserFindPopDialog(mActivity, R.style.dialog, getCurrentBrowserWebView());
        } else {
            mFindDialog.setWebView(getCurrentBrowserWebView());
        }

        mFindDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setFindPopShow(false);
                mFindDialog.clear();
            }
        });
        Window win = mFindDialog.getWindow();
        mFindDialog.show();
        setFindPopShow(true);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.x = 0;//设置x坐标
        params.y = -DisplayUtil.getStatusBarHeight(mActivity);//设置y坐标
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = (int) mActivity.getResources().getDimension(R.dimen
                .find_pop_view_height);

        win.setAttributes(params);
        win.setGravity(Gravity.TOP);
        win.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.setWindowAnimations(R.style.popup_push_bottom);  //添加动画
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mFindDialog != null && mFindDialog.getmInputView() != null) {
                    InputMethodUtils.showKeyboardForView(mFindDialog.getmInputView());
                }
            }
        }, 200);
    }

    @Override
    public void openPreferences() {
        BrowserSettingActivity.startPreferencesForResult(mActivity, PREFERENCES_PAGE);
    }

    /**
     * catches user set default browser option , open preferencesPage to confirm.
     * add by simon liu
     */

    public void openPreferencesToSetDefaultBrowser() {
        BrowserSettingActivity.startPreferencesToSetDefaultBrowserForResult(mActivity, PREFERENCES_PAGE);
    }

    @Override
    public void bookmarkCurrentPage() {
        Intent bookmarkIntent = createBookmarkCurrentPageIntent(false);
        if (bookmarkIntent != null) {
            mActivity.startActivity(bookmarkIntent);
        }
    }

    private void goLive() {
        Tab t = getCurrentTab();
        t.loadUrl(t.getUrl(), null);
    }

    @Override
    public void showPageInfo() {
        mPageDialogsHandler.showPageInfo(mTabControl.getCurrentTab(), false, null);
    }

    public boolean onContextItemSelected(int id) {
        return onContextItemSelected("", id);
    }

    private boolean checkAdblockCon() {
        boolean isClose = BrowserSettings.getInstance().getAdBlockEnabled();
        if (!isClose) {
            return false;
        }
        String position = BrowserSettings.getInstance().getDefaultAdTimeLite();
        if ("3".equals(position)) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long lastTime = (long)SharedPreferencesUtils.get(SharedPreferencesUtils.AD_CLOSE_TIME_CURRENT_KEY,0l);
        if ("2".equals(position) && currentTime - lastTime < 24 * 60 * 60 * 1000) {
            return true;
        }
        if ("1".equals(position) && currentTime - lastTime < 12 * 60 * 60 * 1000) {
            return true;
        }
        if ("0".equals(position) && currentTime - lastTime < 1 * 60 * 60 * 1000) {
            return true;
        }
        return false;
    }

    private void clearAd() {
        try {
            if (!checkAdblockCon()) {
                return;
            }
            String[] imageUrls = StringUtil.getStringForArray(ImageJavascriptInterface.mAdLists);
            BrowserSettings.getInstance().setImgAdBlockCount(BrowserSettings.getInstance().getImgAdBlockCount() + 1);
            BrowserSettings.getInstance().setJsAdBlockCount(BrowserSettings.getInstance().getJsAdBlockCount() + 1);
            if (imageUrls == null || imageUrls.length == 0) {
                return;
            }
            StringUtil.injectJavascript(mActivity, getCurrentBrowserWebView(), "webview_ad.js");
            injectJs("javascript:__byfrost_browser__.closeads(" + StringUtil.join(",", imageUrls) + ")");
        } catch (Exception e) {
        }
    }

    private void clearAd(String imageUrl) {
        try {
            injectJs("javascript:__byfrost_browser__.closead(" + imageUrl + ")");
        } catch (Exception e) {
        }
    }

    public boolean onContextItemSelected(String imageUrl, int id) {
        // Let the History and Bookmark fragments handle menus they created.
        boolean result = true;
        switch (id) {
            case R.id.image_ad_mark:
                if (!TextUtils.isEmpty(imageUrl) && !StringUtils.isBase64(imageUrl)) {
                    StringUtil.injectJavascript(mActivity,getCurrentBrowserWebView(),"webview_ad.js");
                    clearAd("'"+imageUrl+"'");
                    break;
                }
            case R.id.copy_link_context_menu_id:
            case R.id.open_context_menu_id:
            case R.id.save_link_context_menu_id:
            case R.id.open_newtab_context_menu_id:
            case R.id.open_newtab_background_context_menu_id:
                final PreBrowserWebView preBrowserWebView = getCurrentTopWebView();
                if (null == preBrowserWebView) {
                    result = false;
                    break;
                }
                BaseWebView webView = preBrowserWebView.getBaseWebView();
                if (webView != null) {
                    final Map<String, WebView> hrefMap =
                            new ArrayMap<>();
                    hrefMap.put("webview", webView);
                    final Message msg = mHandler.obtainMessage(
                            FOCUS_NODE_HREF, id, 0, hrefMap);
                    webView.requestFocusNodeHref(msg);
                }
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * support programmatically opening the context menu
     */
    public void openContextMenu(View view) {
        mActivity.openContextMenu(view);
    }

    /**
     * programmatically open the options menu
     */
    public void openOptionsMenu() {
        mActivity.openOptionsMenu();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (mOptionsMenuOpen) {
            if (mConfigChanged) {
                // We do not need to make any changes to the state of the
                // title bar, since the only thing that happened was a
                // change in orientation
                mConfigChanged = false;
            } else {
                if (!mExtendedMenuOpen) {
                    mExtendedMenuOpen = true;
                    mUi.onExtendedMenuOpened();
                } else {
                    // Switching the menu back to icon view, so show the
                    // title bar once again.
                    mExtendedMenuOpen = false;
                    mUi.onExtendedMenuClosed(isInLoad());
                }
            }
        } else {
            // The options menu is closed, so open it, and show the title
            mOptionsMenuOpen = true;
            mConfigChanged = false;
            mExtendedMenuOpen = false;
            mUi.onOptionsMenuOpened();
        }
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        mOptionsMenuOpen = false;
        mUi.onOptionsMenuClosed(isInLoad());
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        mUi.onContextMenuClosed(menu, isInLoad());
    }

    // Helper method for getting the top window.
    @Override
    public PreBrowserWebView getCurrentTopWebView() {
        return mTabControl.getCurrentTopWebView();
    }

    public BaseWebView getCurrentTopBrowserWebView() {
        PreBrowserWebView currentTopWebView = getCurrentTopWebView();
        return currentTopWebView == null ? null : currentTopWebView.getBaseWebView();
    }

    @Override
    public PreBrowserWebView getCurrentWebView() {
        return mTabControl.getCurrentWebView();
    }

    public BaseWebView getCurrentBrowserWebView() {
        PreBrowserWebView currentWebView = mTabControl.getCurrentWebView();
        return currentWebView == null ? null : currentWebView.getBaseWebView();
    }

    /*
     * This method is called as a result of the user selecting the options
     * menu to see the download window. It shows the download window on top of
     * the current window.
     */
    void viewDownloads() {
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        mActivity.startActivity(intent);
    }

    int getActionModeHeight() {
        TypedArray actionBarSizeTypedArray = mActivity.obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int size = (int) actionBarSizeTypedArray.getDimension(0, 0f);
        actionBarSizeTypedArray.recycle();
        return size;
    }

    // action mode

    @Override
    public void onActionModeStarted(ActionMode mode) {
        mUi.onActionModeStarted(mode);
        mActionMode = mode;
    }

    /*
     * True if a custom ActionMode (i.e. find or select) is in use.
     */
    @Override
    public boolean isInCustomActionMode() {
        return mActionMode != null;
    }

    /*
     * End the current ActionMode.
     */
    @Override
    public void endActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    /*
     * Called by find and select when they are finished.  Replace title bars
     * as necessary.
     */
    @Override
    public void onActionModeFinished(ActionMode mode) {
        if (!isInCustomActionMode()) return;
        mUi.onActionModeFinished(isInLoad());
        mActionMode = null;
    }

    boolean isInLoad() {
        final Tab tab = getCurrentTab();
        return (tab != null) && tab.inPageLoad();
    }

    // bookmark handling

    /**
     * add the current page as a bookmark to the given folder id
     *
     * @param editExisting If true, check to see whether the site is already
     *                     bookmarked, and if it is, edit that bookmark.  If false, and
     *                     the site is already bookmarked, do not attempt to edit the
     *                     existing bookmark.
     */
    @Override
    public Intent createBookmarkCurrentPageIntent(boolean editExisting) {
        PreBrowserWebView w = getCurrentTopWebView();
        if (w == null) return null;
        BaseWebView webView = w.getBaseWebView();
        if (webView == null) return null;
        Intent i = new Intent(mActivity,
                BrowserAddBookmarkPage.class);
        i.putExtra(BrowserContract.Bookmarks.URL, webView.getUrl());
        i.putExtra(BrowserContract.Bookmarks.TITLE, webView.getTitle());
        String touchIconUrl = "";//w.getTouchIconUrl();
        i.putExtra(BrowserAddBookmarkPage.TOUCH_ICON_URL, touchIconUrl);
        WebSettings settings = webView.getSettings();
        if (settings != null) {
            i.putExtra(BrowserAddBookmarkPage.USER_AGENT,
                    i.putExtra(BrowserAddBookmarkPage.USER_AGENT,
                            settings.getUserAgentString()));
        }
        i.putExtra(BrowserContract.Bookmarks.THUMBNAIL,
                createScreenshot(webView, getDesiredThumbnailWidth(mActivity),
                        getDesiredThumbnailHeight(mActivity)));
        i.putExtra(BrowserContract.Bookmarks.FAVICON, webView.getFavicon());
        if (editExisting) {
            i.putExtra(BrowserAddBookmarkPage.CHECK_FOR_DUPE, true);
        }
        // Put the dialog at the upper right of the screen, covering the
        // star on the title bar.
        i.putExtra("gravity", Gravity.RIGHT | Gravity.TOP);
        return i;
    }

    // file chooser
    @Override
    public void showFileChooser(ValueCallback<Uri[]> callback, FileChooserParams params) {
        mUploadHandler = new UploadHandler(this);
        mUploadHandler.openFileChooser(callback, params);
    }

    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadHandler = new UploadHandler(this);
        mUploadHandler.openFileChooser(uploadMsg, acceptType, capture);
    }
// thumbnails

    /**
     * Return the desired width for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     *
     * @param context Context for finding out the density of the screen.
     * @return desired width for thumbnail screenshot.
     */
    static int getDesiredThumbnailWidth(Context context) {
        return context.getResources().getDimensionPixelOffset(
                R.dimen.bookmarkThumbnailWidth);
    }

    /**
     * Return the desired height for thumbnail screenshots, which are stored in
     * the database, and used on the bookmarks screen.
     *
     * @param context Context for finding out the density of the screen.
     * @return desired height for thumbnail screenshot.
     */
    static int getDesiredThumbnailHeight(Context context) {
        return context.getResources().getDimensionPixelOffset(
                R.dimen.bookmarkThumbnailHeight);
    }

    static Bitmap createScreenshot(WebView view, int width, int height) {
        if (view == null || view.getWidth() == 0
            /*|| view.getContentWidth() == 0*/) {
            return null;
        }
        // We render to a bitmap 2x the desired size so that we can then
        // re-scale it with filtering since canvas.scale doesn't filter
        // This helps reduce aliasing at the cost of being slightly blurry
        final int filter_scale = 2;
        int scaledWidth = width * filter_scale;
        int scaledHeight = height * filter_scale;
        if (sThumbnailBitmap == null || sThumbnailBitmap.getWidth() != scaledWidth
                || sThumbnailBitmap.getHeight() != scaledHeight) {
            if (sThumbnailBitmap != null) {
                sThumbnailBitmap.recycle();
                sThumbnailBitmap = null;
            }
            sThumbnailBitmap =
                    Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(sThumbnailBitmap);
        int contentWidth = view.getWidth();//view.getContentWidth();
        float overviewScale = scaledWidth / (view.getScale() * contentWidth);
        canvas.scale(overviewScale, overviewScale);

        if (view instanceof BaseWebView) {
            ((BaseWebView) view).drawContent(canvas);
        } else {
            view.draw(canvas);
        }
        Bitmap ret = Bitmap.createScaledBitmap(sThumbnailBitmap,
                width, height, true);
        canvas.setBitmap(null);
        return ret;
    }

    private void updateScreenshot(Tab tab) {
        // If this is a bookmarked site, add a screenshot to the database.
        // FIXME: Would like to make sure there is actually something to
        // draw, but the API for that (WebViewCore.pictureReady()) is not
        // currently accessible here.

        WebView view = tab.getCurrentBrowserWebView();
        if (view == null) {
            // Tab was destroyed
            return;
        }
        final String url = tab.getUrl();
        final String originalUrl = view.getOriginalUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }

        // Only update thumbnails for web urls (http(s)://), not for
        // about:, javascript:, data:, etc...
        // Unless it is a bookmarked site, then always update
        if (!Patterns.WEB_URL.matcher(url).matches() && !tab.isBookmarkedSite()) {
            return;
        }

        final Bitmap bm = createScreenshot(view, getDesiredThumbnailWidth(mActivity),
                getDesiredThumbnailHeight(mActivity));
        if (bm == null) {
            return;
        }

        final ContentResolver cr = mActivity.getContentResolver();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                Cursor cursor = null;
                try {
                    // TODO: Clear this up
                    cursor = Bookmarks.queryCombinedForUrl(cr, originalUrl, url);
                    if (cursor != null && cursor.moveToFirst()) {
                        final ByteArrayOutputStream os =
                                new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.PNG, 100, os);

                        ContentValues values = new ContentValues();
                        if (os.toByteArray().length < BuildUtil.WEB_SITE_ICON_LENGHT) {
                            values.put(Images.THUMBNAIL, os.toByteArray());
                        }

                        do {
                            values.put(Images.URL, cursor.getString(0));
                            cr.update(Images.CONTENT_URI, values, null, null);
                        } while (cursor.moveToNext());
                    }
                } catch (IllegalStateException e) {
                    // Ignore
                } catch (SQLiteException s) {
                } finally {
                    if (cursor != null) cursor.close();
                }
                return null;
            }
        }.execute();
    }


    /********************** TODO: UI stuff *****************************/

    // these methods have been copied, they still need to be cleaned up

    /******************
     * tabs
     ***************************************************/

    // basic tab interactions:

    // it is assumed that tabcontrol already knows about the tab
    protected void addTab(Tab tab) {
        mUi.addTab(tab);
    }

    protected void removeTab(Tab tab) {
        mUi.removeJsObjectRef(tab);
        mUi.removeTab(tab);
        mTabControl.removeTab(tab);
        mCrashRecoveryHandler.backupState();
    }

    @Override
    public void setActiveTab(Tab tab) {
        // monkey protection against delayed start
        if (tab != null) {
            mTabControl.setCurrentTab(tab);
            // the tab is guaranteed to have a webview after setCurrentTab
            mUi.setActiveTab(tab);
            injectJsObject(tab, false);
        }
    }

    public  void closeEmptyTab() {
        Tab current = mTabControl.getCurrentTab();
        if (current != null) {
            PreBrowserWebView preBrowserWebView = current.getWebView();
            if (preBrowserWebView != null) {
                BaseWebView webView = preBrowserWebView.getBaseWebView();
                if (webView != null && webView.copyBackForwardList().getSize() == 0 && !current.isNativePage()) {
                    closeCurrentTab();

                }
            }
        }
    }

    public void reuseTab(Tab appTab, UrlData urlData) {
        // Dismiss the subwindow if applicable.
        dismissSubWindow(appTab);
        // Since we might kill the WebView, remove it from the
        // content view first.
        mUi.detachTab(appTab);
        // Recreate the main WebView after destroying the old one.
        mTabControl.recreateWebView(appTab);
        // TODO: analyze why the remove and add are necessary
        mUi.attachTab(appTab);
        if (mTabControl.getCurrentTab() != appTab) {
            switchToTab(appTab);
            loadUrlDataIn(appTab, urlData);
        } else {
            // If the tab was the current tab, we have to attach
            // it to the view system again.
            setActiveTab(appTab);
            loadUrlDataIn(appTab, urlData);
        }
    }

    private void switchTabMode(final Tab appTab, final String url, final boolean incognito) {
        if (appTab != null) {
            final View mainContent = mUi.getMainContent();
            int distance = 16000;
            float cameraDist = mActivity.getResources().getDisplayMetrics().density * distance;
            mainContent.setCameraDistance(cameraDist);
            final boolean reverse = true;
            final boolean scale = true;
            final int duration = 200;
            final int degree = reverse ? 90 : -90;
            final int degree2 = -degree;

            final ObjectAnimator a, b;
            if (!scale) {
                a = ObjectAnimator.ofFloat(mainContent, "rotationY", 0, degree);
                b = ObjectAnimator.ofFloat(mainContent, "rotationY", degree2, 0);
            } else {
                final float scaleX = 0.8f;
                final float scaleY = 0.8f;
                a = ObjectAnimator.ofPropertyValuesHolder(mainContent,
                        PropertyValuesHolder.ofFloat("rotationY", 0, degree),
                        PropertyValuesHolder.ofFloat("scaleX", 1, scaleX),
                        PropertyValuesHolder.ofFloat("scaleY", 1, scaleY));
                b = ObjectAnimator.ofPropertyValuesHolder(mainContent,
                        PropertyValuesHolder.ofFloat("rotationY", degree2, 0),
                        PropertyValuesHolder.ofFloat("scaleX", scaleX, 1),
                        PropertyValuesHolder.ofFloat("scaleY", scaleY, 1));
            }

            a.setInterpolator(new DecelerateInterpolator());
            b.setInterpolator(new AccelerateInterpolator());
            a.setDuration(duration);
            b.setDuration(duration);
            b.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    String onOrOff = null;
                    String toast = null;
                    if (incognito) {
                        onOrOff = mActivity.getResources().getString(R.string.toolbox_menu_on);
                    } else {
                        onOrOff = mActivity.getResources().getString(R.string.toolbox_menu_off);
                    }
                    toast = mActivity.getResources().getString(R.string.menu_browser_incognito) + " : " + onOrOff;
                    ToastUtil.showShortToastByString(mActivity, toast);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });

            a.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    closeCurrentTab(false, false);
                    if (appTab.isNativePage()) {
                        if (mUi instanceof BrowserPhoneUi) {
                            if (incognito) {
                                openIncognitoTab();
                            } else {
                                openTabToHomePage();
                            }
                        }
                    } else {
                        openTab(url, incognito, true, true);
                    }
                    if (scale) {
                        mainContent.setScaleX(1);
                        mainContent.setScaleY(1);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            AnimatorSet set = new AnimatorSet();
            set.play(a).before(b);
            set.start();
        }
    }

    // Remove the sub window if it exists. Also called by TabControl when the
    // user clicks the 'X' to dismiss a sub window.
    @Override
    public void dismissSubWindow(Tab tab) {
        removeSubWindow(tab);
        // dismiss the subwindow. This will destroy the WebView.
        tab.dismissSubWindow();
        PreBrowserWebView wv = getCurrentTopWebView();
        if (wv != null) {
            wv.requestFocus();
        }
    }

    @Override
    public void removeSubWindow(Tab t) {
        if (t.getSubWebView() != null) {
            mUi.removeSubWindow(t.getSubViewContainer());
        }
    }

    @Override
    public void attachSubWindow(Tab tab) {
        if (tab.getSubWebView() != null) {
            mUi.attachSubWindow(tab.getSubViewContainer());
            getCurrentTopWebView().requestFocus();
        }
    }

    private Tab showPreloadedTab(final UrlData urlData) {
        if (!urlData.isPreloaded()) {
            return null;
        }
        final PreloadedTabControl tabControl = urlData.getPreloadedTab();
        final String sbQuery = urlData.getSearchBoxQueryToSubmit();
        if (sbQuery != null) {
            if (!tabControl.searchBoxSubmit(sbQuery, urlData.mUrl, urlData.mHeaders)) {
                // Could not submit query. Fallback to regular tab creation
                tabControl.destroy();
                return null;
            }
        }
        // check tab count and make room for new tab
        if (!mTabControl.canCreateNewTab()) {
            Tab leastUsed = mTabControl.getLeastUsedTab(getCurrentTab());
            if (leastUsed != null) {
                closeTab(leastUsed);
            }
        }
        Tab t = tabControl.getTab();
        t.refreshIdAfterPreload();
        mTabControl.addPreloadedTab(t);
        addTab(t);
        setActiveTab(t);
        return t;
    }

    // open a non inconito tab with the given url data
    // and set as active tab
    public Tab openTab(UrlData urlData) {
        Tab tab = showPreloadedTab(urlData);
        if (tab == null) {
            tab = createNewTab(false, true, true);
            if ((tab != null) && !urlData.isEmpty()) {
                loadUrlDataIn(tab, urlData);
            }
        }
        return tab;
    }

    @Override
    public void createAndOpenTab(String url) {
        if (getCurrentTab().isNativePage()) {
            openUrl(url);
        } else {
            Tab tab = openTab(url, false, true, false);
        }
    }

    @Override
    public void createAndOpenTabIncognito(String url) {
        Tab tab = openTab(url, true, true, false, getCurrentTab());
        setBlockEvents(true);
        injectJsObject(tab, true);
        setBlockEvents(false);
    }


    @Override
    public Tab openTabToHomePage() {
//        return openTab(mSettings.getHomePage(), false, true, false);
        return openTab(mSettings.getHomePage(), false, true, true);
    }

    @Override
    public Tab openIncognitoTab() {
        return openTab(mSettings.getHomePage(), true, true, true);
    }

    @Override
    public Tab openTab(String url, boolean incognito, boolean setActive,
                       boolean useCurrent) {
        return openTab(url, incognito, setActive, useCurrent, null);
    }

    private String mCurrentExtra;

    @Override
    public void setWallpaper(Context activity, String mExtra) {
        if (getContext() == null || TextUtils.isEmpty(mExtra)) {
            return;
        }
        mCurrentExtra = mExtra;
        boolean hasPermission = DownloadHandler.hasReadWritePermissions(getContext());
        if (hasPermission) {
            new WallpaperHandler(activity, mExtra).onMenuItemClick();
        } else {
            DeviceInfoUtils.verifyStoragePermissions((Activity) getContext(), DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_WALL);
        }
    }

    public String mImageUrl;

    @Override
    public void viewImage(String url, Tab parent, boolean setActive, boolean useCurrent) {
        try {
            if(StringUtils.isBase64(url)){
                String localPath = StringUtils.saveBase64Image(mActivity,url);
                openPhotoView(new String[]{localPath});
                return;
            }
            if(url.length()>50000){
                openPhotoView(new String[]{url});
                return;
            }
            mImageUrl = url;
            StringUtil.injectJavascript(mActivity,getCurrentBrowserWebView(),"image_show.js");
            if(url.startsWith("blob:")){
                injectJs("javascript:__byfrost_browser__.getBlobAsBase64('" + url + "')");
                return;
            }
            injectJs("javascript:__byfrost_browser__.openimages('" + mImageUrl + "')");
        } catch (Exception e) {
            openPhotoView(new String[]{url});
        }
    }

    public void openPhotoView() {
        openPhotoView(new String[]{mImageUrl});
    }

    public boolean isSameMediaType(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return true;
        }
        String mediatype = FileUtils.getPicMediaType(mImageUrl);
        String imageUrlType = FileUtils.getPicMediaType(imageUrl);
        return (mediatype + "").equals(imageUrlType + "");
    }

    public void openPhotoView(String[] images) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if(images != null && images[0].length()>50000){
                        return;
                    }
                    Intent intent = new Intent(getContext(), PhotoViewPagerActivity.class);
                    intent.putExtra(PhotoViewPagerActivity.IMAGE_URL_PRAMAS, images);
                    intent.putExtra(PhotoViewPagerActivity.IMAGE_FROM_NET, "1");
                    intent.putExtra(PhotoViewPagerActivity.IMAGE_POSITION, StringUtil.getPosition(images, mImageUrl));
                    getActivity().startActivityForResult(intent,COMBO_VIEW);
                    mImageUrl = "";
                } catch (Exception e) {
                }
            }
        });
    }

    public void injectJs(String js) {
        WebView webview = getCurrentWebView().getBaseWebView();
        if (webview != null && !TextUtils.isEmpty(js)) {
            webview.loadUrl(js);
        }
    }

    @Override
    public Tab openTab(String url, Tab parent, boolean setActive,
                       boolean useCurrent) {
        return openTab(url, (parent != null) && parent.isPrivateBrowsingEnabled(),
                setActive, useCurrent, parent);
    }

    public Tab openTab(String url, boolean incognito, boolean setActive,
                       boolean useCurrent, Tab parent) {
        Tab tab = createNewTab(incognito, setActive, useCurrent);
        mParentTab = parent;
        if (tab != null) {
            if (parent instanceof SnapshotTab) {
                addTab(tab);
                if (setActive) {
                    setActiveTab(tab);
                }
            } else if (parent != null && parent != tab) {
                parent.addChildTab(tab);
            }
            if (!Constants.NATIVE_PAGE_URL.equals(url) || setActive) {
                if (url != null) {
                    loadUrl(tab, url);
                }
            }
        }
        return tab;
    }

    @Override
    public void openTab(Tab tab, String url) {
        addTab(tab);
        setActiveTab(tab);

        if (TextUtils.isEmpty(url)) return;
        if (UrlUtils.isSearch(url)) {
            url = com.intelligence.browser.utils.UrlUtils.filterBySearchEngine(mActivity, url);
        }

        url = UrlUtils.smartUrlFilter(url);

        injectJsObject(tab, true);
        loadUrl(tab, url);
    }

    // this method will attempt to create a new tab
    // incognito: private browsing tab
    // setActive: ste tab as current tab
    // useCurrent: if no new tab can be created, return current tab
    private Tab createNewTab(boolean incognito, boolean setActive,
                             boolean useCurrent) {
        Tab tab = null;
        if (mTabControl.canCreateNewTab()) {
            tab = mTabControl.createNewTab(incognito);
            addTab(tab);
            if (setActive) {
                setActiveTab(tab);
            }
        } else {
            if (useCurrent) {
                tab = mTabControl.getCurrentTabForMode(incognito);
                reuseTab(tab, null);
            } else {
                mUi.showMaxTabsWarning();
            }
        }
        return tab;
    }

    public SnapshotTab createNewSnapshotTab(long snapshotId, boolean setActive) {
        SnapshotTab tab = null;
        if (mTabControl.canCreateNewTab()) {
            tab = mTabControl.createSnapshotTab(snapshotId, mTabControl.isIncognitoShowing());
            addTab(tab);
            if (setActive) {
                setActiveTab(tab);
                if (mUi instanceof BrowserPhoneUi) {
                    ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_WEBVIEW, mTabControl.getCurrentPosition(), false);
                }
            }
        } else {
            mUi.showMaxTabsWarning();
        }
        return tab;
    }

    /**
     * @param tab the tab to switch to
     * @return boolean True if we successfully switched to a different tab.  If
     * the indexth tab is null, or if that tab is the same as
     * the current one, return false.
     */
    @Override
    public boolean switchToTab(Tab tab) {
        Tab currentTab = mTabControl.getCurrentTab();
        if (tab == null || tab == currentTab) {
            return false;
        }
        setActiveTab(tab);
        return true;
    }

    @Override
    public void closeCurrentTab() {
        closeCurrentTab(false);
    }

    protected void closeCurrentTab(boolean andQuit) {
        closeCurrentTab(andQuit, false);
    }

    private void closeCurrentTab(boolean andQuit, boolean createNewOnLastTab) {
        if (mTabControl.getTabCount() == 1) {
            mCrashRecoveryHandler.clearState();
            mUi.removeJsObjectRef(getCurrentTab());
            mTabControl.removeTab(getCurrentTab());
            if (createNewOnLastTab) {
                if (mUi instanceof BrowserPhoneUi) {
                    openTabToHomePage();
                }
            }
            return;
        }
        final Tab current = mTabControl.getCurrentTab();
        final int pos = mTabControl.getCurrentPosition();
        Tab newTab = current.getParent();
        if (newTab == null) {
            newTab = mTabControl.getTab(pos - 1);
            if (newTab == null) {
                newTab = mTabControl.getTab(pos + 1);
            }
        }
        if (andQuit) {
            mTabControl.setCurrentTab(newTab);
            closeTab(current);
        } else if (switchToTab(newTab)) {
            // Close window
            closeTab(current);
        }
    }

    /**
     * Close the tab, remove its associated title bar, and adjust mTabControl's
     * current tab to a valid value.
     */
    @Override
    public void closeTab(Tab tab) {
        if (tab == mTabControl.getCurrentTab()) {
            closeCurrentTab();
        } else {
            removeTab(tab);
        }

    }

    /**
     * Close all tabs except the current one
     */
    @Override
    public void closeOtherTabs() {
        int inactiveTabs = mTabControl.getTabCount() - 1;
        for (int i = inactiveTabs; i >= 0; i--) {
            Tab tab = mTabControl.getTab(i);
            if (tab != mTabControl.getCurrentTab()) {
                removeTab(tab);
            }
        }
    }

    /**
     * Close all tabs
     */
    @Override
    public void closeAllTabs(boolean incognito) {
        boolean originIncognito = mTabControl.isIncognitoShowing();
        if (incognito != originIncognito) {
            mTabControl.setIncognitoShowing(incognito);
        }
        int inactiveTabs = mTabControl.getTabCount() - 1;
        for (int i = inactiveTabs; i >= 0; i--) {
            Tab tab = mTabControl.getTab(i);
            if (i == 0 && incognito == mTabControl.isIncognitoShowing()) {
                mTabControl.setIncognitoShowing(!incognito);
            }
            removeTab(tab);
            if (i == 0) {
                Tab t = mTabControl.getCurrentTab();
                if (t == null) {
                    openTabToHomePage();
                } else if (t.isNativePage()) {
                    panelSwitchHome(t);
                } else {
                    if (mUi instanceof BrowserPhoneUi) {
                        ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_WEBVIEW, mTabControl.getCurrentPosition(),
                                false);
                    }
                }
            }
        }
    }

    // Called when loading from context menu or LOAD_URL message
    protected void loadUrlFromContext(String url) {
        Tab tab = getCurrentTab();
        PreBrowserWebView view = tab != null ? tab.getWebView() : null;
        if (view == null) return;
        BaseWebView webView = view.getBaseWebView();
        // In case the user enters nothing.
        if (url != null && url.length() != 0 && webView != null) {
            url = UrlUtils.smartUrlFilter(url);
            if (view.getBaseWebView() != null && !view.getBaseWebView().getWebViewClient().
                    shouldOverrideUrlLoading(webView, url)) {
                loadUrl(tab, url);
            }
        }
    }

    @Override
    public void loadNativePage(Tab tab) {
        loadUrl(tab, Constants.NATIVE_PAGE_URL);
    }

    public Tab getParentTab() {
        return mParentTab;
    }

    /**
     * Load the URL into the given WebView and update the title bar
     * to reflect the new load.  Call this instead of WebView.loadUrl
     * directly.
     *
     * @param url The URL to load.
     */
    @Override
    public void loadUrl(Tab tab, String url) {
        loadUrl(tab, url, null);
    }

    public void loadUrl(Tab tab, String url, boolean needSwitch) {
        loadUrl(tab, url, null, needSwitch);
    }

    protected void loadUrl(Tab tab, String url, Map<String, String> headers) {
        loadUrl(tab, url, headers, true);
    }

    protected void loadUrl(Tab tab, String url, Map<String, String> headers, boolean needSwitch) {
        if (tab != null) {
            dismissSubWindow(tab);
            if (!TextUtils.isEmpty(url) && url.equals(Constants.NATIVE_PAGE_URL)) {
                if (mBrowserMainPageController.getInitStatus() == BrowserMainPageController.STATUS_EMPTY) {
                    mBrowserMainPageController.initRootView();
                }
                if (mUi instanceof BrowserPhoneUi) {
                    ((BrowserPhoneUi) mUi).panelSwitchHome(mTabControl.getTabPosition(tab), true);
                }
            } else {

                tab.loadUrl(url, headers);
                if (needSwitch) {
                    ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_WEBVIEW, mTabControl.getCurrentPosition(), true);
                }
            }
            mUi.onProgressChanged(tab);
        }
    }

    /**
     * Load UrlData into a Tab and update the title bar to reflect the new
     * load.  Call this instead of UrlData.loadIn directly.
     *
     * @param t    The Tab used to load.
     * @param data The UrlData being loaded.
     */
    public void loadUrlDataIn(Tab t, UrlData data) {
        if (data != null) {
            if (data.isPreloaded()) {
                // this isn't called for preloaded tabs
            } else {
                if (t != null && data.mDisableUrlOverride) {
                    t.disableUrlOverridingForLoad();
                }
                loadUrl(t, data.mUrl, data.mHeaders);
            }
        }
    }

    @Override
    public void onUserCanceledSsl(Tab tab) {
        // Go back directly.
        goBack();
    }

    /**
     * helper method for key handler
     * returns the current tab if it can't advance
     */
    private Tab getNextTab() {
        int pos = mTabControl.getCurrentPosition() + 1;
        if (pos >= mTabControl.getTabCount()) {
            pos = 0;
        }
        return mTabControl.getTab(pos);
    }

    /**
     * helper method for key handler
     * returns the current tab if it can't advance
     */
    private Tab getPrevTab() {
        int pos = mTabControl.getCurrentPosition() - 1;
        if (pos < 0) {
            pos = mTabControl.getTabCount() - 1;
        }
        return mTabControl.getTab(pos);
    }

    boolean isMenuOrCtrlKey(int keyCode) {
        return (KeyEvent.KEYCODE_MENU == keyCode)
                || (KeyEvent.KEYCODE_CTRL_LEFT == keyCode)
                || (KeyEvent.KEYCODE_CTRL_RIGHT == keyCode);
    }

    /**
     * handle key events in browser
     *
     * @return true if handled, false to pass to super
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBottomSetting != null && mBottomSetting.isAdded()) {
            dettachFragment(mBottomSetting);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            return true;
        }

        boolean noModifiers = event.hasNoModifiers();
        // Even if MENU is already held down, we need to call to super to open
        // the IME on long press.
        if (!noModifiers && isMenuOrCtrlKey(keyCode)) {
            mMenuIsDown = true;
            return false;
        }

        PreBrowserWebView webView = getCurrentTopWebView();
        Tab tab = getCurrentTab();
        if (webView == null || tab == null) return false;

        boolean ctrl = event.hasModifiers(KeyEvent.META_CTRL_ON);
        boolean shift = event.hasModifiers(KeyEvent.META_SHIFT_ON);

        switch (keyCode) {
            case KeyEvent.KEYCODE_TAB:
                if (event.isCtrlPressed()) {
                    if (event.isShiftPressed()) {
                        // prev tab
                        switchToTab(getPrevTab());
                    } else {
                        // next tab
                        switchToTab(getNextTab());
                    }
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_SPACE:
                // WebView/WebTextView handle the keys in the KeyDown. As
                // the Activity's shortcut keys are only handled when WebView
                // doesn't, have to do it in onKeyDown instead of onKeyUp.
                if (shift) {
                    pageUp();
                } else if (noModifiers) {
                    pageDown();
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (!noModifiers) break;
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_FORWARD:
                if (!noModifiers) break;
                tab.goForward();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (ctrl) {
                    tab.goBack();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (ctrl) {
                    tab.goForward();
                    return true;
                }
                break;
//          case KeyEvent.KEYCODE_B:    // menu
//          case KeyEvent.KEYCODE_D:    // menu
//          case KeyEvent.KEYCODE_E:    // in Chrome: puts '?' in URL bar
//          case KeyEvent.KEYCODE_F:    // menu
//          case KeyEvent.KEYCODE_G:    // in Chrome: finds next match
//          case KeyEvent.KEYCODE_H:    // menu
//          case KeyEvent.KEYCODE_I:    // unused
//          case KeyEvent.KEYCODE_J:    // menu
//          case KeyEvent.KEYCODE_K:    // in Chrome: puts '?' in URL bar
//          case KeyEvent.KEYCODE_L:    // menu
//          case KeyEvent.KEYCODE_M:    // unused
//          case KeyEvent.KEYCODE_N:    // in Chrome: new window
//          case KeyEvent.KEYCODE_O:    // in Chrome: open file
//          case KeyEvent.KEYCODE_P:    // in Chrome: print page
//          case KeyEvent.KEYCODE_Q:    // unused
//          case KeyEvent.KEYCODE_R:
//          case KeyEvent.KEYCODE_S:    // in Chrome: saves page
            case KeyEvent.KEYCODE_T:
                // we can't use the ctrl/shift flags, they check for
                // exclusive use of a modifier
                if (event.isCtrlPressed()) {
                    if (event.isShiftPressed()) {
                        openIncognitoTab();
                    } else {
                        openTabToHomePage();
                    }
                    return true;
                }
                break;
//          case KeyEvent.KEYCODE_U:    // in Chrome: opens source of page
//          case KeyEvent.KEYCODE_V:    // text view intercepts to paste
//          case KeyEvent.KEYCODE_W:    // menu
//          case KeyEvent.KEYCODE_X:    // text view intercepts to cut
//          case KeyEvent.KEYCODE_Y:    // unused
//          case KeyEvent.KEYCODE_Z:    // unused
        }
        // it is a regular key and webview is not null
        return mUi.dispatchKey(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                if (mUi.isWebShowing()) {
//                    bookmarksOrHistoryPicker(ComboViews.History);
//                    return true;
//                }
//                break;
//        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isMenuOrCtrlKey(keyCode)) {
            mMenuIsDown = false;
            if (KeyEvent.KEYCODE_MENU == keyCode
                    && event.isTracking() && !event.isCanceled()) {
                return onMenuKey();
            }
        }
        if (!event.hasNoModifiers()) return false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.isTracking() && !event.isCanceled()) {
                    onBackKey();
                    return true;
                }
                break;
        }
        return false;
    }

    public boolean isMenuDown() {
        return mMenuIsDown;
    }

    @Override
    public boolean onSearchRequested() {
//        mUi.editUrl(false, true);
        return true;
    }

    @Override
    public boolean shouldCaptureThumbnails() {
        return mUi.shouldCaptureThumbnails();
    }

    @Override
    public boolean supportsVoice() {
        PackageManager pm = mActivity.getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(
                "android.speech.action.RECOGNIZE_SPEECH"), 0);
        return activities.size() != 0;
    }

    @Override
    public void startVoiceRecognizer() {
        Intent voice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voice.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            mActivity.startActivityForResult(voice, VOICE_RESULT);
        } catch (ActivityNotFoundException e) {
//            showDownloadVoicePackage();
            ToastUtil.showShortToast(mActivity, R.string.setting_update_speech_recognition);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ActivityUtils.startActivity(getContext(), BrowserCaptureActivity.class);
//                ActivityUtils.startForResultActivity((Activity) getContext(), BrowserQRCodeActivity.class);
            } else {
                showPermissionDeniedDialog(R.string.browser_permission_camera);
            }
        } else if (requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE
                || requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_FIRST) {
            showPermissionDeniedDialog(R.string.browser_permission_strone);
        } else if (requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_WALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new WallpaperHandler(getActivity(), mCurrentExtra).onMenuItemClick();
            } else {
                showPermissionDeniedDialog(R.string.browser_permission_setwall);
            }
        }else if(requestCode ==  SettingsPreferenecesFragment.NOTIFY_PERMISSION_HOME_REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BrowserSettings.getInstance().showNotification();
                BrowserSettings.getInstance().setNotificationToolShow();
                SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_NOTIFY_BAR_SHOW,true);
            }
        }
    }


    private void showPermissionDeniedDialog(int message) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用程序的权限设置页面
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                        intent.setData(uri);
                        mActivity.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void startScan() {
        if (requestCamerPermission()) {
            ActivityUtils.startQRCodeScanner((Activity) getContext());
        }
    }

    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.CAMERA};

    private static final int PERMISSION_CAMERA = 207;

    private boolean requestCamerPermission() {
        if (Build.VERSION.SDK_INT < BuildUtil.VERSION_CODES.M) {
            return true;
        }
        int hasPermission = mActivity.checkSelfPermission(android.Manifest.permission.CAMERA);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, PERMISSIONS_STORAGE, PERMISSION_CAMERA);
//            mActivity.requestPermissions(new String[]{android.Manifest.permission.CAMERA},
//                    PERMISSION_CAMERA);
            return false;
        }
        return true;
    }

    private void showDownloadVoicePackage() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                .parse("market://details?id=" + VOICE_PACKGE));
        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(GOOGLE_PLAY_DETAILS_LINK + VOICE_PACKGE));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            mActivity.startActivity(intent);
        }
    }

    private boolean isCollectedBookmark(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        ContentResolver contentResolver = getActivity().getContentResolver();
        int mCount = 0;
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(BrowserContract.Bookmarks.CONTENT_URI,
                    null, BrowserContract.Bookmarks.URL + "=?", new String[]{url}, null);
            mCount = cursor.getCount();
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "lookupBookmark ", e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return mCount > 0;
    }

    public boolean canAddBookmark() {
        String url = null;
        PreBrowserWebView preBrowserWebView = getCurrentWebView();
        if (preBrowserWebView != null && preBrowserWebView.isWebViewCreate()) {
            BaseWebView webView = preBrowserWebView.getBaseWebView();
            if (webView != null) {
                url = webView.getUrl();
            } else {
                url = null;
            }
        }
        return isCollectedBookmark(url);
    }


    private boolean isSavedOffLine(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        ContentResolver contentResolver = getActivity().getContentResolver();
        int mCount = 0;
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(SnapshotProvider.Snapshots.CONTENT_URI,
                    null, SnapshotProvider.Snapshots.URL + "='" + url + "'", null, null);
            if (cursor != null) {
                mCount = cursor.getCount();
            }
        } catch (IllegalStateException e) {
            //not need deal with this exception
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return mCount > 0;
    }

    public boolean canSavedOffLine() {
        String url = null;
        BaseWebView webView = getCurrentBrowserWebView();
        if (webView != null) {
            url = webView.getUrl();
        }
        if (url != null && url.startsWith("file://")) {
            return true;
        }
        return isSavedOffLine(url);
    }

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    @Override
    public void showCommonMenu(BottomSettingplaneDialogFragment menu) {
        if(mUi != null){
            mUi.isCanScrollWebPage(false);
        }
        if (menu == null) {
            return;
        }
        mBottomSetting = menu;
        if (mFragmentManager == null) {
            mFragmentManager = ((FragmentActivity) getActivity()).getSupportFragmentManager();
        }
        mFragmentTransaction = mFragmentManager.beginTransaction();
        if (!menu.isAdded()) {
            mFragmentTransaction.add(menu, "fragment_bottom_dialog");
        }
        mFragmentTransaction.show(menu);
        finishUpdate();
    }

    private BaseDialogFragment mWebsiteNavigationFragment;

    @Override
    public void showNavigationPage(BaseDialogFragment menu) {
        if (menu == null) {
            return;
        }
        mWebsiteNavigationFragment = menu;
        if (mFragmentManager == null) {
            mFragmentManager = ((FragmentActivity) getActivity()).getSupportFragmentManager();
        }
        mFragmentTransaction = mFragmentManager.beginTransaction();
        if (!menu.isAdded()) {
            mFragmentTransaction.add(menu, "fragment_website_navigation");
        }
        mFragmentTransaction.show(menu);
        finishUpdate();
    }

    private void dettachFragment(DialogFragment fragment) {
        dettachFragment(fragment, 0);
    }

    private void dettachFragment(DialogFragment fragment, int delayTime) {
        if (fragment != null && mHandler != null) {
            if (delayTime == 0) {
                fragment.dismiss();
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fragment != null) {
                            fragment.dismiss();
                        }
                    }
                }, delayTime);
            }
        }
    }

    public void finishUpdate() {
        try {
            if (this.mFragmentTransaction != null) {
                this.mFragmentTransaction.commitAllowingStateLoss();
                this.mFragmentTransaction = null;
                this.mFragmentManager.executePendingTransactions();
            }
        } catch (Exception e) {
            //Activity has been destroyed
        }
    }

    @Override
    public void menuPopuOnItemClick(View view) {
        int id = view.getId();
        OnClickMenuItem(id);
    }

    @Override
    public void onToolBarItemClick(int viewId) {
        OnClickMenuItem(viewId);
    }

    @Override
    public void addBookmark(String title) {
        Bitmap mTouchIconUrl;
        PreBrowserWebView preBrowserWebView = getCurrentTopWebView();
        if (preBrowserWebView == null) return;
        BaseWebView w = preBrowserWebView.getBaseWebView();
        if (w == null) return;
        String unfilteredUrl;
        unfilteredUrl = UrlUtils.fixUrl(w.getUrl());
        mTouchIconUrl = w.getFavicon();
        w.getFavicon();
        String url = unfilteredUrl.trim();
        try {
            // We allow bookmarks with a javascript: scheme, but these will in most cases
            // fail URI parsing, so don't try it if that's the kind of bookmark we have.

            if (!url.toLowerCase().startsWith("javascript:")) {
                String scheme = UrlUtils.getSchemePrefix(url);
                if (!Bookmarks.urlHasAcceptableScheme(url)) {
                    // If the scheme was non-null, let the user know that we
                    // can't save their bookmark. If it was null, we'll assume
                    // they meant http when we parse it in the WebAddress class.
                    if (scheme != null) {
                        ToastUtil.showShortToast(mActivity, R.string.bookmark_cannot_save_url);
                        return;
                    }
                    WebAddress address;
                    try {
                        address = new WebAddress(unfilteredUrl);
                    } catch (Exception e) {
                        throw new URISyntaxException("", "");

                    }
                    if (address.getHost().length() == 0) {
                        throw new URISyntaxException("", "");
                    }
                    url = address.toString();
                }
            }
            if (title == null || title.isEmpty()) {
                title = w.getTitle();
            }

            if (title == null || title.isEmpty()) {
                title = getContext().getResources().getString(R.string.snap_shot_no_title);
            }
            Bookmarks.addBookmark(mActivity, false, url,
                    title, mTouchIconUrl, -1);
            ToastUtil.showShortToast(mActivity, R.string.bookmark_saved);
        } catch (URISyntaxException e) {
            ToastUtil.showShortToast(mActivity, R.string.bookmark_url_not_valid);
        } catch (IllegalStateException e) {
            ToastUtil.showShortToast(mActivity, R.string.bookmark_not_saved);
        }
    }

    @Override
    public void showDownloadAnimation() {
        if (this.getUi() instanceof BaseUi) {
            //todo 显示下载提示
        }
    }

    @Override
    public int toolBarHeight() {
        if (this.getUi() instanceof BaseUi) {
            return ((BaseUi) this.getUi()).getToolbar().getToolBarCenterViewHeight();
        }
        return 0;
    }

    private void removeBookmark(final String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }

        try {
            getActivity().getContentResolver().delete(BrowserContract.Bookmarks.CONTENT_URI,
                    BrowserContract.Bookmarks.TITLE + "=?", new String[]{title});

        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "lookupBookmark ", e);
        }
    }

    public boolean canGoBack() {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            return false;
        }
        if (current.isNativePage()) {
            return false;
        } else if (mSettings.getHomePage().equals(Constants.NATIVE_PAGE_URL) || current.isPrivateBrowsingEnabled()) {
            return true;
        }
        return current.canGoBack();
    }


    public boolean canGoForward() {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            return false;
        }
        if (current.isNativePage()) {
            PreBrowserWebView webView = current.getWebView();
            if (webView == null || !webView.isWebViewCreate()) {
                return false;
            } else if (!TextUtils.isEmpty(webView.getBaseWebView().getUrl())) {
                return true;
            }
        }
        return current.canGoForward();
    }

    @Override
    public void setBlockEvents(boolean block) {
        mBlockEvents = block;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return mBlockEvents;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return mBlockEvents;
    }

    private void injectJsObject(Tab tab, final boolean force) {
        final JsInterfaceInject jsObject = new JsInterfaceInject();
        final ImageJavascriptInterface imageObject = new ImageJavascriptInterface(this);
        PreBrowserWebView browserWebView = tab.getWebView();
        if (browserWebView != null) {
            browserWebView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(BaseWebView baseWebView) {
                    baseWebView.addJavascriptInterface(jsObject, "__byfrost__");
                    baseWebView.addJavascriptInterface(imageObject, "imagelistener");
                    mUi.loadJsObject(jsObject, force);
                }
            });
        }
    }

    public void showExitDialog() {
        if (mExitDialog != null && mExitDialog.isShowing()) {
            return;
        }

        String[] entries = mActivity.getResources().getStringArray(R.array.exit_dialog_choices);
        final String[] entryValues = mActivity.getResources().getStringArray(R.array.exit_dialog_value);
        mExitDialog = new BrowserMultiselectDialog(mActivity, R.layout.browser_exit_dialog_list_item, entries) {
            @Override
            public void dialogDismiss(Map<Integer, String> selected) {
                boolean mClearHistoryCache = false;
                boolean mConfirmOnExit = false;
                for (Map.Entry<Integer, String> entry : selected.entrySet()) {
                    switch (entryValues[entry.getKey()]) {
                        case PreferenceKeys.PREF_CLEAR_HISTORY_CACHE_EXITING:
                            mCrashRecoveryHandler.clearState();
                            SharedPreferencesUtils.put(PreferenceKeys.DEFAULT_CACHE_DATA_COPIED, true);
                            mSettings.clearCache();
                            mSettings.clearDatabases();
                            mSettings.clearHistory();
                            mClearHistoryCache = true;
                            break;
                        case PreferenceKeys.PREF_CONFIRM_ON_EXIT:
                            mConfirmOnExit = true;
                            break;
                    }
                }
                if (mConfirmOnExit) {
                    mSettings.setClearHistoryAndCacheExiting(mClearHistoryCache);
                    if (mSettings.setConfirmOnExit(false)) {
                        exit();
                    }
                } else {
                    exit();
                }

            }
        };
        mExitDialog.setBrowserTitle(mActivity.getText(R.string.exit_dialog_title).toString())
                .setBrowserNegativeButton(mActivity.getText(R.string.cancel).toString())
                .setBrowserPositiveButton(mActivity.getText(R.string.exit).toString());
        mExitDialog.show();
    }

    private void onToolBarHomeKey() {
        dettachFragment(mBottomSetting);
        Tab currTab = mTabControl.getCurrentTab();
        if (mUi instanceof BrowserPhoneUi) {
            BrowserPhoneUi ui = (BrowserPhoneUi) mUi;
            if (currTab.isNativePage()) {
                ui.togglePageSwitch();
            } else {
                loadUrl(currTab, mSettings.getHomePage());
            }
        }
    }

    public void panelSwitchHome(Tab current) {
        if (mUi instanceof BrowserPhoneUi) {
            ((BrowserPhoneUi) mUi).panelSwitchHome(mTabControl.getCurrentPosition(), false);
        }
    }

    private void againQuit() {

        if (mSettings.getConfirmOnExit()) {
            showExitDialog();
            return;
        }

        long time = System.currentTimeMillis();
        if (time - mOneTime > TIMES) {
            mOneTime = time;
            ToastUtil.showLongToast(getContext(), R.string.click_again_quit);

        } else {
            if (mSettings.getClearHistoryAndCacheExiting()) {
                mCrashRecoveryHandler.clearState();
                SharedPreferencesUtils.put(PreferenceKeys.DEFAULT_CACHE_DATA_COPIED, true);
                mSettings.clearCache();
                mSettings.clearDatabases();
                mSettings.clearHistory();
            }
            exit();
        }
    }

    public void exit() {
        SharedPreferencesUtils.put("browser_last_url", "");
        mActivity.finish();
//        System.exit(0);
    }

    private void setMoveTaskToBack() {
        mActivity.moveTaskToBack(false);
    }

    @Override
    public void registerFullscreenListener(FullscreenListener listener) {
        if (mFullscreenUiList == null) {
            mFullscreenUiList = new ArrayList<>();
        }
        if (!mFullscreenUiList.contains(listener)) {
            mFullscreenUiList.add(listener);
        }
    }

    @Override
    public void setFullscreen(boolean isEnabled) {
        if (mFullscreenUiList == null || mFullscreenUiList.size() <= 0) {
            return;
        }
        for (FullscreenListener listener : mFullscreenUiList) {
            listener.onFullscreenChange(isEnabled);
        }
    }

    public void onNetworkToggle(boolean up) {
        mUi.onNetworkToggle(up);
    }

    @Override
    public void onPageLoadFinished(Tab tab) {
        if (mUi != null) {
            ((BaseUi) mUi).onPageLoadFinished(tab);
        }
        clearAd();
    }

    @Override
    public void onPageLoadStarted(Tab tab) {
        if (mUi != null) {
            ((BaseUi) mUi).onPageLoadStarted(tab);
        }
    }

    @Override
    public void onPageLoadStopped(Tab tab) {
        if (mUi != null) {
            ((BaseUi) mUi).onPageLoadStopped(tab);
        }
        clearAd();
    }

    public void onInputKeyChanged(int height) {
        if (mUi != null) {
            ((BaseUi) mUi).changeWebViewHeight(height);
        }
    }

    private void showRecoveryTabsToast(final int text, final Bundle icicle, final long currentTabId,
                                       final boolean restoreIncognitoTabs) {
        Handler handler = new Handler(mActivity.getMainLooper());
        mToastPopView = new ToastPopView(mActivity);

        int height = mActivity.getResources().getDimensionPixelSize(R.dimen.bottom_toolbar_height);

        final int barHeight = height;
        mUi.getMainContent().post(new Runnable() {
            @Override
            public void run() {
                if (mToastPopView == null || mActivity == null || mActivity.isFinishing()) return;
                mToastPopView.setText(text).setButtonText(R.string.abnormal_recover_ok).setTextOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mTabControl.restoreState(icicle, currentTabId, restoreIncognitoTabs,
                                mUi.needsRestoreAllTabs());
                        mToastPopView.dismiss();
                        mToastPopView = null;

                        setActiveTab(mTabControl.getCurrentTab());
                        if (getCurrentTab() != null && !getCurrentTab().isNativePage()) {
                            ((BrowserPhoneUi) mUi).panelSwitch(ComboHomeViews.VIEW_WEBVIEW, mTabControl.getCurrentPosition()
                                    , true);
                        }
                        ToastUtil.showShortToast(mActivity, R.string.abnormal_recover_recovered);
                    }
                }).setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        ArrayList<Long> restoredTabs = new ArrayList<Long>(mTabControl.getTabCount());
                        for (Tab t : mTabControl.getTabs()) {
                            restoredTabs.add(t.getId());
                        }
                        BackgroundHandler.execute(new PruneThumbnails(mActivity, restoredTabs));
                    }
                });

                if (mActivity != null && !mActivity.isFinishing()) {
                    try {
                        mToastPopView.show(mUi.getMainContent(), barHeight);
                    } catch (Exception e) {
                    }
                }
            }
        });


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivity != null && !mActivity.isFinishing() && mToastPopView != null) {
                    mToastPopView.dismiss();
                    mToastPopView = null;
                }
            }
        }, 5000);
    }

}

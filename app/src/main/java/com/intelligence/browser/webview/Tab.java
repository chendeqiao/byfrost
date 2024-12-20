/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;

import androidx.core.content.ContextCompat;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.R;
import com.intelligence.browser.base.BrowserDownloadListener;
import com.intelligence.browser.base.WebBackForwardListClient;
import com.intelligence.browser.base.WebViewController;
import com.intelligence.browser.database.provider.SnapshotProvider;
import com.intelligence.browser.database.provider.SnapshotProvider.Snapshots;
import com.intelligence.browser.downloads.DownloadTouchIcon;
import com.intelligence.browser.ui.home.HomeProvider;
import com.intelligence.browser.ui.home.ImmersiveController;
import com.intelligence.browser.ui.home.WebViewStatusChange;
import com.intelligence.browser.ui.view.BrowserErrorConsoleView;
import com.intelligence.browser.ui.view.GeolocationPermissionsPrompt;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.utils.WebIconUtils;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.browser.ui.widget.BrowserNoTitleDialog;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.commonlib.tools.BuildUtil;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.UrlUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Pattern;

public class Tab implements PictureListener {

    // Log Tag
    private static final String LOGTAG = "Tab";
    // Special case the logtag for messages for the Console to make it easier to
    // filter them and match the logtag used for these messages in older versions
    // of the browser.
    private static final String CONSOLE_LOGTAG = "browser";

    private static final int MSG_CAPTURE = 42;
    private static final int CAPTURE_DELAY = 100;
    private static final int INITIAL_PROGRESS = 5;


    private static Bitmap sDefaultFavicon;

    private static Paint sAlphaPaint = new Paint();

    static {
        sAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        sAlphaPaint.setColor(Color.TRANSPARENT);
    }

    public WebView getCurrentBrowserWebView() {
        PreBrowserWebView preBrowserWebView = getWebView();
        return preBrowserWebView == null ? null : preBrowserWebView.getBaseWebView();
    }

    public enum SecurityState {
        // The page's main resource does not use SSL. Note that we use this
        // state irrespective of the SSL authentication state of sub-resources.
        SECURITY_STATE_NOT_SECURE,
        // The page's main resource uses SSL and the certificate is good. The
        // same is true of all sub-resources.
        SECURITY_STATE_SECURE,
        // The page's main resource uses SSL and the certificate is good, but
        // some sub-resources either do not use SSL or have problems with their
        // certificates.
        SECURITY_STATE_MIXED,
        // The page's main resource uses SSL but there is a problem with its
        // certificate.
        SECURITY_STATE_BAD_CERTIFICATE,
    }

    Context mContext;
    protected WebViewController mWebViewController;

    // The tab ID
    private long mId = -1;

    // The Geolocation permissions prompt
    private GeolocationPermissionsPrompt mGeolocationPermissionsPrompt;
    // Main WebView wrapper
    private View mContainer;
    // Main WebView
    private PreBrowserWebView mMainView;
    // Subwindow container
    private View mSubViewContainer;
    // Subwindow WebView
    private PreBrowserWebView mSubView;

    // Saved bundle for when we are running low on memory. It contains the
    // information needed to restore the WebView if the user goes back to the
    // tab.
    private Bundle mSavedState;
    // Parent Tab. This is the Tab that created this Tab, or null if the Tab was
    // created by the UI
    private Tab mParent;
    // Tab that constructed by this Tab. This is used when this Tab is
    // destroyed, it clears all mParentTab values in the children.
    private Vector<Tab> mChildren;
    // If true, the tab is in the foreground of the current activity.
    private boolean mInForeground;
    // If true, the tab is in page loading state (after onPageStarted,
    // before onPageFinsihed)
    private boolean mInPageLoad;
    private boolean mIsShowErrorPage;
    private boolean mDisableOverrideUrlLoading;
    // The last reported progress of the current page
    private int mPageLoadProgress;
    // The time the load started, used to find load page time
    private long mLoadStartTime;
    // Application identifier used to find tabs that another application wants
    // to reuse.
    private String mAppId;
    // flag to indicate if tab should be closed on back
    private boolean mCloseOnBack;
    // Keep the original url around to avoid killing the old WebView if the url
    // has not changed.
    // Error console for the tab
    private BrowserErrorConsoleView mErrorConsole;
    // The listener that gets invoked when a download is started from the
    // mMainView
    private final BrowserDownloadListener mDownloadListener;
    // Listener used to know when we move forward or back in the history list.
    private final WebBackForwardListClient mWebBackForwardListClient;
    private BrowserDataController mBrowserDataController;

    // AsyncTask for downloading touch icons
    public DownloadTouchIcon mTouchIconLoader;

    // displays SSL error(s) dialog to the user.
    BrowserDialog mSslErrorDialog;

    private BrowserSettings mSettings;
    private int mCaptureWidth;
    private int mCaptureHeight;
    private Bitmap mCapture;
    private boolean mCaptureSuccess = false;
    private Handler mHandler;
    private boolean mUpdateThumbnail;

    // 用于自定义Tab的标记，只有该标记为true时才是自定义的Tab
    private boolean mIsNativePage = false;

    private WebViewStatusChange mWebViewStatusChange;
    private View mHomePage;
    private int mPagePosition = 0;
    private boolean mSetThemeColorSuccess;

    /**
     * See {@link #clearBackStackWhenItemAdded}.
     */
    private Pattern mClearHistoryUrlPattern;

    public static synchronized Bitmap getDefaultFavicon(Context context) {
        if (sDefaultFavicon == null) {
            sDefaultFavicon = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.browser_app_small_icon);
        }
        return sDefaultFavicon;
    }

    // All the state needed for a page
    protected static class PageState {
        String mUrl;
        String mOriginalUrl;
        String mTitle;
        SecurityState mSecurityState;
        // This is non-null only when mSecurityState is SECURITY_STATE_BAD_CERTIFICATE.
        SslError mSslCertificateError;
        Bitmap mFavicon;
        boolean mIsBookmarkedSite;
        public boolean mIncognito;

        PageState(Context c, boolean incognito) {
            mIncognito = incognito;
            mOriginalUrl = mUrl = "";
            mTitle = c.getString(R.string.home_page);
            mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
        }

        PageState(Context c, boolean incognito, String url, Bitmap favicon) {
            mIncognito = incognito;
            mOriginalUrl = mUrl = url;
            if (URLUtil.isHttpsUrl(url)) {
                mSecurityState = SecurityState.SECURITY_STATE_SECURE;
            } else {
                mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
            }
            mFavicon = favicon;
        }

    }

    // The current/loading page's state
    protected PageState mCurrentState;

    // Used for saving and restoring each Tab
    static final String ID = "ID";
    static final String CURRURL = "currentUrl";
    static final String CURRTITLE = "currentTitle";
    static final String PARENTTAB = "parentTab";
    static final String APPID = "appid";
    static final String INCOGNITO = "privateBrowsingEnabled";
    static final String ISNATIVEHOMEPAGE = "isNativePage";
    static final String HOMEPAGE_STATUS = "homePageStatus";
    static final String PAGEPOSITION = "pagePosition";
    static final String USERAGENT = "useragent";
    static final String CLOSEFLAG = "closeOnBack";

    // Container class for the next error dialog that needs to be displayed
    private class ErrorDialog {
        public final int mTitle;
        public final String mDescription;
        public final int mError;

        ErrorDialog(int title, String desc, int error) {
            mTitle = title;
            mDescription = desc;
            mError = error;
        }
    }

    private void processNextError() {
        if (mQueuedErrors == null) {
            return;
        }
        // The first one is currently displayed so just remove it.
        mQueuedErrors.removeFirst();
        if (mQueuedErrors.size() == 0) {
            mQueuedErrors = null;
            return;
        }
        showError(mQueuedErrors.getFirst());
    }

    private DialogInterface.OnDismissListener mDialogListener =
            new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface d) {
                    processNextError();
                }
            };
    private LinkedList<ErrorDialog> mQueuedErrors;

    private void queueError(int err, String desc) {
        if (mQueuedErrors == null) {
            mQueuedErrors = new LinkedList<ErrorDialog>();
        }
        for (ErrorDialog d : mQueuedErrors) {
            if (d.mError == err) {
                // Already saw a similar error, ignore the new one.
                return;
            }
        }
        ErrorDialog errDialog = new ErrorDialog(
                err == WebViewClient.ERROR_FILE_NOT_FOUND ?
                        R.string.browserFrameFileErrorLabel :
                        R.string.browserFrameNetworkErrorLabel,
                desc, err);
        mQueuedErrors.addLast(errDialog);

        // Show the dialog now if the queue was empty and it is in foreground
        if (mQueuedErrors.size() == 1 && mInForeground) {
            showError(errDialog);
        }
    }

    private void showError(ErrorDialog errDialog) {
        if (mInForeground) {
            BrowserNoTitleDialog dialog = new BrowserNoTitleDialog(mContext, errDialog.mDescription);
            dialog.setOnDismissListener(mDialogListener);
            dialog.setBrowserMessage(errDialog.mTitle)
                    .setBrowserPositiveButton(R.string.ok)
                    .show();
        }
    }

    // -------------------------------------------------------------------------
    // WebViewClient implementation for the main WebView
    // -------------------------------------------------------------------------

    private final WebViewClient mWebViewClient = new WebViewClient() {
        private Message mDontResend;
        private Message mResend;
        private int mImgAdCount;
        private int mJsAdCount;
        private int mPopupAdCount;
        private String currentUrl;
        private boolean isCount = false;

        private boolean providersDiffer(String url, String otherUrl) {
            Uri uri1 = Uri.parse(url);
            Uri uri2 = Uri.parse(otherUrl);
            return !uri1.getEncodedAuthority().equals(uri2.getEncodedAuthority());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (mMainView != null) {
                mMainView.onPageStarted();
            }
            if (mSubView != null) {
                mSubView.onPageStarted();
            }
            mSetThemeColorSuccess = false;
            disableJsIfUrlEncodedFailed(view, url);
            if (mInForeground) {
                ImmersiveController.getInstance().setWebViewStatusBarColor(ContextCompat.getColor(mContext, R.color.status_bar_webview));
            }
            mImgAdCount = 0;
            mJsAdCount = 0;
            mPopupAdCount = 0;
            if (TextUtils.isEmpty(currentUrl) || !currentUrl.equals(url)) {
                currentUrl = url;
                isCount = true;
            } else {
                isCount = false;
            }
            mInPageLoad = true;
            mIsShowErrorPage = false;
            mUpdateThumbnail = true;
            mPageLoadProgress = INITIAL_PROGRESS;
            mCurrentState = new PageState(mContext,
                    view.isPrivateBrowsingEnabled(), url, favicon);
            mLoadStartTime = SystemClock.uptimeMillis();


            if (isPrivateBrowsingEnabled()) {
                // Ignore all the cookies while an incognito tab has activity
                CookieManager.getInstance().setAcceptCookie(BrowserSettings.getInstance().getEnableCookiesIncognito());
            }
            // If we start a touch icon load and then load a new page, we don't
            // want to cancel the current touch icon loader. But, we do want to
            // create a new one when the touch icon url is known.
            if (mTouchIconLoader != null) {
                mTouchIconLoader.mTab = null;
                mTouchIconLoader = null;
            }

            // reset the error console
            if (mErrorConsole != null) {
                mErrorConsole.clearErrorMessages();
                if (mWebViewController.shouldShowErrorConsole()) {
                    mErrorConsole.showConsole(BrowserErrorConsoleView.SHOW_NONE);
                }
            }

            // finally update the UI in the activity if it is in the foreground
            mWebViewController.onPageStarted(Tab.this, view, favicon);
            if (mInForeground) {
                mWebViewController.onPageLoadStarted(Tab.this);
            }
            updateBookmarkedStatus();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mIsShowErrorPage = false;
            SharedPreferencesUtils.put(BrowserPhoneUi.BROWSER_LAST_TIME_URL,url);
            if(mMainView != null){
                mMainView.onPageFinished();
            }
            if (mSubView != null){
                mSubView.onPageFinished();
            }
            view.getSettings().setJavaScriptEnabled(true);
            view.getSettings().setDomStorageEnabled(true);
            if (isCount) {
                mSettings.setImgAdBlockCount(mSettings.getImgAdBlockCount() + mImgAdCount);
                mSettings.setJsAdBlockCount(mSettings.getJsAdBlockCount() + mJsAdCount);
                mSettings.setPopupAdBlockCount(mSettings.getPopupAdBlockCount() + mPopupAdCount);
            }
            isCount = false;
            mDisableOverrideUrlLoading = false;
            if (!isPrivateBrowsingEnabled()) {
            } else {
                // Ignored all the cookies while an incognito tab had activity,
                // restore default after completion
                CookieManager.getInstance().setAcceptCookie(mSettings.acceptCookies());
            }
            syncCurrentState(view, url);
            setWebViewThemeColor(view);
            mWebViewController.onPageFinished(Tab.this);
            mWebViewController.onPageLoadFinished(Tab.this);
//            addImageClickListener(view);
        }

        // return true if want to hijack the url to let another app to handle it
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mIsShowErrorPage = false;
            if (!mDisableOverrideUrlLoading && mInForeground) {
                return mWebViewController.shouldOverrideUrlLoading(Tab.this,
                        view, url);
            } else {
                return false;
            }
        }

        /**
         * Updates the security state. This method is called when we discover
         * another resource to be loaded for this page (for example,
         * javascript). While we update the security state, we do not update
         * the lock icon until we are done loading, as it is slightly more
         * secure this way.
         */
        @Override
        public void onLoadResource(WebView view, String url) {
            if (url != null && url.length() > 0) {
                // It is only if the page claims to be secure that we may have
                // to update the security state:
                if (mCurrentState.mSecurityState == SecurityState.SECURITY_STATE_SECURE) {
                    // If NOT a 'safe' url, change the state to mixed content!
                    if (!(URLUtil.isHttpsUrl(url) || URLUtil.isDataUrl(url)
                            || URLUtil.isAboutUrl(url))) {
                        mCurrentState.mSecurityState = SecurityState.SECURITY_STATE_MIXED;
                    }
                }
            }
        }

        /**
         * Show a dialog informing the user of the network error reported by
         * WebCore if it is in the foreground.
         */
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            if(errorCode != WebViewClient.ERROR_HOST_LOOKUP ||
                    errorCode != WebViewClient.ERROR_CONNECT ||  errorCode != WebViewClient.ERROR_TIMEOUT){
                if(mMainView != null){
                    mMainView.onReceivedError();
                }
                if (mSubView != null){
                    mSubView.onReceivedError();
                }
                mIsShowErrorPage = true;
                return;
            }
            if (errorCode != WebViewClient.ERROR_HOST_LOOKUP &&
                    errorCode != WebViewClient.ERROR_CONNECT &&
                    errorCode != WebViewClient.ERROR_BAD_URL &&
                    errorCode != WebViewClient.ERROR_UNSUPPORTED_SCHEME &&
                    errorCode != WebViewClient.ERROR_FILE &&
                    errorCode != WebViewClient.ERROR_TIMEOUT) {
                queueError(errorCode, description);
                mIsShowErrorPage = true;
                // Don't log URLs when in private browsing mode
                if (!isPrivateBrowsingEnabled()) {
                }
            }
        }

        /**
         * Check with the user if it is ok to resend POST data as the page they
         * are trying to navigate to is the result of a POST.
         */
        @Override
        public void onFormResubmission(WebView view, final Message dontResend,
                                       final Message resend) {
            if (!mInForeground) {
                dontResend.sendToTarget();
                return;
            }
            if (mDontResend != null) {
                dontResend.sendToTarget();
                return;
            }
            mDontResend = dontResend;
            mResend = resend;

            new BrowserNoTitleDialog(mContext) {
                @Override
                public void onPositiveButtonClick() {
                    super.onPositiveButtonClick();
                    if (mResend != null) {
                        mResend.sendToTarget();
                        mResend = null;
                        mDontResend = null;
                    }
                }

                @Override
                public void onNegativeButtonClick() {
                    super.onNegativeButtonClick();
                    if (mDontResend != null) {
                        mDontResend.sendToTarget();
                        mResend = null;
                        mDontResend = null;
                    }
                }

                @Override
                public void cancel() {
                    super.cancel();
                    if (mDontResend != null) {
                        mDontResend.sendToTarget();
                        mResend = null;
                        mDontResend = null;
                    }
                }
            }.setBrowserMessage(R.string.browserFrameFormResubmitMessage)
                    .setBrowserPositiveButton(R.string.ok)
                    .setBrowserNegativeButton(R.string.cancel)
                    .show();
        }

        /**
         * Insert the url into the visited history database.
         * @param url The url to be inserted.
         * @param isReload True if this url is being reloaded.
         * FIXME: Not sure what to do when reloading the page.
         */
        @Override
        public void doUpdateVisitedHistory(WebView view, String url,
                                           boolean isReload) {
            mWebViewController.doUpdateVisitedHistory(Tab.this, isReload);
        }

        /**
         * Displays SSL error(s) dialog to the user.
         */
        @Override
        public void onReceivedSslError(final WebView view,
                                       final SslErrorHandler handler, final SslError error) {
            if (!mInForeground) {
                handler.cancel();
                setSecurityState(SecurityState.SECURITY_STATE_NOT_SECURE);
                return;
            }
            if (mSslErrorDialog != null && mSslErrorDialog.isShowing()) {
                return;
            }
            if (mSettings.showSecurityWarnings()) {
                if (mSslErrorDialog == null) {
                    mSslErrorDialog = new BrowserDialog(mContext) {
                        @Override
                        public void onPositiveButtonClick() {
                            super.onPositiveButtonClick();
                            handler.proceed();
                            handleProceededAfterSslError(error);
                        }

                        @Override
                        public void onNegativeButtonClick() {
                            super.onNegativeButtonClick();
                            cancel();
                        }

                        @Override
                        public void onNeutralButtonClick() {
                            super.onNeutralButtonClick();
                            mWebViewController.showSslCertificateOnError(
                                    view, handler, error);
                        }
                    };

                    mSslErrorDialog.setBrowserTitle(R.string.security_warning)
                            .setBrowserMessage(R.string.ssl_warnings_header)
                            .setBrowserPositiveButton(R.string.ssl_continue)
                            .setBrowserNeutralButton(R.string.view_certificate)
                            .setBrowserNegativeButton(R.string.go_back)
                            .setWidth(DisplayUtil.getScreenWidth(mContext) * 7 / 8);
                    mSslErrorDialog.setOnCancelListener(
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    handler.cancel();
                                    setSecurityState(SecurityState.SECURITY_STATE_NOT_SECURE);
                                    mWebViewController.onUserCanceledSsl(Tab.this);
                                }
                            });
                }
                mSslErrorDialog.show();

            } else {
                handler.proceed();
                handleProceededAfterSslError(error);
            }
        }

        /**
         * Handles an HTTP authentication request.
         *
         * @param handler The authentication handler
         * @param host The host
         * @param realm The realm
         */
        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              final HttpAuthHandler handler, final String host,
                                              final String realm) {
            mWebViewController.onReceivedHttpAuthRequest(Tab.this, view, handler, host, realm);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            return HomeProvider.shouldInterceptRequest(mContext, url);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            if (!mInForeground) {
                return false;
            }
            return mWebViewController.shouldOverrideKeyEvent(event);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            if (!mInForeground) {
                return;
            }
            if (!mWebViewController.onUnhandledKeyEvent(event)) {
                super.onUnhandledKeyEvent(view, event);
            }
        }
    };

    private void syncCurrentState(WebView view, String url) {
        // Sync state (in case of stop/timeout)
        if (view == null) {
            throw new RuntimeException("syncCurrentState时 WebView 为空");
        }
        mCurrentState.mUrl = view.getUrl();
        if (mCurrentState.mUrl == null) {
            mCurrentState.mUrl = "";
        }
        mCurrentState.mOriginalUrl = view.getOriginalUrl();
        mCurrentState.mTitle = view.getTitle();
        mCurrentState.mFavicon = view.getFavicon();
        if (!URLUtil.isHttpsUrl(mCurrentState.mUrl)) {
            // In case we stop when loading an HTTPS page from an HTTP page
            // but before a provisional load occurred
            mCurrentState.mSecurityState = SecurityState.SECURITY_STATE_NOT_SECURE;
            mCurrentState.mSslCertificateError = null;
        }
        mCurrentState.mIncognito = view.isPrivateBrowsingEnabled();
    }

    // -------------------------------------------------------------------------
    // WebChromeClient implementation for the main WebView
    // -------------------------------------------------------------------------

    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        // Helper method to create a new tab or sub window.
        private void createWindow(final boolean dialog, final Message msg) {
            WebView.WebViewTransport transport =
                    (WebView.WebViewTransport) msg.obj;
            if (dialog && mSubView != null) {
                createSubWindow();
                mWebViewController.attachSubWindow(Tab.this);
                transport.setWebView(mSubView.getBaseWebView());
            } else {
                final Tab newTab = mWebViewController.openTab(null,
                        Tab.this, true, true);
                transport.setWebView(newTab.getWebView().getBaseWebView());
            }
            msg.sendToTarget();
        }

        @Override
        public boolean onCreateWindow(WebView view, final boolean dialog,
                                      final boolean userGesture, final Message resultMsg) {
            // only allow new window or sub window for the foreground case
            if (!mInForeground) {
                return false;
            }
            // Short-circuit if we can't create any more tabs or sub windows.
            if (dialog && mSubView != null) {
                new BrowserDialog(mContext)
                        .setBrowserTitle(R.string.too_many_subwindows_dialog_title)
                        .setBrowserMessage(R.string.too_many_subwindows_dialog_message)
                        .setBrowserPositiveButton(R.string.ok)
                        .show();
                return false;
            } else if (!mWebViewController.getTabControl().canCreateNewTab()) {
                ToastUtil.showShortToast(mContext, R.string.too_many_windows_dialog_message);
                return false;
            }

            // Short-circuit if this was a user gesture.
            if (userGesture) {
                createWindow(dialog, resultMsg);
                return true;
            }

            BrowserDialog browserDialog = new BrowserDialog(mContext) {
                @Override
                public void onPositiveButtonClick() {
                    super.onPositiveButtonClick();
                    createWindow(dialog, resultMsg);
                }

                @Override
                public void onNegativeButtonClick() {
                    super.onNegativeButtonClick();
                    resultMsg.sendToTarget();
                }
            };
            browserDialog.setBrowserTitle(R.string.too_many_subwindows_dialog_title)
                    .setBrowserMessage(R.string.too_many_subwindows_dialog_message)
                    .setBrowserPositiveButton(R.string.allow)
                    .setBrowserNegativeButton(R.string.block)
                    .setCancelable(false);
            browserDialog.show();
            return true;
        }

        @Override
        public void onRequestFocus(WebView view) {
            if (!mInForeground) {
                mWebViewController.switchToTab(Tab.this);
            }
        }

        @Override
        public void onCloseWindow(WebView window) {
            if (mParent != null) {
                // JavaScript can only close popup window.
                if (mInForeground) {
                    mWebViewController.switchToTab(mParent);
                }
                mWebViewController.closeTab(Tab.this);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            mWebViewController.getTabControl().setActiveTab(Tab.this);
            return false;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   JsResult result) {
            mWebViewController.getTabControl().setActiveTab(Tab.this);
            return false;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, JsPromptResult result) {
            mWebViewController.getTabControl().setActiveTab(Tab.this);
            return false;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mPageLoadProgress = newProgress;
            if (newProgress == 100) {
                mInPageLoad = false;
            }
            mWebViewController.onProgressChanged(Tab.this);
            if (mUpdateThumbnail && newProgress == 100) {
                mUpdateThumbnail = false;
            }
        }

        @Override
        public void onReceivedTitle(WebView view, final String title) {
            if (mWebViewStatusChange != null) {
                mWebViewStatusChange.onReceivedTitle(view.getUrl(), view.getOriginalUrl(), view.getTitle());
            }
            mCurrentState.mTitle = title;
            mWebViewController.onReceivedTitle(Tab.this, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            mCurrentState.mFavicon = icon;
            mWebViewController.onFavicon(Tab.this, view, icon);
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url,
                                           boolean precomposed) {
            if (mWebViewStatusChange != null) {
                mWebViewStatusChange.onReceivedTouchIconUrl(url);
            }
            final ContentResolver cr = mContext.getContentResolver();
            // Let precomposed icons take precedence over non-composed
            // icons.
            if (precomposed && mTouchIconLoader != null) {
                mTouchIconLoader.cancel(false);
                mTouchIconLoader = null;
            }
            // Have only one async task at a time.
            if (mTouchIconLoader == null) {
                mTouchIconLoader = new DownloadTouchIcon(Tab.this, cr, view);
                mTouchIconLoader.execute(url);
            }
        }

        @Override
        public void onShowCustomView(View view,
                                     WebChromeClient.CustomViewCallback callback) {
            Activity activity = mWebViewController.getActivity();
            if (activity != null) {
                onShowCustomView(view, activity.getRequestedOrientation(), callback);
            }
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation,
                                     WebChromeClient.CustomViewCallback callback) {
            if (mInForeground) mWebViewController.showCustomView(Tab.this, view,
                    requestedOrientation, callback);
        }

        @Override
        public void onHideCustomView() {
            if (mInForeground) mWebViewController.hideCustomView();
        }

        /**
         * The origin has exceeded its database quota.
         * @param url the URL that exceeded the quota
         * @param databaseIdentifier the identifier of the database on which the
         *            transaction that caused the quota overflow was run
         * @param currentQuota the current quota for the origin.
         * @param estimatedSize the estimated size of the database.
         * @param totalUsedQuota is the sum of all origins' quota.
         * @param quotaUpdater The callback to run when a decision to allow or
         *            deny quota has been made. Don't forget to call this!
         */
        @Override
        public void onExceededDatabaseQuota(String url,
                                            String databaseIdentifier, long currentQuota, long estimatedSize,
                                            long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            mSettings.getWebStorageSizeManager()
                    .onExceededDatabaseQuota(url, databaseIdentifier,
                            currentQuota, estimatedSize, totalUsedQuota,
                            quotaUpdater);
        }

        /**
         * The Application Cache has exceeded its max size.
         * @param spaceNeeded is the amount of disk space that would be needed
         *            in order for the last appcache operation to succeed.
         * @param totalUsedQuota is the sum of all origins' quota.
         * @param quotaUpdater A callback to inform the WebCore thread that a
         *            new app cache size is available. This callback must always
         *            be executed at some point to ensure that the sleeping
         *            WebCore thread is woken up.
         */
//        @Override
//        public void onReachedMaxAppCacheSize(long spaceNeeded,
//                                             long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
//            mSettings.getWebStorageSizeManager()
//                    .onReachedMaxAppCacheSize(spaceNeeded, totalUsedQuota,
//                            quotaUpdater);
//        }

        /**
         * Instructs the browser to show a prompt to ask the user to set the
         * Geolocation permission state for the specified origin.
         * @param origin The origin for which Geolocation permissions are
         *     requested.
         * @param callback The callback to call once the user has set the
         *     Geolocation permission state.
         */
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            if (mInForeground) {
                getGeolocationPermissionsPrompt().show(origin, callback);
            }
        }

        /**
         * Instructs the browser to hide the Geolocation permissions prompt.
         */
        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (mInForeground && mGeolocationPermissionsPrompt != null) {
                mGeolocationPermissionsPrompt.hide();
            }
        }

/*        @Override
        public void onPermissionRequest(PermissionRequest request) {
            if (!mInForeground) return;
            getPermissionsPrompt().show(request);
        }

        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            if (mInForeground && mPermissionsPrompt != null) {
                mPermissionsPrompt.hide();
            }
        }*/

        /* Adds a JavaScript error message to the system log and if the JS
         * console is enabled in the about:debug options, to that console
         * also.
         * @param consoleMessage the message object.
         */
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (mInForeground) {
                // call getErrorConsole(true) so it will create one if needed
                BrowserErrorConsoleView errorConsole = getErrorConsole(true);
                errorConsole.addErrorMessage(consoleMessage);
                if (mWebViewController.shouldShowErrorConsole()
                        && errorConsole.getShowState() !=
                        BrowserErrorConsoleView.SHOW_MAXIMIZED) {
                    errorConsole.showConsole(BrowserErrorConsoleView.SHOW_MINIMIZED);
                }
            }

            // Don't log console messages in private browsing mode
            if (isPrivateBrowsingEnabled()) return true;

            switch (consoleMessage.messageLevel()) {
                case TIP:
                    break;
                case LOG:
                    break;
                case WARNING:
                    break;
                case ERROR:
                    break;
                case DEBUG:
                    break;
            }

            return true;
        }

        /**
         * Ask the browser for an icon to represent a <video> element.
         * This icon will be used if the Web page did not specify a poster attribute.
         * @return Bitmap The icon or null if no such icon is available.
         */
        @Override
        public Bitmap getDefaultVideoPoster() {
            if (mInForeground) {
                return mWebViewController.getDefaultVideoPoster();
            }
            return null;
        }

        /**
         * Ask the host application for a custom progress view to show while
         * a <video> is loading.
         * @return View The progress view.
         */
        @Override
        public View getVideoLoadingProgressView() {
            if (mInForeground) {
                return mWebViewController.getVideoLoadingProgressView();
            }
            return null;
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                         FileChooserParams params) {
            if (mInForeground) {
                mWebViewController.showFileChooser(callback, params);
                return true;
            } else {
                return false;
            }
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            if (mInForeground) {
                mWebViewController.openFileChooser(uploadMsg, acceptType, capture);
            } else {
                uploadMsg.onReceiveValue(null);
            }
        }

        /**
         * Deliver a list of already-visited URLs
         */
        @Override
        public void getVisitedHistory(final ValueCallback<String[]> callback) {
            if (isPrivateBrowsingEnabled()) {
                callback.onReceiveValue(new String[0]);
            } else {
                mWebViewController.getVisitedHistory(callback);
            }
        }

    };

    // -------------------------------------------------------------------------
    // WebViewClient implementation for the sub window
    // -------------------------------------------------------------------------

    // Subclass of WebViewClient used in subwindows to notify the main
    // WebViewClient of certain WebView activities.
    private static class SubWindowClient extends WebViewClient {
        // The main WebViewClient.
        private final WebViewClient mClient;
        private final WebViewController mController;

        SubWindowClient(WebViewClient client, WebViewController controller) {
            mClient = client;
            mController = controller;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // Unlike the others, do not call mClient's version, which would
            // change the progress bar.  However, we do want to remove the
            // find or select dialog.
            mController.endActionMode();
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url,
                                           boolean isReload) {
            mClient.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return mClient.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            mClient.onReceivedSslError(view, handler, error);
        }

/*        @Override
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            mClient.onReceivedClientCertRequest(view, request);
        }*/

        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                                              HttpAuthHandler handler, String host, String realm) {
            mClient.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend,
                                       Message resend) {
            mClient.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            mClient.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view,
                                              android.view.KeyEvent event) {
            return mClient.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view,
                                        android.view.KeyEvent event) {
            mClient.onUnhandledKeyEvent(view, event);
        }
    }

    // -------------------------------------------------------------------------
    // WebChromeClient implementation for the sub window
    // -------------------------------------------------------------------------

    private class SubWindowChromeClient extends WebChromeClient {
        // The main WebChromeClient.
        private final WebChromeClient mClient;

        SubWindowChromeClient(WebChromeClient client) {
            mClient = client;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mClient.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog,
                                      boolean userGesture, android.os.Message resultMsg) {
            return mClient.onCreateWindow(view, dialog, userGesture, resultMsg);
        }

        @Override
        public void onCloseWindow(WebView window) {
            mWebViewController.dismissSubWindow(Tab.this);
        }
    }

    // -------------------------------------------------------------------------

    // Construct a new tab
    public Tab(WebViewController wvcontroller, PreBrowserWebView w) {
        this(wvcontroller, w, null);
    }

    public Tab(WebViewController wvcontroller, Bundle state) {
        this(wvcontroller, null, state);
    }

    public Tab(WebViewController wvcontroller, PreBrowserWebView w, Bundle state) {
        mWebViewController = wvcontroller;
        mContext = mWebViewController.getContext();
        mSettings = BrowserSettings.getInstance();
        mBrowserDataController = BrowserDataController.getInstance(mContext);
        mCurrentState = new PageState(mContext, BrowserSettings.getInstance().noFooterBrowser());
        mInPageLoad = false;
        mIsShowErrorPage = false;
        mInForeground = false;

        mDownloadListener = new BrowserDownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype, String referer,
                                        long contentLength) {
                mWebViewController.onDownloadStart(Tab.this, url, userAgent, contentDisposition,
                        mimetype, referer, contentLength);
            }
        };
        mWebBackForwardListClient = new WebBackForwardListClient() {
            @Override
            public void onNewHistoryItem(WebHistoryItem item) {
                if (mClearHistoryUrlPattern != null) {
                    boolean match =
                            mClearHistoryUrlPattern.matcher(item.getOriginalUrl()).matches();
                    if (match) {
                        if (mMainView != null) {
                            WebView webView = mMainView.getBaseWebView();
                            if (webView != null) {
                                webView.clearHistory();
                            }
                        }
                    }
                    mClearHistoryUrlPattern = null;
                }
            }
        };
        mCaptureWidth = (DisplayUtil.getScreenWidth(mContext) < DisplayUtil.getScreenHeight(mContext) ? DisplayUtil.getScreenWidth(mContext) : DisplayUtil.getScreenHeight(mContext)) / 2;
        mCaptureHeight = (DisplayUtil.getScreenWidth(mContext) > DisplayUtil.getScreenHeight(mContext) ? DisplayUtil.getScreenWidth(mContext) : DisplayUtil.getScreenHeight(mContext)) / 2;
        updateShouldCaptureThumbnails();
        restoreState(state);
        if (getId() == -1) {
            mId = TabControl.getNextId();
        }
        setWebView(w);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message m) {
                switch (m.what) {
                    case MSG_CAPTURE:
                        capture();
                        break;
                }
            }
        };
    }

    public boolean shouldUpdateThumbnail() {
        return mUpdateThumbnail;
    }

    /**
     * This is used to get a new ID when the tab has been preloaded, before it is displayed and
     * added to TabControl. Preloaded tabs can be created before restoreInstanceState, leading
     * to overlapping IDs between the preloaded and restored tabs.
     */
    public void refreshIdAfterPreload() {
        mId = TabControl.getNextId();
    }

    public void updateShouldCaptureThumbnails() {
        if (mWebViewController.shouldCaptureThumbnails()) {
            synchronized (Tab.this) {
                if (mCapture == null) {
                    initCaptureBitmap(mCaptureWidth, mCaptureHeight);
                    if (mInForeground) {
                        postCapture();
                    }
                }
            }
        } else {
            synchronized (Tab.this) {
                mCapture = null;
                deleteThumbnail();
            }
        }
    }

    public void setController(WebViewController ctl) {
        mWebViewController = ctl;
        updateShouldCaptureThumbnails();
    }

    public long getId() {
        return mId;
    }

    void setWebView(PreBrowserWebView w) {
        setWebView(w, true);
    }

    /**
     * Sets the WebView for this tab, correctly removing the old WebView from
     * the container view.
     */
    void setWebView(PreBrowserWebView w, final boolean restore) {
        if (mMainView == w) {
            return;
        }

        // If the WebView is changing, the page will be reloaded, so any ongoing
        // Geolocation permission requests are void.
        if (mGeolocationPermissionsPrompt != null) {
            mGeolocationPermissionsPrompt.hide();
        }

        mWebViewController.onSetWebView(this, w);

        if (mMainView != null) {
            mMainView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(BaseWebView baseWebView) {
                    baseWebView.setPictureListener(null);
                }
            });
            if (w != null) {
                syncCurrentState(w.getBaseWebView(), null);
            } else {
                mCurrentState = new PageState(mContext, BrowserSettings.getInstance().noFooterBrowser());
            }
        }
        // set the new one
        mMainView = w;
        if (mMainView != null && null != mCurrentState) {
            mCurrentState.mIncognito = w.isPrivateBrowsingEnabled();
        }
        // attach the WebViewClient, WebChromeClient and DownloadListener
        if (mMainView != null) {
            mMainView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(BaseWebView baseWebView) {
                    baseWebView.setWebViewClient(mWebViewClient);
                    baseWebView.setWebChromeClient(mWebChromeClient);
                    // Attach DownloadManager so that downloads can start in an active
                    // or a non-active window. This can happen when going to a site that
                    // does a redirect after a period of time. The user could have
                    // switched to another tab while waiting for the download to start.
                    baseWebView.setDownloadListener(mDownloadListener);
                    TabControl tc = mWebViewController.getTabControl();
                    if (tc != null && tc.getOnThumbnailUpdatedListener() != null) {
                        baseWebView.setPictureListener(Tab.this);
                    }
                    if (mMainView != null && restore && (mSavedState != null)) {
                        mMainView.initBrowserWebViewIfNull();
                        restoreUserAgent();
                        WebBackForwardList restoredState
                                = baseWebView.restoreState(mSavedState);
                        if (restoredState == null || restoredState.getSize() == 0) {
                            if (!mIsNativePage) {
                            }
                        }
                        mSavedState = null;
                    }
                }
            });


        }
    }

    /**
     * Destroy the tab's main WebView and subWindow if any
     */
    public void destroy() {
        if (mMainView != null) {
            dismissSubWindow();
            // save the WebView to call destroy() after detach it from the tab
            if (mMainView != null && mMainView.isWebViewCreate()) {
                mMainView.destroy();
            }
            setWebView(null);
        }
        if (mWebViewController != null) {
            mWebViewController.onPageLoadStopped(Tab.this);
        }
    }

    void removeFromTree() {
        removeFromTree(true);
    }
    /**
     * Remove the tab from the parent
     */
    public void removeFromTree(boolean deleteThumbnail) {
        // detach the children
        if (mChildren != null) {
            for (Tab t : mChildren) {
                t.setParent(null);
            }
        }
        // remove itself from the parent list
        if (mParent != null) {
            mParent.mChildren.remove(this);
        }
        if (deleteThumbnail) {
            deleteThumbnail();
        }
    }

    /**
     * Create a new subwindow unless a subwindow already exists.
     *
     * @return True if a new subwindow was created. False if one already exists.
     */
    boolean createSubWindow() {
        if (mSubView == null) {
            mWebViewController.createSubWindow(this);
            mSubView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(final BaseWebView baseWebView) {
                    baseWebView.setWebViewClient(new SubWindowClient(mWebViewClient,
                            mWebViewController));
                    baseWebView.setWebChromeClient(new SubWindowChromeClient(
                            mWebChromeClient));
                    // Set a different DownloadListener for the mSubView, since it will
                    // just need to dismiss the mSubView, rather than close the Tab
                    baseWebView.setDownloadListener(new BrowserDownloadListener() {
                        public void onDownloadStart(String url, String userAgent,
                                                    String contentDisposition, String mimetype, String referer,
                                                    long contentLength) {
                            mWebViewController.onDownloadStart(Tab.this, url, userAgent,
                                    contentDisposition, mimetype, referer, contentLength);
                            if (baseWebView.copyBackForwardList().getSize() == 0) {
                                // This subwindow was opened for the sole purpose of
                                // downloading a file. Remove it.
                                mWebViewController.dismissSubWindow(Tab.this);
                            }
                        }
                    });
                    mSubView.setOnCreateContextMenuListener(mWebViewController.getActivity());
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Dismiss the subWindow for the tab.
     */
    void dismissSubWindow() {
        if (mSubView != null) {
            mWebViewController.endActionMode();
            mSubView.destroy();
            mSubView = null;
            mSubViewContainer = null;
        }
    }


    /**
     * Set the parent tab of this tab.
     */
    void setParent(Tab parent) {
        if (parent == this) {
            throw new IllegalStateException("Cannot set parent to self!");
        }
        mParent = parent;
        // This tab may have been freed due to low memory. If that is the case,
        // the parent tab id is already saved. If we are changing that id
        // (most likely due to removing the parent tab) we must update the
        // parent tab id in the saved Bundle.
        if (mSavedState != null) {
            if (parent == null) {
                mSavedState.remove(PARENTTAB);
            } else {
                mSavedState.putLong(PARENTTAB, parent.getId());
            }
        }

        // Sync the WebView useragent with the parent
        if (parent != null && mSettings.hasDesktopUseragent(parent.getWebView())
                != mSettings.hasDesktopUseragent(getWebView())) {
            mSettings.toggleDesktopUseragent(getWebView());
        }

        if (parent != null && parent.getId() == getId()) {
            throw new IllegalStateException("Parent has same ID as child!");
        }
    }

    /**
     * If this Tab was created through another Tab, then this method returns
     * that Tab.
     *
     * @return the Tab parent or null
     */
    public Tab getParent() {
        return mParent;
    }

    /**
     * When a Tab is created through the content of another Tab, then we
     * associate the Tabs.
     *
     * @param child the Tab that was created from this Tab
     */
    public void addChildTab(Tab child) {
        if (mChildren == null) {
            mChildren = new Vector<Tab>();
        }
        mChildren.add(child);
        child.setParent(this);
    }

    Vector<Tab> getChildren() {
        return mChildren;
    }

    public void resume() {
        if (mMainView != null) {
            setupHwAcceleration(mMainView);
            mMainView.onResume();
            if (mSubView != null) {
                mSubView.onResume();
            }
            mSetThemeColorSuccess = false;
            if (mInForeground) {
                ImmersiveController.getInstance().setWebViewStatusBarColor(ContextCompat.getColor(mContext, R.color.status_bar_webview));
                if (mMainView.isWebViewCreate()) {
                    setWebViewThemeColor(mMainView.getBaseWebView());
                }
            }
        }
    }

    private void setupHwAcceleration(View web) {
        if (web == null) return;
        BrowserSettings settings = BrowserSettings.getInstance();
        if (settings.isHardwareAccelerated()) {
            web.setLayerType(View.LAYER_TYPE_NONE, null);
        } else {
            web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void stop() {
        if (mMainView != null) {
            mMainView.onPause();
            if (mSubView != null) {
                mSubView.onPause();
            }
        }
    }

    void putInForeground() {
        if (mInForeground) {
            return;
        }
        mInForeground = true;
        resume();
        final Activity activity = mWebViewController.getActivity();
        if (mMainView != null) {
            mMainView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(BaseWebView baseWebView) {
                    baseWebView.setOnCreateContextMenuListener(activity);
                }
            });
        }
        if (mSubView != null) {
            mSubView.setOnCreateContextMenuListener(activity);
        }
        // Show the pending error dialog if the queue is not empty
        if (mQueuedErrors != null && mQueuedErrors.size() > 0) {
            showError(mQueuedErrors.getFirst());
        }
        mWebViewController.bookmarkedStatusHasChanged(this);
    }

    void putInBackground() {
        if (!mInForeground) {
            return;
        }
        if (!isNativePage()) {
            capture();
        }
        mInForeground = false;
        stop();
        if (mMainView != null) {
            mMainView.setOnCreateContextMenuListener(null);
        }
        if (mSubView != null) {
            mSubView.setOnCreateContextMenuListener(null);
        }
    }

    public boolean inForeground() {
        return mInForeground;
    }

    /**
     * Return the top window of this tab; either the subwindow if it is not
     * null or the main window.
     *
     * @return The top window of this tab.
     */
    public PreBrowserWebView getTopWindow() {
        if (mSubView != null) {
            return mSubView;
        }
        return mMainView;
    }

    /**
     * Return the main window of this tab. Note: if a tab is freed in the
     * background, this can return null. It is only guaranteed to be
     * non-null for the current tab.
     *
     * @return The main WebView of this tab.
     */
    public PreBrowserWebView getWebView() {
        /* Ensure the root webview object is in sync with our internal incognito status */
        if (mMainView != null) {
            if (isPrivateBrowsingEnabled() && !mMainView.isPrivateBrowsingEnabled()) {
                mMainView.setPrivateBrowsing(isPrivateBrowsingEnabled());
            }
        }
        return mMainView;
    }

    public BaseWebView getMainBrowserWebView() {
        return mMainView == null ? null : mMainView.getBaseWebView();
    }

    public void setViewContainer(View container) {
        mContainer = container;
    }

    public View getViewContainer() {
        return mContainer;
    }

    View getWebViewParentView() {
        if (mContainer == null) {
            return null;
        }
        return mContainer.findViewById(R.id.webview_wrapper);
    }

    /**
     * Return whether private browsing is enabled for the main window of
     * this tab.
     *
     * @return True if private browsing is enabled.
     */
    public boolean isPrivateBrowsingEnabled() {
        return mCurrentState.mIncognito;
    }

    public void setPrivateBrowsingEnabled(boolean isPrivate){
        mCurrentState.mIncognito = isPrivate;
        if(getSubWebView() != null) {
            getSubWebView().setPrivateBrowsing(isPrivate);
        }
        if(getWebView() != null) {
            getWebView().setPrivateBrowsing(isPrivate);
        }
    }
    /**
     * Return the subwindow of this tab or null if there is no subwindow.
     *
     * @return The subwindow of this tab or null.
     */
    public PreBrowserWebView getSubWebView() {
        return mSubView;
    }

    BaseWebView getSubBrowserWebView() {
        return mSubView == null ? null : mSubView.getBaseWebView();
    }

    public void setSubWebView(PreBrowserWebView subView) {
        mSubView = subView;
    }

    public View getSubViewContainer() {
        return mSubViewContainer;
    }

    public void setSubViewContainer(View subViewContainer) {
        mSubViewContainer = subViewContainer;
    }

    /**
     * @return The geolocation permissions prompt for this tab.
     */
    GeolocationPermissionsPrompt getGeolocationPermissionsPrompt() {
        if (mGeolocationPermissionsPrompt == null) {
            ViewStub stub = mContainer
                    .findViewById(R.id.geolocation_permissions_prompt);
            mGeolocationPermissionsPrompt = (GeolocationPermissionsPrompt) stub
                    .inflate();
        }
        return mGeolocationPermissionsPrompt;
    }

    /**
     * @return The application id string
     */
    public String getAppId() {
        return mAppId;
    }

    /**
     * Set the application id string
     */
    public void setAppId(String id) {
        mAppId = id;
    }

    boolean closeOnBack() {
        return mCloseOnBack;
    }

    public void setCloseOnBack(boolean close) {
        mCloseOnBack = close;
    }

    public String getUrl() {
        if (mIsNativePage) {
            return Constants.NATIVE_PAGE_URL;
        }
        return UrlUtils.filteredUrl(mCurrentState.mUrl);
    }

    public String getOriginalUrl() {
        if (mCurrentState.mOriginalUrl == null) {
            return getUrl();
        }
        return UrlUtils.filteredUrl(mCurrentState.mOriginalUrl);
    }

    /**
     * Get the title of this tab.
     */
    public String getTitle() {
        String title = mCurrentState.mTitle;
        if (isNativePage()) {
            title = mContext.getString(R.string.home_page);
        }
        if (TextUtils.isEmpty(title)) {
            title = getUrl();
            if (TextUtils.isEmpty(title) && isNativePage()) {
                title = mContext.getString(R.string.home_page);
            }
        }
        return title;
    }

    /**
     * Get the favicon of this tab.
     */
    public Bitmap getFavicon() {
        if (mCurrentState.mFavicon != null) {
            return mCurrentState.mFavicon;
        }
        return getDefaultFavicon(mContext);
    }

    public boolean isBookmarkedSite() {
        return mCurrentState.mIsBookmarkedSite;
    }

    /**
     * Return the tab's error console. Creates the console if createIfNEcessary
     * is true and we haven't already created the console.
     *
     * @param createIfNecessary Flag to indicate if the console should be
     *                          created if it has not been already.
     * @return The tab's error console, or null if one has not been created and
     * createIfNecessary is false.
     */
    public BrowserErrorConsoleView getErrorConsole(boolean createIfNecessary) {
        if (createIfNecessary && mErrorConsole == null) {
            mErrorConsole = new BrowserErrorConsoleView(mContext);
            mErrorConsole.setWebView(mMainView);
        }
        return mErrorConsole;
    }

    /**
     * Sets the security state, clears the SSL certificate error and informs
     * the controller.
     */
    private void setSecurityState(SecurityState securityState) {
        mCurrentState.mSecurityState = securityState;
        mCurrentState.mSslCertificateError = null;
        mWebViewController.onUpdatedSecurityState(this);
    }

    /**
     * @return The tab's security state.
     */
    public SecurityState getSecurityState() {
        return mCurrentState.mSecurityState;
    }

    /**
     * Gets the SSL certificate error, if any, for the page's main resource.
     * This is only non-null when the security state is
     * SECURITY_STATE_BAD_CERTIFICATE.
     */
    public SslError getSslCertificateError() {
        return mCurrentState.mSslCertificateError;
    }

    public int getLoadProgress() {
        if (mInPageLoad) {
            return mPageLoadProgress;
        }
        return 100;
    }

    /**
     * @return TRUE if onPageStarted is called while onPageFinished is not
     * called yet.
     */
    public boolean inPageLoad() {
        return mInPageLoad;
    }

    public boolean isShowErrorPage() {
        return mIsShowErrorPage;
    }

    /**
     * @return The Bundle with the tab's state if it can be saved, otherwise null
     */
    public Bundle saveState() {
        // If the WebView is null it means we ran low on memory and we already
        // stored the saved state in mSavedState.
        if (mMainView == null && !isNativePage()) {
            return mSavedState;
        }

        if (TextUtils.isEmpty(mCurrentState.mUrl) && !isNativePage()) {
            return null;
        }

        mSavedState = new Bundle();
        boolean isPrivateBrowsingEnable = false;
        if (!isNativePage()) {
            mMainView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(BaseWebView baseWebView) {
                    WebBackForwardList savedList = baseWebView.saveState(mSavedState);
                    if (savedList == null || savedList.getSize() == 0) {
                    }
                }
            });
            isPrivateBrowsingEnable = mMainView.isPrivateBrowsingEnabled();
        }
        mSavedState.putLong(ID, mId);
        mSavedState.putString(CURRURL, mCurrentState.mUrl);
        mSavedState.putString(CURRTITLE, mCurrentState.mTitle);
        mSavedState.putBoolean(INCOGNITO, isPrivateBrowsingEnable);
        mSavedState.putBoolean(ISNATIVEHOMEPAGE, mIsNativePage);
        mSavedState.putInt(PAGEPOSITION, mPagePosition);
        if (mAppId != null) {
            mSavedState.putString(APPID, mAppId);
        }
        mSavedState.putBoolean(CLOSEFLAG, mCloseOnBack);
        // Remember the parent tab so the relationship can be restored.
        if (mParent != null) {
            mSavedState.putLong(PARENTTAB, mParent.mId);
        }
        mSavedState.putBoolean(USERAGENT,
                mSettings.hasDesktopUseragent(getWebView()));
        return mSavedState;
    }

    /*
     * Restore the state of the tab.
     */
    private void restoreState(Bundle b) {
        mSavedState = b;
        if (mSavedState == null) {
            return;
        }
        // Restore the internal state even if the WebView fails to restore.
        // This will maintain the app id, original url and close-on-exit values.
        mId = b.getLong(ID);
        mAppId = b.getString(APPID);
        mCloseOnBack = b.getBoolean(CLOSEFLAG);
        restoreUserAgent();
        String url = b.getString(CURRURL);
        String title = b.getString(CURRTITLE);
        boolean incognito = BrowserSettings.getInstance().noFooterBrowser();
        mIsNativePage = b.getBoolean(ISNATIVEHOMEPAGE);
        mPagePosition = b.getInt(PAGEPOSITION);
        mCurrentState = new PageState(mContext, incognito, url, null);
        mCurrentState.mTitle = title;
        synchronized (Tab.this) {
            if (mCapture != null) {
                BrowserDataController.getInstance(mContext).loadThumbnail(this);
            }
        }
    }

    private void restoreUserAgent() {
        if (mMainView == null || mSavedState == null) {
            return;
        }
        if (mSavedState.getBoolean(USERAGENT)
                != mSettings.hasDesktopUseragent(mMainView)) {
            mSettings.toggleDesktopUseragent(mMainView);
        }
    }

    public void updateBookmarkedStatus() {
        mBrowserDataController.queryBookmarkStatus(getUrl(), mIsBookmarkCallback);
    }

    private BrowserDataController.OnQueryUrlIsBookmark mIsBookmarkCallback
            = new BrowserDataController.OnQueryUrlIsBookmark() {
        @Override
        public void onQueryUrlIsBookmark(String url, boolean isBookmark) {
            if (mCurrentState.mUrl != null && mCurrentState.mUrl.equals(url)) {
                mCurrentState.mIsBookmarkedSite = isBookmark;
                mWebViewController.bookmarkedStatusHasChanged(Tab.this);
            }
        }
    };

    public Bitmap getScreenshot() {
        synchronized (Tab.this) {
            return mCapture;
        }
    }

    public boolean isSnapshot() {
        return false;
    }

    private class SaveCallback implements ValueCallback<String> {
        String mPath;

        @Override
        public void onReceiveValue(String path) {
            mPath = path;
            synchronized (this) {
                notifyAll();
            }
            ContentValues mValues = createSnapshotValues();
            try {
                mValues.put(Snapshots.VIEWSTATE_PATH, mPath);
                mValues.put(Snapshots.VIEWSTATE_SIZE, new File(mPath).length());
            } catch (Exception e) {
                return;
            }
            String value = mValues.getAsString(SnapshotProvider.Snapshots.TITLE) + ":" + mValues.getAsString
                    (SnapshotProvider.Snapshots.URL);

            Map<String, String> params = new ArrayMap<>();
            final ContentResolver cr = mContext.getContentResolver();
            Uri result = cr.insert(SnapshotProvider.Snapshots.CONTENT_URI, mValues);
            if (result == null) {
                return;
            }
            ToastUtil.showShortToastByString(mContext, mContext.getString(R.string.prompt_saved_off_line));
        }

        public String getPath() {
            return mPath;
        }
    }

    /**
     * Must be called on the UI thread
     */
    public ContentValues createSnapshotValues() {
        PreBrowserWebView web = mMainView;
        if (web == null) return null;
        ContentValues values = new ContentValues();
        values.put(Snapshots.TITLE, mCurrentState.mTitle);
        values.put(Snapshots.URL, mCurrentState.mUrl);
        values.put(Snapshots.DATE_CREATED, System.currentTimeMillis());
        //Here is stored touch icon
        byte[] touchIcon = compressBitmap(WebIconUtils.getWebIconFromLocalDb(mContext, mCurrentState.mUrl));
        if (touchIcon != null && touchIcon.length < BuildUtil.WEB_SITE_ICON_LENGHT) {
            values.put(Snapshots.FAVICON, touchIcon);
        }

        //now not need saved thumbnail,saved it for future
//        Bitmap screenshot = Controller.createScreenshot(mMainView,
//                Controller.getDesiredThumbnailWidth(mContext),
//                Controller.getDesiredThumbnailHeight(mContext));
//        values.put(Snapshots.THUMBNAIL, compressBitmap(screenshot));
        return values;
    }

    /**
     * Probably want to call this on a background thread
     */
    public void saveViewState() {
        PreBrowserWebView web = mMainView;
        if (web == null) return;
        String path = UUID.randomUUID().toString() + ".mhtml";
        File savedFile = new File(mContext.getFilesDir(), path);
        path = savedFile.getAbsolutePath();
        final SaveCallback callback = new SaveCallback();
        final String finalPath = path;
        web.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
            @Override
            public void call(BaseWebView baseWebView) {
                try {
                    synchronized (callback) {
                        baseWebView.saveWebArchive(finalPath, false, callback);
                    }
                } catch (Exception e) {
                    String savedPath = callback.getPath();
                    if (savedPath != null) {
                        File file = new File(savedPath);
                        if (file.exists() && !file.delete()) {
                            file.deleteOnExit();
                        }
                    }
                }
            }
        });
    }

    public byte[] compressBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public void loadUrl(String url, Map<String, String> headers) {
        if (mMainView != null) {
            mPageLoadProgress = INITIAL_PROGRESS;
            mInPageLoad = true;
            mIsShowErrorPage = false;
            mCurrentState = new PageState(mContext, mMainView.isPrivateBrowsingEnabled(), url, null);
            mMainView.loadUrl(url, headers);
            mWebViewController.onPageStarted(this, mMainView.getBaseWebView(), null);
        }
    }

    public void loadData(String data) {
        if (mMainView != null) {
            mPageLoadProgress = INITIAL_PROGRESS;
            mInPageLoad = true;
            mIsShowErrorPage = false;
            mCurrentState = new PageState(mContext, false, data, null);
            mMainView.loadData(data, "text/html", "UTF-8");
            mWebViewController.onPageStarted(this, mMainView.getBaseWebView(), null);
        }
    }

    public void disableUrlOverridingForLoad() {
        mDisableOverrideUrlLoading = true;
    }

    public  void capture() {
        if (mIsNativePage) {
            if (mHomePage == null) return;
            captureView(mHomePage);
        } else {
            if (mMainView == null || mCapture == null || mMainView.getHeight() <= 0) return;
            captureView(mMainView);
        }

        mHandler.removeMessages(MSG_CAPTURE);
        persistThumbnail();
    }

    protected void captureView(View view) {
        float scale = mCaptureWidth / (float) view.getWidth();
        Canvas c = new Canvas(mCapture);
        mCapture.eraseColor(Color.WHITE);
        final int left = view.getScrollX();
        int top = view.getScrollY();
        int state = c.save();
        c.translate(-left, -top);
        c.scale(scale, scale, left, top);
        view.draw(c);
        c.restoreToCount(state);
        c.setBitmap(null);
        mCaptureSuccess = true;
        if (!TextUtils.isEmpty(mSettings.getHomePage()) && mSettings.getHomePage().equals(getUrl())) {
            mWebViewController.getTabControl().setHomeCapture(isPrivateBrowsingEnabled(), mCapture, true);
        }
    }

    public boolean getCaptureSuccess() {
        return mCaptureSuccess;
    }

    public void initCaptureBitmap(int width, int height) {
        mCapture = ImageUtils.createBitmap(width, height, Bitmap.Config.RGB_565);
        if (mCapture != null) {
            mCapture.eraseColor(ContextCompat.getColor(mContext, R.color.incognito_bg_color));
            mWebViewController.getTabControl().setHomeCapture(true, mCapture, false);
            mCapture.eraseColor(Color.WHITE);
            mWebViewController.getTabControl().setHomeCapture(false, mCapture, false);
        }
    }

    @Override
    public void onNewPicture(WebView view, Picture picture) {
    }

    private void postCapture() {
        if (!mHandler.hasMessages(MSG_CAPTURE)) {
            mHandler.sendEmptyMessageDelayed(MSG_CAPTURE, CAPTURE_DELAY);
        }
    }

    public boolean canGoBack() {
        return getWebView() != null && getWebView().canGoBack();
    }

    public boolean canGoForward() {
        return getWebView() != null && getWebView().canGoForward();
    }

    public void goBack() {
        if (getCurrentBrowserWebView() != null) {
            mSetThemeColorSuccess = false;
            if (mInForeground) {
                ImmersiveController.getInstance().setWebViewStatusBarColor(ContextCompat.getColor(mContext, R.color.status_bar_webview));
            }
            getCurrentBrowserWebView().goBack();
            setWebViewThemeColor(getCurrentBrowserWebView());
        }
    }

    public void goForward() {
        if (getCurrentBrowserWebView() != null) {
            getCurrentBrowserWebView().goForward();
        }
    }

    /**
     * Causes the tab back/forward stack to be cleared once, if the given URL is the next URL
     * to be added to the stack.
     * <p/>
     * This is used to ensure that preloaded URLs that are not subsequently seen by the user do
     * not appear in the back stack.
     */
    public void clearBackStackWhenItemAdded(Pattern urlPattern) {
        mClearHistoryUrlPattern = urlPattern;
    }

    protected void persistThumbnail() {
        BrowserDataController.getInstance(mContext).saveThumbnail(this);
    }

    protected void deleteThumbnail() {
        BrowserDataController.getInstance(mContext).deleteThumbnail(this);
    }

    void updateCaptureFromBlob(byte[] blob) {
        synchronized (Tab.this) {
            if (mCapture == null) {
                return;
            }
            ByteBuffer buffer = ByteBuffer.wrap(blob);
            try {
                mCapture.copyPixelsFromBuffer(buffer);
            } catch (RuntimeException rex) {
                Log.e(LOGTAG, "Load capture has mismatched sizes; buffer: "
                        + buffer.capacity() + " blob: " + blob.length
                        + "capture: " + mCapture.getByteCount());
                throw rex;
            }
        }
    }

    public void updateCaptureFromBitmap(Bitmap bitmap) {
        synchronized (this) {
            if (mCapture == null || bitmap == null) {
                return;
            }
            mCapture = bitmap.copy(Bitmap.Config.RGB_565, true);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(100);
        builder.append(mId);
        builder.append(") has parent: ");
        if (getParent() != null) {
            builder.append("true[");
            builder.append(getParent().getId());
            builder.append("]");
        } else {
            builder.append("false");
        }
        builder.append(", incog: ");
        builder.append(isPrivateBrowsingEnabled());
        if (!isPrivateBrowsingEnabled()) {
            builder.append(", title: ");
            builder.append(getTitle());
            builder.append(", url: ");
            builder.append(getUrl());
        }
        return builder.toString();
    }

    private void handleProceededAfterSslError(SslError error) {
        if (error.getUrl().equals(mCurrentState.mUrl)) {
            // The security state should currently be SECURITY_STATE_SECURE.
            setSecurityState(SecurityState.SECURITY_STATE_BAD_CERTIFICATE);
            mCurrentState.mSslCertificateError = error;
        } else if (getSecurityState() == SecurityState.SECURITY_STATE_SECURE) {
            // The page's main resource is secure and this error is for a
            // sub-resource.
            setSecurityState(SecurityState.SECURITY_STATE_MIXED);
        }
    }

    public void setAcceptThirdPartyCookies(final boolean accept) {
        final CookieManager cookieManager = CookieManager.getInstance();
        if (mMainView != null && Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.LOLLIPOP) {
            mMainView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                @Override
                public void call(BaseWebView baseWebView) {
                    try {
                        cookieManager.setAcceptThirdPartyCookies(baseWebView, accept);
                    } catch (Exception e) {
                    }
                }
            });

        }
    }

    public boolean isNativePage() {
        return mIsNativePage;
    }

    public void setNativePage(boolean isNativeTab) {
        if (mIsNativePage && !isNativeTab) {//Status changed, add webview
            if (null == mMainView) {
                PreBrowserWebView newWebView = mWebViewController.getTabControl().createNewWebView();
                mCurrentState = new PageState(mContext, newWebView.isPrivateBrowsingEnabled());
                newWebView.addWebViewOperation(new PreBrowserWebView.WebViewOperation() {
                    @Override
                    public void call(BaseWebView baseWebView) {
                        baseWebView.setOnCreateContextMenuListener(mWebViewController.getActivity());
                    }
                });
                setWebView(newWebView);
            } else {
//                assert false;
            }
        }

        this.mIsNativePage = isNativeTab;
        if (mIsNativePage) {
            mWebViewController.doUpdateVisitedHistory(this, true);
        }
    }

    public void setHomePage(View homePage) {
        this.mHomePage = homePage;
    }

    public void setWebViewStatusChange(WebViewStatusChange change) {
        this.mWebViewStatusChange = change;
    }

    public void setWebViewThemeColor(WebView view) {
        if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.KITKAT) {
            try {
                view.evaluateJavascript(Constants.JAVASCRIPT_THEME_COLOR, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
//                        setSystemBarColor(s);
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    public void setSystemBarColor(String color) {
        if (!mInForeground) return;
        if (!TextUtils.isEmpty(color)) {
            color = color.replace("\"", "").replace("\'", "");
            if (!TextUtils.isEmpty(color)) {
                try {
                    if (color.toLowerCase().contains("black")) {
                        color = "black";
                    }
                    ImmersiveController.getInstance().setWebViewStatusBarColor(Color.parseColor(color));
                    mSetThemeColorSuccess = true;
                    return;
                } catch (Exception e) {
                }
            }
        }
    }

    private void addImageClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src);  " +//通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
                "    }  " +
                "}" +
                "})()");
    }

    /**
     * fix 4.1系统加载网页时出现IllegalArgumentException
     */
    public static void disableJsIfUrlEncodedFailed(WebView webView, String url){
        if (webView == null || TextUtils.isEmpty(url) || Build.VERSION.SDK_INT != BuildUtil.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        try {
            Class<?> urlEncodeUtilsClass = Class.forName("org.apache.http.client.utils.URLEncodedUtils");
            Method methodParse= urlEncodeUtilsClass.getDeclaredMethod("parse", URI.class, String.class);
            methodParse.invoke(null, new URI(url), null);
            webView.getSettings().setJavaScriptEnabled(true);
        } catch (IllegalArgumentException e) {
            webView.getSettings().setJavaScriptEnabled(false);
        } catch (Exception f) {
            return;
        }
    }
}

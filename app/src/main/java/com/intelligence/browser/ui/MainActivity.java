/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.intelligence.browser.base.ActivityController;
import com.intelligence.browser.base.UI;
import com.intelligence.browser.downloads.BrowserDownloadManager;
import com.intelligence.browser.downloads.BrowserNetworkStateNotifier;
import com.intelligence.browser.controller.NullController;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.browser.view.DestroyAnim;
import com.intelligence.browser.webview.Controller;
import com.intelligence.commonlib.config.BrowserConfig;
import com.intelligence.commonlib.config.VersionInfo;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.UrlUtils;

import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String ACTION_SHOW_BOOKMARKS = "show_bookmarks";
    public static final String ACTION_SHOW_BROWSER = "show_browser";
    public static final String ACTION_OPEN_URL = "open_url";
    public static final String ACTION_RESTART = "--restart--";
    private static final String EXTRA_STATE = "state";
    public static final String EXTRA_DISABLE_URL_OVERRIDE = "disable_url_override";

    private final static String LOGTAG = "browser";

    private ActivityController mController = NullController.INSTANCE;

    private HandlerThread mHandlerThread;
    private BroadcastReceiver mReceiver;
    private float mTouchX, mTouchY;
    private View mRootView;
    private int mCurrHeight = -1;
    private boolean mIsActive = false;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mRootView == null) return;
            Rect r = new Rect();
            mRootView.getWindowVisibleDisplayFrame(r);
            int visitHeight = mRootView.getBottom() - r.bottom;
            if (mCurrHeight == visitHeight) {
                return;
            }
            mCurrHeight = visitHeight;
            if (mController != null && mController instanceof Controller) {
                ((Controller) mController).onInputKeyChanged(mCurrHeight);
            }
        }
    };

    @Override
    public void setCurrentLanguage() {
        super.setCurrentLanguage();
        RecommendUrlUtil.resetDbForChangeLanguage(MainActivity.this);
    }

    static void initSVG(){
        try {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }catch (Exception e){
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initSVG();
        mController = createController();
        BrowserApplication.getInstance().setController(mController);
        Intent intent = (icicle == null) ? getIntent() : null;
        mController.start(intent);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                    RecommendUrlUtil.resetDbForChangeLanguage(context);
                    getController().exit();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalListener);
        showPrivacyProduct();
    }

    public interface ConfigCallBack{
        void callBack(VersionInfo versionInfo);
    }
    private Controller createController() {
        Controller controller = new Controller(this);
        UI ui = new BrowserPhoneUi(this, controller);
        controller.setUi(ui);
        return controller;
    }

    Controller getController() {
        return (Controller) mController;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (shouldIgnoreIntents()) return;

            if (ACTION_RESTART.equals(intent.getAction())) {
                Bundle outState = new Bundle();
                mController.onSaveInstanceState(outState);
                finish();
                getApplicationContext().startActivity(
                        new Intent(getApplicationContext(), MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(EXTRA_STATE, outState));
                return;
            }
            if (intent.getData() != null) {
                String url = intent.getData().getQueryParameter("url");
                boolean newtab = intent.getData().getBooleanQueryParameter("newtab", false);
                boolean isInputUrl = intent.getData().getBooleanQueryParameter("isInputUrl", false);
                String inputword = intent.getData().getQueryParameter("inputword");
                String fromsource = intent.getData().getQueryParameter("fromsource");
                if ((SchemeUtil.BROWSER_SOURCE_KEY_SEARCH + "").equals(fromsource)) {
//                if (!SharedPreferencesUtils.getRecommonWebsites() && url.contains("https://wap.sogou.com/")) {
//                    url = url + "&bid=sogou-mobp-95e62984b87e9064";
//                }
                    ((Controller) mController).onSelect(url, isInputUrl, inputword);
                } else if (!TextUtils.isEmpty(url)) {
                    if (UrlUtils.isSearch(url)) {
                        String filterUrl = com.intelligence.browser.utils.UrlUtils.filterBySearchEngine(this, url);
                        if (filterUrl != null) {
                            url = filterUrl;
                        }
                    }
                    if (newtab) {
                        ((Controller) mController).createAndOpenTab(url);
                    } else {
                        ((Controller) mController).openUrl(url);
                    }
                }
            }
            mController.handleNewIntent(intent);
        }catch (Exception e){

        }
    }

    private PowerManager mPowerManager;

    private boolean shouldIgnoreIntents() {
        // Only process intents if the screen is on and the device is unlocked
        // aka, if we will be user-visible
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }
        boolean ignore = !mPowerManager.isScreenOn();
        return ignore;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mController.onResume();
        BrowserNetworkStateNotifier.getInstance().registerReveiver(this);
        if (DeviceInfoUtils.isAppOnForeground(this) && !mIsActive) {
            mIsActive = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mController != null){
            mController.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (Window.FEATURE_OPTIONS_PANEL == featureId) {
            mController.onMenuOpened(featureId, menu);
        }
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        mController.onOptionsMenuClosed(menu);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        mController.onContextMenuClosed(menu);
    }

    /**
     * onSaveInstanceState(Bundle map)
     * onSaveInstanceState is called right before onStop(). The map contains
     * the saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mController.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        try {
            mController.onPause();
            super.onPause();
            BrowserNetworkStateNotifier.getInstance().unRegisterReceiver(this);

        } catch (Exception e) {
        }
    }

    private boolean isAppInBackground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.processName.equals(getPackageName())) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void onStop() {
        mController.onStop();
        super.onStop();

        if (!DeviceInfoUtils.isAppOnForeground(this) && mIsActive) {
            mIsActive = false;
        }
    }

    @Override
    protected void onDestroy() {

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
        super.onDestroy();
        if (mController != null) {
            mController.onDestroy();
        }
        mController = NullController.INSTANCE;

        if (mReceiver != null) {
            this.unregisterReceiver(mReceiver);
        }

        BrowserDownloadManager.getInstance().onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mController.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mController.onLowMemory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return mController.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return mController.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mController.onOptionsItemSelected(item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        mController.onCreateContextMenu(menu, v, menuInfo, mTouchX, mTouchY);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return mController.onContextItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mController.onKeyDown(keyCode, event) ||
                super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return mController.onKeyLongPress(keyCode, event) ||
                super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mController.onKeyUp(keyCode, event) ||
                super.onKeyUp(keyCode, event);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        mController.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        mController.onActionModeFinished(mode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mController.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onSearchRequested() {
        return mController.onSearchRequested();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mController.dispatchKeyEvent(event)
                || super.dispatchKeyEvent(event);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return mController.dispatchKeyShortcutEvent(event)
                || super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mTouchX = ev.getRawX();
        mTouchY = ev.getRawY();
        return mController.dispatchTouchEvent(ev)
                || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return mController.dispatchTrackballEvent(ev)
                || super.dispatchTrackballEvent(ev);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return mController.dispatchGenericMotionEvent(ev) ||
                super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
        mController.invalidateOptionsMenu();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void checkUpdate() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (true) {
                    try {
                        BrowserConfig.initBrowserConfig(MainActivity.this, new ConfigCallBack() {
                            @Override
                            public void callBack(VersionInfo versionInfo) {
                                SharedPreferencesUtils.setNewVersion(versionInfo.versionCode);
                            }
                        });
                    } catch (Exception e) {
                    }
                } else {
//                    request();
                }
            }
        }, 0);
    }

    boolean isNeedExitAnim = false;
    @Override
    public void finish() {
        if(isNeedExitAnim) {
            new DestroyAnim(this, Color.BLACK).start(new Runnable() {
                public void run() {
                    MainActivity.super.finish();
                }
            });
        }else {
            MainActivity.super.finish();
        }
    }

    private boolean isShowPerssionDialog;
    private void showPrivacyProduct(){
        checkUpdate();
    }

    public void exitApp(){
        isNeedExitAnim = false;
        finish();
    }
}

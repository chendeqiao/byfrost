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

package com.intelligence.browser.base;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.ui.webview.JsInterfaceInject;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.List;

public interface UI {

    enum ComboViews {
        History,
        Bookmarks,
        Snapshots,
    }

    enum ComboHomeViews {
        VIEW_HIDE_NATIVE_PAGER,
        VIEW_NATIVE_PAGER,
        VIEW_NAV_SCREEN,
        VIEW_WEBVIEW,
    }

    void onPause();

    void onStop();

    void onResume();

    void onDestroy();

    void onConfigurationChanged(Configuration config);

    boolean onBackKey();

    boolean onMenuKey();

    boolean needsRestoreAllTabs();

    void addTab(Tab tab);

    void removeTab(Tab tab);

    void setActiveTab(Tab tab);

    void updateTabs(List<Tab> tabs);

    void detachTab(Tab tab);

    void attachTab(Tab tab);

    void onSetWebView(Tab tab, PreBrowserWebView view);

    void createSubWindow(Tab tab, PreBrowserWebView subWebView);

    void attachSubWindow(View subContainer);

    void removeSubWindow(View subContainer);

    void onTabDataChanged(Tab tab);

    void onPageStopped(Tab tab);

    void onProgressChanged(Tab tab);

    void showActiveTabsPage();

    void removeActiveTabsPage();

    void showComboView(ComboViews startingView, Bundle extra);

    void showCustomView(View view, int requestedOrientation,
                        CustomViewCallback callback);

    void onHideCustomView();

    boolean isCustomViewShowing();

    boolean onPrepareOptionsMenu(Menu menu);

    void updateMenuState(Tab tab, Menu menu);

    void onOptionsMenuOpened();

    void onExtendedMenuOpened();

    boolean onOptionsItemSelected(MenuItem item);

    void onOptionsMenuClosed(boolean inLoad);

    void onExtendedMenuClosed(boolean inLoad);

    void onContextMenuCreated(Menu menu);

    void onContextMenuClosed(Menu menu, boolean inLoad);

    void onActionModeStarted(ActionMode mode);

    void onActionModeFinished(boolean inLoad);

    void setShouldShowErrorConsole(Tab tab, boolean show);

    // returns if the web page is clear of any overlays (not including sub windows)
    boolean isWebShowing();

    void showWeb(boolean animate);

    Bitmap getDefaultVideoPoster();

    View getVideoLoadingProgressView();

    void bookmarkedStatusHasChanged(Tab tab);

    void showMaxTabsWarning();

    boolean dispatchKey(int code, KeyEvent event);

    void setUseQuickControls(boolean enabled);

    boolean shouldCaptureThumbnails();

    boolean blockFocusAnimations();

    void onVoiceResult(String result);

    void onQrUrl(String url);

    void onSelectIncognito(String url);

    void loadJsObject(JsInterfaceInject jsObject, boolean force);
    void removeJsObjectRef(Tab tab);

    /**
     * 以两种方式打开tab
     * @param url 要打开的的url
     * @param isNewTab  是否以新标签的方式打开
     */
    void showAndOpenUrl(String url ,boolean isNewTab) ;

    /**
     * open new tab for webpage,the same with plus in left top corner
     */
    void openViewPage();

    void onNetworkToggle(boolean up);

    View getMainContent();

    void onIncognito(boolean isIncognito);

    /**
     * this is open search input activity
     * @param url
     */
    void openSearchInputView(String url);

    void showWebViewPage(boolean isShowWebView);

    void isRecommendEdit(boolean isEdit);

    void isCanScrollWebPage(boolean isShowGoWebview);

    void toobarunfold(float isOpen);

    void adHintVisible(boolean isClose,String url);

    void showTools(boolean isShow);

    void openSelectWebSiteView(View view, WebSiteData webSiteData);

    void showCardTipsView(boolean isShow);

}

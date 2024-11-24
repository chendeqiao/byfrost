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

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.intelligence.browser.webview.BaseWebView;
import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.webview.TabControl;
import com.intelligence.browser.base.UI.ComboViews;
import com.intelligence.browser.ui.setting.BottomSettingplaneDialogFragment;
import com.intelligence.browser.ui.widget.PreBrowserWebView;
import com.intelligence.componentlib.dialog.BaseDialogFragment;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.List;


public interface UiController {

    UI getUi();

    PreBrowserWebView getCurrentWebView();

    PreBrowserWebView getCurrentTopWebView();

    BaseWebView getCurrentTopBrowserWebView();

    Tab getCurrentTab();

    TabControl getTabControl();

    List<Tab> getTabs();

    Tab openTabToHomePage();

    Tab openIncognitoTab();

    Tab openTab(String url, boolean incognito, boolean setActive,
                boolean useCurrent);

    /**
     * 激活Tab某个tab，并加载新的URl
     *
     * @param tab
     * @param url
     */
    void openTab(Tab tab, String url);

    /**
     * 创建并打开指定的url
     *
     * @param url
     */
    void createAndOpenTab(String url);

    void createAndOpenTabIncognito(String url);

    void setActiveTab(Tab tab);

    boolean switchToTab(Tab tab);

    void closeCurrentTab();

    void closeTab(Tab tab);

    void closeOtherTabs();

    void closeAllTabs(boolean incognito);

    void stopLoading();

    Intent createBookmarkCurrentPageIntent(boolean canBeAnEdit);

    void bookmarksOrHistoryPicker(ComboViews startView);

    void bookmarkCurrentPage();

    void editUrl();

    void handleNewIntent(Intent intent);

    boolean shouldShowErrorConsole();

    void hideCustomView();

    void attachSubWindow(Tab tab);

    void removeSubWindow(Tab tab);

    boolean isInCustomActionMode();

    void endActionMode();

    void shareCurrentPage();

    void updateMenuState(Tab tab, Menu menu);

    boolean onOptionsItemSelected(MenuItem item);

    void loadUrl(Tab tab, String url);

    void loadNativePage(Tab tab);

    void setBlockEvents(boolean block);

    Activity getActivity();

    void showPageInfo();

    void openPreferences();

    void findOnPage();

    void toggleUserAgent();

    BrowserSettings getSettings();

    boolean supportsVoice();

    void startVoiceRecognizer();

    void startScan();

    /**
     * The WebView back
     */
    void goBack();

    void goForward();

    boolean canGoBack();

    boolean canGoForward();

    void showCommonMenu(BottomSettingplaneDialogFragment menu);

    void showNavigationPage(BaseDialogFragment menu);

    /**
     * The menu bar item click event
     */
    void menuPopuOnItemClick(View view);

    void onCloseMenu(boolean isDoAnimation);

    void onCloseMenu(boolean isDoAnimation,int delayTime);

    void updateCommomMenuState(BottomSettingplaneDialogFragment commomMenu);

    void onToolBarItemClick(int viewId);

    void updateToolBarItemState();

    void panelSwitchHome(Tab current);

    void registerFullscreenListener(FullscreenListener listener);

    void setFullscreen(boolean isEnabled);

    BrowserMainPageController getViewPageController();

    void addBookmark(String title);

    void toobarunfold(float isOpen);

    void showDownloadAnimation();

    int toolBarHeight();

    void refreshWebview();

    void showWebViewPage(boolean isShowWebView);

    void showTools(boolean isShow);

    boolean setBookMarksStyle();

    boolean canAddBookmark();

    void onDownloadStart( String url, String useragent, String mimeType, long contentLength);

    void openSelectWebSiteView(View view, WebSiteData webSiteData);

    boolean isCanScroll(MotionEvent ev);
}

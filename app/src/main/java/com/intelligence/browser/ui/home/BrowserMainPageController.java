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

package com.intelligence.browser.ui.home;

import android.app.Activity;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.intelligence.browser.R;
import com.intelligence.browser.base.UI;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.ui.home.navigation.WebNavigationView;
import com.intelligence.browser.webview.Tab;
import com.intelligence.commonlib.tools.DisplayUtil;

public class BrowserMainPageController implements
        View.OnClickListener {

    public static final int STATUS_EMPTY = 0;
    public static final int STATUS_DONE = 1;

    private Activity mContext;
    private FrameLayout mHomePage;
    private View mBlackBackground;
    private WebNavigationView mWebNavigationViewView;
    private Tab mTab;
    private Handler mHandler = new Handler();
    private BaseUi mUi;
    public UiController mController;
    private int mInitState = STATUS_EMPTY;
    private OnSearchUrl mListener;

    private HomePageHeadView mHeadTitleView;

    private boolean mDataInit = false;

    private boolean mIncognito;

    public BrowserMainPageController(UiController controller, Activity context) {
        mInitState = STATUS_EMPTY;
        mController = controller;
        this.mContext = context;
    }

    public void setUi(UI ui) {
        mUi = (BaseUi) ui;
    }

    // 占位，避免编译过滤掉资源文件
    public static int[] mRecommendIcon_en = {
            R.drawable.browser_webicon_baidu,
            R.drawable.browser_webicon_qiyi,
            R.drawable.browser_webicon_taobao,
            R.drawable.browser_webicon_tengxun,
            R.drawable.browser_recommend_blank,
            R.drawable.browser_recommend_add,
            R.drawable.browser_webicon_weibo,
            R.drawable.browser_webicon_tmall,
            R.drawable.browser_webicon_bilibili,
            R.drawable.browser_webicon_fenghuang,
            R.drawable.browser_webicon_xigua
    };

    public static int[] mIconSearch = {
            R.drawable.browser_engine_baidu, R.drawable.browser_engine_google, R.drawable.browser_engine_yahoo,
            R.drawable.browser_engine_bing, R.drawable.browser_engine_360,
            R.drawable.browser_engine_sougou, R.drawable.browser_yandex_icon,
            R.drawable.browser_duckduckgo
    };

    public void registerSearchListener(final OnSearchUrl listener) {
        this.mListener = listener;
    }

    public void showViewPager() {
        attachMainViewPager();
        if (mHomePage != null) {
            mHomePage.setVisibility(View.VISIBLE);
            mHomePage.bringToFront();
        }
    }

    public void hideViewPager() {
        if (mHomePage != null) {
            mHomePage.setVisibility(View.GONE);
        }
    }

    public boolean isVisible() {
        return mInitState != STATUS_EMPTY &&
                mHomePage != null &&
                mHomePage.getVisibility() == View.VISIBLE;
    }

    public void setSearchViewMoveStyle() {
        if (mUi != null)
            mUi.openSearchInputView("");
    }

    public void onResumeIfNeed() {
        if (!mDataInit) {
            onResume();
            mDataInit = true;
        }
    }

    public void onResume() {
//        // 通知主页数据变更,这里的数据需要异步加载
        if (mHeadTitleView != null) {
            mHeadTitleView.onResume();
            mHeadTitleView.updateSearchEngineLogo(false);
        }
        if (mWebNavigationViewView != null) {
            mWebNavigationViewView.onResume();
        }
    }

    public void onPause() {
        if (mHeadTitleView != null) {
            mHeadTitleView.onPause();
        }
    }

    public void wrapScreenshot(Tab tab) {
        if (tab != null) {
            tab.setHomePage(mHomePage);
        }
    }

    public void switchTab(Tab tab) {
        this.mTab = tab;
    }

    public Tab getCurrentTab() {
        return mTab;
    }

    /**
     * 处理推荐item的点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
    }

    public int getInitStatus() {
        return mInitState;
    }

    /**
     * 初始化主界面
     */
    public void initRootView() {

        mHomePage = (FrameLayout) mUi.getHomeContainer();
        mBlackBackground = new View(mContext);
//        refreshBlackBgOffEdit(mIncognito);
        mHomePage.addView(mBlackBackground);

        initWebNavigationView();

        mHeadTitleView = new HomePageHeadView(mContext, this, false, mController.getTabControl().isIncognitoShowing());
        mHomePage.addView(mHeadTitleView, mHeadTitleView.createLayoutParams());

        mHomePage.setClipChildren(false);
        mInitState = STATUS_DONE;
    }

    public void notifyCardTips() {
        if(mWebNavigationViewView != null){
            mWebNavigationViewView.notifyCardTips();
        }
    }

    public void showCardTips(boolean isShowTips){
        if(mHeadTitleView != null){
            mHeadTitleView.showCardTips(isShowTips);
        }
        if(mWebNavigationViewView != null){
//            mWebNavigationViewView.showCardTips();
        }
    }

    public void initWebNavigationView() {
        this.mWebNavigationViewView = new WebNavigationView((FragmentActivity) mContext, mUi);
        mHomePage.addView(mWebNavigationViewView);
    }

    public boolean isShowTipsCard() {
        if (mWebNavigationViewView != null) {
            return mWebNavigationViewView.isShowTipsCard();
        }
        return false;
    }

    public void onIncognito(boolean incognito) {
        mHeadTitleView.onIncognito(incognito);
    }

    public void isRecommendEdit(boolean isEdit) {
        if(mHeadTitleView != null) {
            mHeadTitleView.isRecommendEdit(isEdit);
        }
    }

    public void attachMainViewPager() {
        if (getInitStatus() == BrowserMainPageController.STATUS_EMPTY) {
            initRootView();
        }
        if (mHomePage != null) {
            mWebNavigationViewView.setVisibility(View.VISIBLE);
            if (mHeadTitleView != null) {
                mHeadTitleView.setVisibility(View.VISIBLE);
            }
        }
    }

    public boolean onBackKey() {
        return mWebNavigationViewView != null && mWebNavigationViewView.onBackKey();
    }

    public void updateDefaultSearchEngine() {
        if (mHeadTitleView != null) {
            mHeadTitleView.post(new Runnable() {
                @Override
                public void run() {
                    mHeadTitleView.updateSearchEngineLogo(true);
                    mHeadTitleView.onResume();
                }
            });
        }
    }

    public boolean supportsVoice() {
        if (mController != null) {
            return mController.supportsVoice();
        }
        return false;
    }

    public void startVoiceRecognizer() {
        mController.startVoiceRecognizer();
    }

    public void startScan(){
        mController.startScan();
    }

    public boolean isCanScroll(MotionEvent ev){
        if(mWebNavigationViewView != null){
            return mWebNavigationViewView.isCanScroll(ev);
        }
        return false;
    }

}

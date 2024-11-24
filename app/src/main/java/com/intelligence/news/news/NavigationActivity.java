package com.intelligence.news.news;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.intelligence.browser.R;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.browser.view.TopScrollView;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.SystemTintBarUtils;
import com.intelligence.news.hotword.BrowserInitDataContants;
import com.intelligence.news.news.groupwebsites.GroupSitesView;
import com.intelligence.news.news.header.AppSitesView;
import com.intelligence.news.news.header.BrowserHisotryView;
import com.intelligence.news.news.header.website.WebSitesView;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.websites.WebsitesHttpRequest;
import com.intelligence.news.websites.bean.AllWebSiteData;
import com.intelligence.browser.ui.BaseActivity;

import java.util.ArrayList;

public class NavigationActivity extends BaseActivity {
    private SearchBar mSearchBar;
    private View mHomeIcon;
    private AppSitesView mAppSites;
    private BrowserHisotryView mBrowserHistory;
    private WebSitesView mWebsites;
    private FrameLayout mHeaderAd;
    private LinearLayout mWebSitesContaniner;
    private View mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_navigation_page);
        overridePendingTransition(R.anim.browser_slide_page_start, R.anim.browser_slide_page_exit);
        SystemTintBarUtils.setSystemBarColorByValue(this,Color.WHITE);
        init();
//        initAd();
        requestData();
        SharedPreferencesUtils.put(SharedPreferencesUtils.IS_OPEN_NEWS_TODAY, System.currentTimeMillis());
        Global.addActivity(this);
    }

    private TopScrollView mScrollView;
    private void init() {
        TopScrollView mScrollView = findViewById(R.id.scroll_view);
        mScrollView.setOnScrollToTopListener(new TopScrollView.OnScrollToTopListener() {
            @Override
            public void onScrollToTop() {
                finish();
            }
        });
        mSearchBar = findViewById(R.id.article_search_bar);
        mSetting = findViewById(R.id.browser_navigation_setting);
        mHomeIcon = findViewById(R.id.search_hot_word);
        mAppSites = findViewById(R.id.browser_app_sites);
        mBrowserHistory = findViewById(R.id.browser_history_view);
        mWebsites = findViewById(R.id.browser_websites_view);
        mHeaderAd = findViewById(R.id.browser_header_ad);
        mWebSitesContaniner = findViewById(R.id.navigation_container);

        mSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Global.addActivity(NavigationActivity.this);
                SchemeUtil.jumpToScheme(NavigationActivity.this, SchemeUtil.BROWSER_SCHEME_PATH_SEARCH);
                needAnim = false;
                finish();
            }
        });
        mHomeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                SchemeUtil.jumpToScheme(NavigationActivity.this, SchemeUtil.BROWSER_SCHEME_PATH_HOME);
            }
        });

        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                needAnim = false;
                SchemeUtil.jumpToScheme(NavigationActivity.this,SchemeUtil.BROWSER_SCHEME_PATH_SETTING);
                finish();
            }
        });
    }

    private void requestData() {
        if (BrowserApplication.getInstance().isChina()) {
            WebsitesHttpRequest websitesHttpRequest = new WebsitesHttpRequest();
            DataResult dataResult = websitesHttpRequest.parseData(SharedPreferencesUtils.getNavigationCache(BrowserInitDataContants.BROWSER_NEW_HEADER), true);
            if (dataResult != null) {
                mAppSites.setData(dataResult.appsites);
                mBrowserHistory.setData(dataResult.browserhistory);
                mWebsites.bindData(dataResult.websites, false);
                setBottomNavigation(dataResult.allWebSite);
            }
        } else {
            setBottomNavigation(RecommendUrlUtil.getGroupSieteEditData(NavigationActivity.this));
        }
    }

    public boolean needAnim = true;

    @Override
    public void finish() {
        super.finish();
        if (needAnim) {
            overridePendingTransition(R.anim.browser_slide_page_start, R.anim.browser_slide_page_exit);
        } else {
            overridePendingTransition(0, 0);
        }
    }

    private void setBottomNavigation(ArrayList<AllWebSiteData> modules) {
        if (CollectionUtils.isEmpty(modules)) {
            return;
        }
        for (int i = 0; i < modules.size(); i++) {
            GroupSitesView groupSitesView = new GroupSitesView(this);
            groupSitesView.setGroupWebsitesData(modules.get(i),false,null);
            mWebSitesContaniner.addView(groupSitesView);
        }
    }

    private void initAd() {
//        if(!BrowserApplication.getInstance().isChina()){
            AdView adView = new AdView(this);
            adView.setAdUnitId("ca-app-pub-7100088665720611/5280355299");
            adView.setAdSize(AdSize.BANNER);

// Replace ad container with new ad view.
            mHeaderAd.removeAllViews();
            mHeaderAd.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
//        }
//        AdLoadListener listener = new AdLoadListener() {
//            @Override
//            public void setView(View view) {
//                if(mHeaderAd != null){
//                    mHeaderAd.removeAllViews();
//                }
//                mHeaderAd.addView(view);
//                mHeaderAd.setVisibility(View.VISIBLE);
//            }
//        };
//        AdConfig adConfig = new AdConfig();
//        adConfig.setAdListener(listener).setHeight(50)
//                .setWidth(ScreenUtils.getScreenWidthDP(this) - 40)
//                .setAdId(AdListFactory.AD_BANNER_NAVIGATION_ID)
//                .setAdType(AdListFactory.AD_BANNER).setAdForbidTime(AdListFactory.AD_FORBID_TIME_TOP);
//        AdListFactory.getInstance().generateAdView(this, adConfig);
    }
}
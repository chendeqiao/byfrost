package com.intelligence.news.news.groupwebsites;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.browser.ui.home.navigation.WebNavigationEditable;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.news.hotword.BrowserInitDataContants;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.websites.WebsitesHttpRequest;
import com.intelligence.news.websites.bean.AllWebSiteData;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;
import java.util.List;

public class GroupSitesEditView extends FrameLayout  {

    private LinearLayout mWebSitesContaniner;
    private WebNavigationEditable webNavigationEditable;
    private List<RecommendUrlEntity> mRecommendData;
    public void setWebNavigationEditable(WebNavigationEditable webNavigationEditable, List<RecommendUrlEntity> data) {
        this.webNavigationEditable = webNavigationEditable;
        mRecommendData = data;
    }

    public GroupSitesEditView(@NonNull Context context) {
        this(context, null, 0);
        init(context);
    }

    public GroupSitesEditView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public GroupSitesEditView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.browser_module_navigation_edit_view, this);
        mWebSitesContaniner = findViewById(R.id.navigation_edit_container);
        requestData();
    }

    public void updateData(List<RecommendUrlEntity> data) {
        if (!CollectionUtils.isEmpty(mAllWebSite)) {
            mRecommendData = data;
            mWebSitesContaniner.removeAllViews();
            setBottomNavigation(filterData(mAllWebSite, mRecommendData));
        }
    }

    private ArrayList<AllWebSiteData> mAllWebSite;

    private void requestData() {
        if (BrowserApplication.getInstance().isChina()) {
            WebsitesHttpRequest websitesHttpRequest = new WebsitesHttpRequest();
            DataResult dataResult = websitesHttpRequest.parseData(SharedPreferencesUtils.getNavigationCache(BrowserInitDataContants.BROWSER_NEW_HEADER), true);
            if (dataResult != null) {
                mAllWebSite = dataResult.allWebSite;
                setBottomNavigation(dataResult.allWebSite);
            }
        } else {
            mAllWebSite = RecommendUrlUtil.getGroupSieteEditData(getContext());
            if (!CollectionUtils.isEmpty(mAllWebSite)) {
                setBottomNavigation(filterData(mAllWebSite, mRecommendData));
            }
        }
    }

    private ArrayList<AllWebSiteData> filterData(ArrayList<AllWebSiteData> modules, List<RecommendUrlEntity> mRecommendData){
        if(CollectionUtils.isEmpty(modules) || CollectionUtils.isEmpty(mRecommendData)){
            return modules;
        }
        for (AllWebSiteData siteData : modules){
            for (WebSiteData webSiteData : siteData.webSites) {
                webSiteData.isEditAdd = false;
                for (RecommendUrlEntity recommendUrlEntity : mRecommendData) {
                    if ((recommendUrlEntity.getUrl() + "").equals(webSiteData.scheme)){
                        webSiteData.isEditAdd = true;
                    }
                }
            }
        }
        return modules;
    }

    private void setBottomNavigation(ArrayList<AllWebSiteData> modules) {
        if (CollectionUtils.isEmpty(modules)) {
            return;
        }
        for (int i = 0; i < modules.size(); i++) {
            AllWebSiteData websites = modules.get(i);
            if(websites  == null || CollectionUtils.isEmpty(websites.webSites)){
                break;
            }
            GroupSitesView groupSitesView = new GroupSitesView(this.getContext());
            if(i == 0){
                groupSitesView.setLineHeight(0);
            }else {
                groupSitesView.setLineHeight(2);
            }
            groupSitesView.setGroupWebsitesData(modules.get(i), true, new WebSitesAdapter.OnWebiSiteItemClickListener() {
                @Override
                public void onWebiSiteItemClick(WebSiteData webSiteData) {
                    if(webNavigationEditable != null){
                        int position = webNavigationEditable.doUrlContained(webSiteData.scheme);
                        if (webSiteData.isEditAdd) {
                            webNavigationEditable.deleteNavigation(position);
                        } else {
                            boolean isSuccuss = webNavigationEditable.addNewNavigation(webSiteData.title, webSiteData.scheme,webSiteData.logo, false);
                            if(!isSuccuss) {
                                webSiteData.isEditAdd = !webSiteData.isEditAdd;
                            }
                        }
                    }
                }
            });
            mWebSitesContaniner.addView(groupSitesView);
        }
    }

}
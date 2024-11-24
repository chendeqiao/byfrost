package com.intelligence.news.news.channel;/*
package com.yunxin.news.channel;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends BaseFragment {
    private static final int AD_LIST_HEADER_HEIGHT = 160;
    private View mRoot;
    private NewsRequest mNewsRequest;
    private NewsListAdapter mNewsListAdapter;
    private ProgressBar mProgressView;
    private RelativeLayout mHeaderAdLayout;
    private List mNewsList;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.browser_news_layout, container, false);
        initView(mRoot);
        initHeadAd();
        obtainData(false);
        return mRoot;
    }

    private void initView(View view) {
        mNewsListAdapter = new NewsListAdapter(getContext());
        vListView = view.findViewById(R.id.browser_news_listview);
        vListView.setDivider(this.getResources().getDrawable(R.drawable.browser_list_item_divider));
        vListView.setDividerHeight(1);
        mProgressView = view.findViewById(R.id.browser_progress_bar);
        vListView.setAdapter(mNewsListAdapter);
        vListView.setLoadMoreCallback(new RefreshableView.LoadMoreCallback() {
            @Override
            public boolean onLoadMore(boolean isUserManual) {
                obtainData(true);
                return false;
            }
        });
        vListView.setPullToRefreshCallback(new RefreshableView.PullToRefreshCallback() {
            @Override
            public boolean onPullToRefresh(boolean isUserManual) {
                return false;
            }
        });
        vListView.setPullRefreshEnabled(false);
    }

    private void initHeadAd(){
        mHeaderAdLayout = new RelativeLayout(getActivity());
        mHeaderAdLayout.setGravity(Gravity.CENTER);
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mHeaderAdLayout.setLayoutParams(layoutParams);

        AdLoadListener listener =  new AdLoadListener() {
            @Override
            public void setView(View view) {
                if(mHeaderAdLayout != null) {
                    mHeaderAdLayout.removeAllViews();
                    mHeaderAdLayout.removeAllViews();
                    mHeaderAdLayout.addView(view);
                    mHeaderAdLayout.setVisibility(View.VISIBLE);
                    vListView.addHeaderView(mHeaderAdLayout);
                }
            }
        };
        mHeaderAdLayout.setVisibility(View.GONE);
        AdConfig adConfig = new AdConfig();
        adConfig.setAdListener(listener).setHeight(AD_LIST_HEADER_HEIGHT)
                .setWidth(ScreenUtils.getScreenWidthDP(getActivity()) - 20)
                .setAdId(AdListFactory.AD_BANNER_LIST_HEADER_ID)
                .setAdType(AdListFactory.AD_BANNER)
                .setAdParentView(mHeaderAdLayout)
                .setAdForbidTime(AdListFactory.AD_FORBID_TIME_TOP);
        AdListFactory.getInstance().generateAdView(getActivity(), adConfig);
    }

    private void obtainData(final boolean isLoadMore){
        mNewsRequest = new NewsRequest();
        mNewsRequest.getFeedStream(1, new ResponseCallback() {
            @Override
            public void onSuccess(Object responseObj) {
                mProgressView.setVisibility(View.GONE);
                if(responseObj == null || !(responseObj instanceof DataResult)){
                    return;
                }
                if(isLoadMore){
                    mNewsList.addAll(((DataResult)responseObj).newsList);
                }else {
                    mNewsList = ((DataResult)responseObj).newsList;
                }
                insertOperator(mNewsList);
                inserAD(mNewsList);
                mNewsListAdapter.setList(mNewsList);
                vListView.onLoadMoreComplete();
            }

            @Override
            public void onFailure(OkHttpException failuer) {
                mProgressView.setVisibility(View.GONE);
            }
        });
    }

    private InsertHelper mDataInsertHelper = new InsertHelper();

    ArrayList mInsertList = new ArrayList();
    ArrayList mAdList = new ArrayList();

    private void insertOperator(List list) {
        list.removeAll(mInsertList);
        mInsertList.clear();
        NewsData newsData = new NewsData();
        newsData.cardType = CardFactory.CARD_HOTWORD;
        newsData.position = 3;
        newsData.mediaType = NetConfig.PAGE_DOUYIN;

        NewsData newsData2 = new NewsData();
        newsData2.cardType = CardFactory.CARD_HOTWORD;
        newsData2.position = 10;
        newsData2.mediaType = NetConfig.PAGE_BAIDU;
        mInsertList.add(newsData);
        mInsertList.add(newsData2);
        mDataInsertHelper.insert(mInsertList, list);
    }

    private void inserAD(List mNewsList) {
        mNewsList.removeAll(mAdList);
        mAdList.clear();
        NewsData newsData = new NewsData();
        newsData.cardType = CardFactory.CARD_ADVERT;
        newsData.position = 3;
        newsData.mediaType = NetConfig.PAGE_DOUYIN;

        NewsData newsData2 = new NewsData();
        newsData2.cardType = CardFactory.CARD_ADVERT;
        newsData2.position = 10;
        newsData2.mediaType = NetConfig.PAGE_BAIDU;
        mAdList.add(newsData);
        mAdList.add(newsData2);
        mDataInsertHelper.insert(mAdList, mNewsList);
    }
}*/

package com.intelligence.news.hotword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.baseUi.BaseFragment;
import com.intelligence.commonlib.net.OkHttpException;
import com.intelligence.commonlib.net.ResponseCallback;
import com.intelligence.news.news.cards.HotWordAdapter;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.news.mode.HotWordData;

import java.util.ArrayList;
import java.util.List;

public class HotWordFragment extends BaseFragment {
    private boolean mIsVisibleToUser;
    private RecyclerView mRecycleView;
    private HotWordAdapter mhotWordAdapter;
    private View.OnClickListener mListener;
    private HotWordHttpRequest hotWordHttpRequest;
    private View mRefresh;

    private View mRoot;
    private int mPageType;

    public HotWordFragment setType(int pageType){
        mPageType = pageType;
        return this;
    }

    public void setClickListener(View.OnClickListener listener){
        mListener = listener;
        if(mhotWordAdapter != null){
            mhotWordAdapter.setClickListener(listener);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.browser_hot_word_layout, container, false);
        initView(mRoot);
        return mRoot;
    }

    private void initView(View view) {
        mRecycleView = view.findViewById(R.id.hotword_recyclerview);
        mRefresh = view.findViewById(R.id.hot_word_refresh);
//            mRefresh.setOnClickListener(this);
        mhotWordAdapter = new HotWordAdapter(getContext());
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleView.setAdapter(mhotWordAdapter);
        if(mListener != null) {
            mhotWordAdapter.setClickListener(mListener);
        }
        requestData(false);
    }

    private void requestData(final boolean isFromNet) {
        hotWordHttpRequest = new HotWordHttpRequest();
        hotWordHttpRequest.getHotWordApi(mPageType, isFromNet, new ResponseCallback<DataResult>() {
            @Override
            public void onSuccess(DataResult responseObj) {
                if (responseObj == null) {
                    responseObj = HotWordHttpRequest.getData(mPageType);
                }
                insertAd(responseObj.newsList);
                mhotWordAdapter.setData(responseObj.newsList);
                if(!isFromNet) {
                    requestData(true);
                }
            }

            @Override
            public void onFailure(OkHttpException failuer) {
                if (!isFromNet) {
                    mhotWordAdapter.setData(HotWordHttpRequest.getData(mPageType).newsList);
                }
            }
        });
    }

    private void insertAd(List<HotWordData> targetList){
        List<HotWordData> insertList = new ArrayList<>();
//        for (int i = 0;i<5;i++){
            HotWordData hotWordData = new HotWordData();
//            hotWordData.position = (i+1)*10;
            hotWordData.dataType = HotWordData.TYPE_AD;
//            if(i==0) {
//                hotWordData.id = AdListFactory.AD_BANNER_HOTWORD_10;
//            }else if(i == 1){
//                hotWordData.id = AdListFactory.AD_BANNER_HOTWORD_20;
//            }else if(i == 2){
//                hotWordData.id = AdListFactory.AD_BANNER_HOTWORD_30;
//            }else if(i == 3){
//                hotWordData.id = AdListFactory.AD_BANNER_HOTWORD_40;
//            }else if(i == 4){
//                hotWordData.id = AdListFactory.AD_BANNER_HOTWORD_50;
//            }
        targetList.add(hotWordData);
//        }

//        for (HotWordData entity : insertList) {
//            if (entity == null) {
//                continue;
//            }
//            int position =entity.position - 1;
//            if (position < 0 || position >= targetList.size()) {
//                continue;
//            }
//            targetList.add(position, entity);
//        }
    }
}

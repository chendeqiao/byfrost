package com.intelligence.browser.ui.home.navigation;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.ui.adapter.RecommendAdapter;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.commonlib.tools.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class RecommendPagerAdapter extends PagerAdapter implements DatabaseManager.DataChangeListener<RecommendUrlEntity> {

    private List<RecommendUrlEntity> mDataList;
    private ArrayList<RecommendPagerView> mPageList = new ArrayList<>();
    private Activity mActivity;
    private RecommendAdapter.AdapterItemListener mListener;

    public RecommendPagerAdapter(Activity activity, List<RecommendUrlEntity> list, RecommendAdapter.AdapterItemListener listener) {
        mActivity = activity;
        mListener = listener;
        mDataList = list;
        updateData(list);
    }

    public void updateData(List<RecommendUrlEntity> list) {
        if (list == null) {
            return;
        }
        if(mPageList.size()==0){
            RecommendPagerView recommendPagerView1 = new RecommendPagerView(mActivity, mListener);
            mPageList.add(recommendPagerView1);

            RecommendPagerView recommendPagerView2 = new RecommendPagerView(mActivity, mListener);
            mPageList.add(recommendPagerView2);
        }
        mDataList = list;
        if (list.size() < WebNavigationView.RECMMEND_ITEM_COUNT) {
            notifyDataSetChanged();
            mPageList.get(0).updateRecommendData(mDataList.subList(0, list.size()));
        } else {
            notifyDataSetChanged();
            mPageList.get(0).post(new Runnable() {
                @Override
                public void run() {
                    mPageList.get(0).updateRecommendData(mDataList.subList(0, WebNavigationView.RECMMEND_ITEM_COUNT));
                    mPageList.get(1).updateRecommendData(mDataList.subList(WebNavigationView.RECMMEND_ITEM_COUNT, mDataList.size()));
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getCount() {
        return mPageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mPageList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mPageList.get(position));
        return mPageList.get(position);
    }

    public boolean isDeleteMode() {
        if(CollectionUtils.isEmpty(mPageList)){
            return false;
        }
        for (RecommendPagerView pagerView : mPageList) {
            if (pagerView.isDeleteMode()) {
                return true;
            }
        }
        return false;
    }

    public void setDeleteMode(boolean isDelete) {
        if(CollectionUtils.isEmpty(mPageList)){
            return;
        }
        for (RecommendPagerView pagerView : mPageList) {
            pagerView.setDeleteMode(isDelete);
        }
    }

    public void exitEditState() {
        if(CollectionUtils.isEmpty(mPageList)){
            return;
        }
        for (RecommendPagerView pagerView : mPageList) {
            pagerView.exitEditState();
        }
    }

    @Override
    public void onUpdateToDB(RecommendUrlEntity entity) {
        if(CollectionUtils.isEmpty(mPageList)){
            return;
        }
        for (RecommendPagerView pagerView : mPageList) {
            pagerView.onUpdateToDB(entity);
        }
    }

    @Override
    public void onInsertToDB(RecommendUrlEntity entity) {
        if(CollectionUtils.isEmpty(mPageList)){
            return;
        }
        for (RecommendPagerView pagerView : mPageList) {
            pagerView.onInsertToDB(entity);
        }
    }

    @Override
    public void onDeleteToDB(RecommendUrlEntity entity) {
        if(CollectionUtils.isEmpty(mPageList)){
            return;
        }
        for (RecommendPagerView pagerView : mPageList) {
            pagerView.onDeleteToDB(entity);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if(CollectionUtils.isEmpty(mPageList)){
            return;
        }
        for (RecommendPagerView pagerView : mPageList) {
            pagerView.notifyDataSetChanged();
        }
    }
}


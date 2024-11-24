package com.intelligence.browser.ui.home.navigation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.intelligence.browser.R;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.news.news.groupwebsites.GroupSitesEditView;

import java.util.List;

@SuppressLint("ValidFragment")
public class AddNewNavigationPage extends Fragment implements  View.OnClickListener {

    private View mRootView;

    private GroupSitesEditView mGroupSitesEditView;
    private List<RecommendUrlEntity> mDataList;
    private ScrollView mScrollView;
    private WebNavigationEditable mNavigationEditable;
    public AddNewNavigationPage(){
        super();
    }

    @SuppressLint("ValidFragment")
    public AddNewNavigationPage(WebNavigationEditable navigationEditable, List<RecommendUrlEntity> data) {
        super();
        mDataList = data;
        mNavigationEditable = navigationEditable;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.browser_edit_navigation_layout, container, false);
        initView(mRootView);
        return mRootView;
    }

    private void initView(View rootView) {

        mGroupSitesEditView = rootView.findViewById(R.id.browser_navigation_edit_layout);
        mGroupSitesEditView.setWebNavigationEditable(mNavigationEditable,mDataList);

    }

    public void updateGroupSitesData(WebNavigationEditable webNavigationEditable, List<RecommendUrlEntity> data){
        if(mScrollView != null){
            mScrollView.scrollTo(0,0);
        }
        if(mGroupSitesEditView != null){
//            mGroupSitesEditView.updateData(webNavigationEditable,data);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                //doNothing
        }
    }

    public int getHeight() {
        return mRootView == null ? 0 : mRootView.getHeight();
    }

    public void onKeyBoardShow() {
        //do nothing
    }

    public void onKeyBoardHint() {
        //do nothing
    }

    public void updateData(List<RecommendUrlEntity> data){
        if(mGroupSitesEditView != null){
            mGroupSitesEditView.updateData(data);
        }
    }

    public interface OnFocusChangeListener {
        void onFocusChange();
    }

}

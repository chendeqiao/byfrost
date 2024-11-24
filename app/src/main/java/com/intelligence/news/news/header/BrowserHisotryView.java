package com.intelligence.news.news.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;

public class BrowserHisotryView extends FrameLayout {
    private BrowserHistoryAdapter mCarHistoryAdapter;
    private RecyclerView mRecleView;
    private ArrayList<WebSiteData> mCarHisotryData;

    public BrowserHisotryView(@NonNull Context context) {
        this(context, null, 0);
    }

    public BrowserHisotryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrowserHisotryView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_car_browser_view, this);
        initView(view);
    }

    private void initView(View view) {
        mRecleView = findViewById(R.id.car_history_recycleview);
        mRecleView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        mCarHistoryAdapter = new BrowserHistoryAdapter(getContext());
        mRecleView.setAdapter(mCarHistoryAdapter);
    }

    public void setHeightMargin(int marginTop, int marginBottom) {
//        MarginLayoutParams params = (MarginLayoutParams) mRecleView.getLayoutParams();
//        params.topMargin = marginTop;
//        params.bottomMargin = marginBottom;
//        mRecleView.setLayoutParams(params);
    }

    public void setData(ArrayList<WebSiteData> list) {
        if(CollectionUtils.isEmpty(list)){
            setVisibility(GONE);
            return;
        }
        mCarHisotryData = list;
        mCarHistoryAdapter.setData(list);
    }

    public ArrayList<WebSiteData> getCarHistoryData(){
        return mCarHisotryData;
    }
}
package com.intelligence.news.news.header.website;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.news.news.groupwebsites.WebSitesAdapter;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.List;

public class SugarPageView extends FrameLayout {
    private List<WebSiteData> mDataList;
    private RecyclerView mWebsiteRecycleView;
    private WebSitesAdapter mWebSitesAdapter;


    public SugarPageView(@NonNull Context context) {
        this(context, null, 0);
    }

    public SugarPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SugarPageView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.layout_sugar_page_view, this);
        initView(view);
    }

    private void initView(View view) {
        mWebsiteRecycleView = view.findViewById(R.id.browser_websites_recyclerview);

        mWebsiteRecycleView.setLayoutManager(new GridLayoutManager(getContext(), 5) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mWebSitesAdapter = new WebSitesAdapter(getContext(),null);
        mWebsiteRecycleView.setAdapter(mWebSitesAdapter);
    }


    public void setData(List<WebSiteData> list,boolean isEdit) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        mWebSitesAdapter.setWebsites(list,isEdit);
    }
}
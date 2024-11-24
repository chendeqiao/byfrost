package com.intelligence.news.news.groupwebsites;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.news.websites.bean.AllWebSiteData;
import com.intelligence.news.websites.bean.WebSiteData;

public class GroupSitesView extends FrameLayout implements WebSitesAdapter.OnWebiSiteItemClickListener {
    private WebSitesAdapter mWebSitesAdapter;
    private WebSitesAdapter mWebLableAdapter;
    private Context mContext;
    public RecyclerView mGroupWebsites;
    public RecyclerView mGroupLables;
    public TextView mGroupTitle;
    private View mWebsitesLine;

    public GroupSitesView(@NonNull Context context) {
        this(context, null, 0);
    }

    public GroupSitesView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupSitesView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(mContext);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.browser_module_navigation_view, this);
        mGroupWebsites = findViewById(R.id.browser_websites_recyclerview);
        mWebsitesLine = findViewById(R.id.navigation_edit_line);
        mGroupLables = findViewById(R.id.browser_lable_recyclerview);
        mGroupTitle = findViewById(R.id.sugar_module_name);

        mWebSitesAdapter = new WebSitesAdapter(mContext,this);
        mGroupWebsites.setAdapter(mWebSitesAdapter);
        mGroupWebsites.setLayoutManager(new GridLayoutManager(mContext, 5) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        mWebLableAdapter = new WebSitesAdapter(mContext,this);
        mGroupLables.setAdapter(mWebLableAdapter);
        mGroupLables.setLayoutManager(new GridLayoutManager(mContext, 5) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }

    public void setLineHeight(int size){
        if(mWebsitesLine != null) {
            mWebsitesLine.getLayoutParams().height = ScreenUtils.dpToPxInt(getContext(),size);
            mWebsitesLine.requestLayout();
        }
    }
    private WebSitesAdapter.OnWebiSiteItemClickListener mOnWebiSiteItemClickListener;

    public void setGroupWebsitesData(AllWebSiteData websites,boolean isEditNav,WebSitesAdapter.OnWebiSiteItemClickListener onWebiSiteItemClickListener){
        mGroupTitle.setText(websites.title);
        if(!CollectionUtils.isEmpty(websites.webSites)){
            mWebSitesAdapter.setWebsites(websites.webSites,isEditNav);
            mGroupWebsites.setVisibility(VISIBLE);
        }else {
            mGroupWebsites.setVisibility(GONE);
        }

        if(!CollectionUtils.isEmpty(websites.lables) && !isEditNav){
            mWebLableAdapter.setWebsites(websites.lables,false,isEditNav);
            mGroupLables.setVisibility(VISIBLE);
        }
        mOnWebiSiteItemClickListener = onWebiSiteItemClickListener;
    }

    @Override
    public void onWebiSiteItemClick(WebSiteData webSiteData) {
        if(mOnWebiSiteItemClickListener != null){
            mOnWebiSiteItemClickListener.onWebiSiteItemClick(webSiteData);
        }
    }
}
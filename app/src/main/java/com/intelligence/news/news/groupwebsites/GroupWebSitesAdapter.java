package com.intelligence.news.news.groupwebsites;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.news.websites.bean.AllWebSiteData;

import java.util.ArrayList;
import java.util.List;

public class GroupWebSitesAdapter extends RecyclerView.Adapter<GroupWebSitesAdapter.GroupWebSitesHolder> {
    private ArrayList<AllWebSiteData> mWebsites;
    private Context mContext;

    public GroupWebSitesAdapter(Context context) {
        mWebsites = new ArrayList<>();
        mContext = context;
    }

    private boolean mIsEdit;
    public void setWebsites(ArrayList<AllWebSiteData> websites,boolean isEdit){
        this.mWebsites = websites;
        mIsEdit = isEdit;
        notifyDataSetChanged();
    }
    @Override
    public GroupWebSitesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.browser_module_navigation_view, parent, false);
        return new GroupWebSitesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupWebSitesHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(final GroupWebSitesHolder holder, int position) {
        final AllWebSiteData groupData = mWebsites.get(position);
        holder.mGroupTitle.setText(groupData.title);

        if (!CollectionUtils.isEmpty(groupData.webSites)) {
            holder.mGroupWebsites.setVisibility(View.VISIBLE);
            WebSitesAdapter webSitesAdapter = (WebSitesAdapter) holder.mGroupWebsites.getAdapter();
            if (webSitesAdapter == null) {
                webSitesAdapter = new WebSitesAdapter(mContext,null);
                holder.mGroupWebsites.setAdapter(webSitesAdapter);
                holder.mGroupWebsites.setLayoutManager(new GridLayoutManager(mContext, 5) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                });
            }
            webSitesAdapter.setWebsites(groupData.webSites,mIsEdit);
        } else {
            holder.mGroupWebsites.setVisibility(View.GONE);
        }

        if (!CollectionUtils.isEmpty(groupData.lables)) {
            holder.mGroupLables.setVisibility(View.VISIBLE);
            WebSitesAdapter webSitesAdapter = (WebSitesAdapter) holder.mGroupLables.getAdapter();
            if (webSitesAdapter == null) {
                webSitesAdapter = new WebSitesAdapter(mContext,null);
                holder.mGroupLables.setAdapter(webSitesAdapter);
                holder.mGroupLables.setLayoutManager(new GridLayoutManager(mContext, 5) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                });
            }
            webSitesAdapter.setWebsites(groupData.lables,mIsEdit);
        } else {
            holder.mGroupLables.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(CollectionUtils.isEmpty(mWebsites)){
            return 0;
        }
        return mWebsites.size();
    }

    public class GroupWebSitesHolder extends RecyclerView.ViewHolder {
        public RecyclerView mGroupWebsites;
        public RecyclerView mGroupLables;
        public TextView mGroupTitle;
        public View mItemView;

        GroupWebSitesHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mGroupWebsites = itemView.findViewById(R.id.browser_websites_recyclerview);
            mGroupLables = itemView.findViewById(R.id.browser_lable_recyclerview);
            mGroupTitle = itemView.findViewById(R.id.sugar_module_name);
        }
    }
}
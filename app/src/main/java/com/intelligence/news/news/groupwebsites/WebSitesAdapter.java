package com.intelligence.news.news.groupwebsites;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;
import java.util.List;

public class WebSitesAdapter extends RecyclerView.Adapter<WebSitesAdapter.WebsiteViewHolder> {
    private List<WebSiteData> mWebsites;
    private Context mContext;
    private boolean mIsShowLogo = true;

    private OnWebiSiteItemClickListener mOnWebiSiteItemClickListener;
    public WebSitesAdapter(Context context,OnWebiSiteItemClickListener onWebiSiteItemClickListener) {
        mWebsites = new ArrayList<>();
        mContext = context;
        mOnWebiSiteItemClickListener = onWebiSiteItemClickListener;
    }

    public void setWebsites(List<WebSiteData> websites, boolean isShowLogo,boolean isEdit) {
        this.mWebsites = websites;
        this.mIsShowLogo = isShowLogo;
        notifyDataSetChanged();
    }

    private boolean mIsEdit;
    public void setWebsites(List<WebSiteData> websites,boolean isEdit) {
        this.mWebsites = websites;
        mIsEdit = isEdit;
        notifyDataSetChanged();
    }

    @Override
    public WebSitesAdapter.WebsiteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_sugar_item, parent, false);
        return new WebSitesAdapter.WebsiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WebSitesAdapter.WebsiteViewHolder holder, int position) {
        final WebSiteData website = mWebsites.get(position);
        holder.mItemTitle.setText(website.title);
        if(!TextUtils.isEmpty(website.logo)) {
            int resID = holder.mItemTitle.getContext().getResources().getIdentifier(website.logo,
                    "drawable", holder.mItemTitle.getContext().getPackageName());
            holder.mLogo.setImageResource(resID);
            holder.mLogo.setBackground(null);
        }
        holder.mItemAdd.setVisibility(mIsEdit ? View.VISIBLE : View.GONE);
        if (website.isEditAdd) {
            holder.mItemAdd.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_navigation_edit_icon_succuss));
        } else {
            holder.mItemAdd.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_navigation_edit_icon));
        }
        holder.mItemTitle.setTextSize(mIsShowLogo ? 12 : 15);
        holder.mLogo.setVisibility(mIsShowLogo ? View.VISIBLE : View.GONE);
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsEdit){
                    if (mOnWebiSiteItemClickListener != null) {
                        mOnWebiSiteItemClickListener.onWebiSiteItemClick(website);
                    }
                    website.isEditAdd = !website.isEditAdd;
                    notifyDataSetChanged();
                }else {
                    SchemeUtil.openNewWebView(mContext, website.scheme);
                    Global.clearActivity();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isEmpty(mWebsites)) {
            return 0;
        }
        return mWebsites.size();
    }

    public class WebsiteViewHolder extends RecyclerView.ViewHolder {
        public ImageView mLogo;
        public TextView mItemTitle;
        public ImageView mItemAdd;
        public View mItemView;

        WebsiteViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mLogo = itemView.findViewById(R.id.sugar_page_icon);
            mItemTitle = itemView.findViewById(R.id.sugar_page_title);
            mItemAdd = itemView.findViewById(R.id.sugar_page_add);
        }
    }

    public interface OnWebiSiteItemClickListener{
        void onWebiSiteItemClick(WebSiteData webSiteData);
    }
}


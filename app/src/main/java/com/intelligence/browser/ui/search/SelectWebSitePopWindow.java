package com.intelligence.browser.ui.search;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.R;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.news.websites.bean.AllWebSiteData;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;
import java.util.List;

public class SelectWebSitePopWindow implements AdapterView.OnItemClickListener {
    private View mView;
    private PopupWindow mPopWindow;
    private Context mContext;
    private RecyclerView mlistView;
    private SelectWebSitesAdapter mWebSitesAdapter;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    private BaseUi.SelectWebSiteListener mSelectSearchEngineListener;
    private WebSiteData mWebSiteData;

    public SelectWebSitePopWindow(Context context, WebSiteData webSiteData, BaseUi.SelectWebSiteListener selectSearchEngineListener) {
        mContext = context;
        mSelectSearchEngineListener = selectSearchEngineListener;
        mWebSiteData = webSiteData;
        initWindow();
    }

    public void initWindow() {
        this.mView = View.inflate(mContext, R.layout.browser_select_site_popwindow, null);
        mlistView = mView.findViewById(R.id.browser_select_websites_recyclerview);
        mWebSitesAdapter = new SelectWebSitesAdapter(mlistView.getContext(), new BaseUi.SelectWebSiteListener() {
            @Override
            public void selectWebSite(WebSiteData webSiteData) {
                if (mSelectSearchEngineListener != null) {
                    mSelectSearchEngineListener.selectWebSite(webSiteData);
                }
            }
        });
        mlistView.setAdapter(mWebSitesAdapter);
        mlistView.setLayoutManager(new GridLayoutManager(mContext, 5) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });

        mPopWindow = new PopupWindow(mView, (int) (ScreenUtils.getScreenWidth(mlistView.getContext())), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());

        ArrayList<AllWebSiteData> arrayList = RecommendUrlUtil.getGroupSitesData(mView.getContext(), mWebSiteData.logo);
        if (!CollectionUtils.isEmpty(arrayList)) {
            mWebSitesAdapter.setWebsites(arrayList.get(0).webSites, true, false);
        }
    }

    public boolean isShow() {
        if (mPopWindow != null) {
            return mPopWindow.isShowing();
        }
        return false;
    }

    public void show(View parentView) {
        int[] location = new int[2];
        parentView.getLocationOnScreen(location);
        mPopWindow.setAnimationStyle(R.style.select_website_popwindow_down);
        mPopWindow.showAsDropDown(parentView,  0, -ScreenUtils.dpToPxInt(mView.getContext(), 10),Gravity.CENTER_HORIZONTAL);
    }

    public void dismiss() {
        mPopWindow.dismiss();
    }

    public class SelectWebSitesAdapter extends RecyclerView.Adapter<SelectWebSitesAdapter.WebsiteViewHolder> {
        private List<WebSiteData> mWebsites;
        private Context mContext;
        private boolean mIsShowLogo = true;

        private BaseUi.SelectWebSiteListener mOnWebiSiteItemClickListener;

        public SelectWebSitesAdapter(Context context, BaseUi.SelectWebSiteListener onWebiSiteItemClickListener) {
            mWebsites = new ArrayList<>();
            mContext = context;
            mOnWebiSiteItemClickListener = onWebiSiteItemClickListener;
        }

        public void setWebsites(List<WebSiteData> websites, boolean isShowLogo, boolean isEdit) {
            this.mWebsites = websites;
            this.mIsShowLogo = isShowLogo;
            notifyDataSetChanged();
        }


        public void setWebsites(List<WebSiteData> websites, boolean isEdit) {
            this.mWebsites = websites;
            notifyDataSetChanged();
        }

        @Override
        public SelectWebSitesAdapter.WebsiteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_tools_item, parent, false);
            return new SelectWebSitesAdapter.WebsiteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SelectWebSitesAdapter.WebsiteViewHolder holder, int position) {
            final WebSiteData website = mWebsites.get(position);
            holder.mItemTitle.setText(website.title);
            try {
                if (!TextUtils.isEmpty(website.logo)) {
                    int resID = holder.mItemTitle.getContext().getResources().getIdentifier(website.logo,
                            "drawable", holder.mItemTitle.getContext().getPackageName());
                    holder.mLogo.setImageBitmap(ImageUtils.drawableToBitmap(holder.mItemTitle.getContext(), resID));
                }
            }catch (Exception e){
            }
            holder.mItemTitle.setTextSize(mIsShowLogo ? 12 : 15);
            holder.mLogo.setVisibility(mIsShowLogo ? View.VISIBLE : View.GONE);
            holder.mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SchemeUtil.openNewWebView(mContext, website.scheme);
                    Global.clearActivity();
                    dismiss();
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
            public RoundImageView mLogo;
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
    }

}

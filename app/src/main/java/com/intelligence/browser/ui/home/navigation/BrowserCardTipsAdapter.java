package com.intelligence.browser.ui.home.navigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.data.TipsCardInfo;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.List;
import java.util.Random;

public class BrowserCardTipsAdapter extends
        RecyclerView.Adapter<BrowserCardTipsAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<TipsCardInfo> mDataList;

    public BrowserCardTipsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setData(List<TipsCardInfo> data) {
        this.mDataList = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View arg0) {
            super(arg0);
        }
        View vItem;
        TextView vTitle;
        ImageView vColse;
        View vOpen;
        View vNativeLayout;
        View vDefaultLayout;
        View vAdLayout;
    }

    @Override
    public int getItemCount() {
        if(mDataList !=null) {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.browser_tips_card_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.vItem = view;
        viewHolder.vColse = view.findViewById(R.id.browser_tips_close);
        viewHolder.vTitle = view.findViewById(R.id.browser_tips_title);
        viewHolder.vOpen = view.findViewById(R.id.browser_tips_title_open);
        viewHolder.vNativeLayout = view.findViewById(R.id.browser_native_card_layout);
        viewHolder.vDefaultLayout = view.findViewById(R.id.browser_default_browser_layout);
        viewHolder.vAdLayout = view.findViewById(R.id.browser_ad_card_layout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if(mDataList == null){
            return;
        }
        boolean isSingleCard = mDataList.size()<=1;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)viewHolder.itemView.getLayoutParams();
        params.width = ScreenUtils.getScreenWidth(viewHolder.itemView.getContext()) - ScreenUtils.dpToPxInt(viewHolder.itemView.getContext(), isSingleCard ? 40 : 60);
        params.setMarginEnd(ScreenUtils.dpToPxInt(viewHolder.itemView.getContext(), isSingleCard || mDataList.size() - 1 == i ? 20 : 0));
        viewHolder.itemView.setLayoutParams(params);

        final TipsCardInfo tipsCardInfo = mDataList.get(i);
        if (tipsCardInfo.isAd()) {
            viewHolder.vAdLayout.setVisibility(View.VISIBLE);
            viewHolder.vNativeLayout.setVisibility(View.GONE);
        } else {
            if (tipsCardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_DEFAULT_BROWSER) {
                viewHolder.vAdLayout.setVisibility(View.GONE);
                viewHolder.vNativeLayout.setVisibility(View.GONE);
                viewHolder.vDefaultLayout.setVisibility(View.VISIBLE);
            } else {
                viewHolder.vAdLayout.setVisibility(View.GONE);
                viewHolder.vNativeLayout.setVisibility(View.VISIBLE);
                viewHolder.vDefaultLayout.setVisibility(View.GONE);
            }
        }

        viewHolder.vColse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnCardTipsClickListener != null){
                    mOnCardTipsClickListener.onCloseCard(tipsCardInfo);
                }
            }
        });
        viewHolder.vItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnCardTipsClickListener != null){
                    mOnCardTipsClickListener.onOpenCard(tipsCardInfo);
                }
            }
        });
        viewHolder.vOpen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnCardTipsClickListener != null){
                    mOnCardTipsClickListener.onOpenCard(tipsCardInfo);
                }
            }
        });
    }

    private OnCardTipsClickListener mOnCardTipsClickListener;

    public void setOnCardTipsClickListener(OnCardTipsClickListener onCardTipsClickListener) {
        mOnCardTipsClickListener = onCardTipsClickListener;
    }

    public interface OnCardTipsClickListener {
        void onCloseCard(TipsCardInfo cardInfo);

        void onOpenCard(TipsCardInfo cardInfo);
    }
}

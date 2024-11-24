package com.intelligence.news.news.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.news.news.mode.HotWordData;

import java.util.ArrayList;

public class HotWordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View
        .OnClickListener {
    private ArrayList<HotWordData> mHotwordlist = new ArrayList<>();
    private final LayoutInflater mLayoutInflater;
    private View.OnClickListener mListener;
    private Context mContext;
    private boolean mIsShowDivider;

    public HotWordAdapter(Context context) {
        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HotWordData.TYPE_AD) {
            View view = mLayoutInflater.inflate(R.layout.browser_hotword_ad, parent, false);
            return new HotWordAdapter.HotWordAdViewHolder(view);
        } else {
            View view = mLayoutInflater.inflate(R.layout.item_hotword, parent, false);
            return new HotWordAdapter.HotWordViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mHotwordlist.get(position).dataType;
    }

    public void setClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public void setIsShowDivider(boolean isShowDivider) {
        mIsShowDivider = isShowDivider;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        HotWordData hotWordData = mHotwordlist.get(position);
        if (viewHolder instanceof HotWordViewHolder) {
            HotWordViewHolder holder = (HotWordViewHolder) viewHolder;
            holder.mTitle.setText(hotWordData.hotword);
            ViewGroup.LayoutParams layoutParams =    holder.mTitle.getLayoutParams();
            layoutParams.width = 0;
            if (layoutParams != null) {
                holder.mTitle.setLayoutParams(layoutParams);
            }
            int hotwordnum = StringUtil.getInt(hotWordData.hotwordnum, 0);
            if (hotwordnum > 0) {
                holder.mBrowserNumber.setVisibility(View.VISIBLE);
                holder.mBrowserNumber.setText(hotwordnum / 10000 + "万");
                holder.mTrend.setVisibility(View.GONE);
                holder.mTrend.setImageDrawable(mContext.getResources().getDrawable(hotWordData.trend ? R.drawable.browser_hot_word_up : R.drawable.browser_hot_word_down));
            } else {
                holder.mTrend.setVisibility(View.VISIBLE);
                holder.mBrowserNumber.setVisibility(View.GONE);
                holder.mTrend.setImageDrawable(mContext.getResources().getDrawable(hotWordData.trend ? R.drawable.browser_hot_word_up : R.drawable.browser_hot_word_down));

            }

            holder.mIndexIcon.setVisibility(View.GONE);
            holder.mIndex.setVisibility(View.GONE);
            if (position == 0) {
                holder.mIndexIcon.setVisibility(View.VISIBLE);
                holder.mIndexIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_hotword_frist));
            } else if (position == 1) {
                holder.mIndexIcon.setVisibility(View.VISIBLE);
                holder.mIndexIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_hotword_secound));
            } else if (position == 2) {
                holder.mIndexIcon.setVisibility(View.VISIBLE);
                holder.mIndexIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_hotword_third));
            } else {
                holder.mIndex.setText((position + 1) + "");
                holder.mIndex.setVisibility(View.VISIBLE);
                holder.mIndex.setTextColor(mContext.getResources().getColor(R.color.browser_tips_color));
            }
            holder.mRootView.setTag(hotWordData);
            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getTag() != null) {
                        if (mListener != null) {
                            mListener.onClick(view);
                        }
                    }
                }
            });
            if (hotWordData.labletype == HotWordData.LABLE_TYPE_NEW) {
                holder.mRedtips.setVisibility(View.VISIBLE);
                holder.mRedtips.setText("新");
                holder.mRedtips.setBackground(mContext.getResources().getDrawable(R.drawable.browser_hot_label_new));
//                holder.mRedtips.setBadgeBackgroundColor(R.color.web_icon_background4);
            } else if (hotWordData.labletype == HotWordData.LABLE_TYPE_RECOMEND) {
                holder.mRedtips.setVisibility(View.VISIBLE);
                holder.mRedtips.setText("荐");
                holder.mRedtips.setBackground(mContext.getResources().getDrawable(R.drawable.browser_hot_label_recomend));
            } else if (hotWordData.labletype == HotWordData.LABLE_TYPE_HOT) {
                holder.mRedtips.setVisibility(View.VISIBLE);
                holder.mRedtips.setText("热");
                holder.mRedtips.setBackground(mContext.getResources().getDrawable(R.drawable.browser_hot_label_hot));
            } else {
                holder.mRedtips.setBackground(null);
                holder.mRedtips.setVisibility(View.GONE);
            }
            holder.mDivider.setVisibility(mIsShowDivider ? View.VISIBLE : View.GONE);
            holder.mRedtips.invalidate();
        } else if (viewHolder instanceof HotWordAdViewHolder) {
            HotWordAdViewHolder hotWordAdViewHolder = (HotWordAdViewHolder) viewHolder;
            initAd(hotWordData, hotWordAdViewHolder.mAdLayout);
        }
    }

    private void initAd(HotWordData hotWordData, final FrameLayout mHeaderAd) {
//        AdLoadListener listener = new AdLoadListener() {
//            @Override
//            public void setView(View view) {
//                if(mHeaderAd != null){
//                    mHeaderAd.removeAllViews();
//                }
//                mHeaderAd.addView(view);
//                mHeaderAd.setVisibility(View.VISIBLE);
//            }
//        };
//        if(mHeaderAd.getChildCount()>0){
//            return;
//        }
//        AdConfig adConfig = new AdConfig();
//        adConfig.setAdListener(listener).setHeight(50)
//                .setWidth(ScreenUtils.getScreenWidthDP(mContext) - 40)
//                .setAdId(hotWordData.id)
//                .setAdParentView(mHeaderAd)
//                .setAdType(AdListFactory.AD_BANNER).setAdForbidTime(AdListFactory.AD_FORBID_TIME_TOP);
//        AdListFactory.getInstance().generateAdView((Activity) mContext, adConfig);
    }

    @Override
    public int getItemCount() {
        return mHotwordlist.size();
    }

    @Override
    public void onClick(View view) {
    }

    public void setData(ArrayList hotwordlist) {
        if (CollectionUtils.isEmpty(hotwordlist)) {
            return;
        }
        mHotwordlist = hotwordlist;
        notifyDataSetChanged();
    }

    static class HotWordViewHolder extends RecyclerView.ViewHolder {
        private TextView mIndex;
        private TextView mTitle;
        private TextView mBrowserNumber;
        private TextView mRedtips;
        private View mRootView;
        private ImageView mTrend;
        private ImageView mIndexIcon;
        private View mDivider;

        public HotWordViewHolder(View itemView) {
            super(itemView);
            mIndex = itemView.findViewById(R.id.hotword_index);
            mRootView = itemView.findViewById(R.id.hotword_item);
            mRedtips = itemView.findViewById(R.id.hotword_redtips);
            mTitle = itemView.findViewById(R.id.hotword_title);
            mBrowserNumber = itemView.findViewById(R.id.hotword_number);
            mTrend = itemView.findViewById(R.id.hotword_trend);
            mIndexIcon = itemView.findViewById(R.id.hotword_index_icon);
            mDivider = itemView.findViewById(R.id.browser_hotword_divider);
        }
    }

    static class HotWordAdViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout mAdLayout;

        public HotWordAdViewHolder(View itemView) {
            super(itemView);
            mAdLayout = itemView.findViewById(R.id.hotword_item_ad_layout);
        }
    }
}
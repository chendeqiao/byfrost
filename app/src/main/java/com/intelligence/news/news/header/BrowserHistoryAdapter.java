package com.intelligence.news.news.header;

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
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.List;
import java.util.Random;

public class BrowserHistoryAdapter extends
        RecyclerView.Adapter<BrowserHistoryAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<WebSiteData> mDataList;

    public BrowserHistoryAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setData(List<WebSiteData> data) {
        this.mDataList = data;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View arg0) {
            super(arg0);
        }
        View vItem;
        TextView vName;
        ImageView vIcon;
        FrameLayout vRootView;
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
        View view = mInflater.inflate(R.layout.browser_car_history_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.vItem = view;
        viewHolder.vName = view.findViewById(R.id.car_history_name);
        viewHolder.vIcon = view.findViewById(R.id.car_history_icon);
        viewHolder.vRootView = view.findViewById(R.id.article_car_hitory_rootview);
        return viewHolder;
    }

    private Drawable getLableIcon(Context context) {
        Random random = new Random();
        int i = random.nextInt(4);
        Drawable drawable;
        switch (i) {
            case 1:
                drawable = context.getResources().getDrawable(R.drawable.browser_lable_icon_orange);
                break;
            case 2:
                drawable = context.getResources().getDrawable(R.drawable.browser_lable_icon_yellow);
                break;
            case 3:
                drawable = context.getResources().getDrawable(R.drawable.browser_lable_icon_blue);
                break;
            case 4:
                drawable = context.getResources().getDrawable(R.drawable.browser_lable_icon_green);
                break;
            default:
                drawable = context.getResources().getDrawable(R.drawable.browser_lable_icon_orange);
                break;
        }
        return drawable;
    }
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if(mDataList == null){
            return;
        }
        final WebSiteData browserHistory = mDataList.get(i);
        viewHolder.vName.setText(browserHistory.title);
        try {
            int resID = viewHolder.vName.getContext().getResources().getIdentifier(browserHistory.logo,
                    "drawable", viewHolder.vName.getContext().getPackageName());
            Drawable drawable = viewHolder.vName.getContext().getResources().getDrawable(resID);
            viewHolder.vIcon.setImageDrawable(drawable);
        } catch (Exception e) {
        }

        if (i == 0) {
            viewHolder.vRootView.setPadding((int) ScreenUtils.dpToPx(mContext, 20), 0, (int) ScreenUtils.dpToPx(mContext, 8), 0);
        } else if (i == mDataList.size() - 1) {
            viewHolder.vRootView.setPadding(0, 0, (int) ScreenUtils.dpToPx(mContext, 20), 0);
        } else {
            viewHolder.vRootView.setPadding(0, 0, (int) ScreenUtils.dpToPx(mContext, 8), 0);
        }
        viewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (browserHistory != null) {
                    SchemeUtil.openNewWebView(mContext, browserHistory.scheme);
                    Global.clearActivity();
                }
            }
        });
    }
}

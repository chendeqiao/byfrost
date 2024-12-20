package com.intelligence.browser.view.switchsearchengine;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.intelligence.browser.R;
import com.intelligence.browser.ui.search.SearchEnginePreference;

public class WebSiteSuspanededView extends RelativeLayout {

    private ImageView mIcon;
    private View mView;
    private boolean mIsItem; //是否是以mRecyclerView的Item为蓝本的

    public WebSiteSuspanededView(Context context, View view, boolean isItem) {
        super(context);
        mView = view;
        this.mIsItem = isItem;
        initView();
    }


    public void initView() {
        View.inflate(getContext(), R.layout.browser_search_engine_suspended, this);
        mIcon = findViewById(R.id.image_icon);
        setImageViewWH();
    }

    public ImageView getImageView() {
        return mIcon;
    }

    public void setImageResource(int rid) {
        mIcon.setImageResource(rid);
    }

    public void setImage(String name) {
        SearchEnginePreference.setSearchEngineIcon(getContext(), mIcon, name);
    }

    public void setImageViewWH() {
        if (mView == null) {
            return;
        }

        ImageView imageView = mView.findViewById(mIsItem ? R.id.item_icon : R.id.select_search_engine_icon);
        if (imageView == null) {
            return;
        }

        mIcon.getLayoutParams().width = imageView.getWidth();
        mIcon.getLayoutParams().height = imageView.getHeight();

        if (mIsItem) {
            mIcon.setLayoutParams(imageView.getLayoutParams());
        } else {
            mIcon.getLayoutParams().width = imageView.getWidth();
            mIcon.getLayoutParams().height = imageView.getHeight();
        }
    }

    public void setImageViewMargins(int margin) {

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mIcon.getLayoutParams();
        layoutParams.setMargins(margin, margin, margin, margin);
        mIcon.setLayoutParams(layoutParams);
    }

}

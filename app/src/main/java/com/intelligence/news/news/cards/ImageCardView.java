package com.intelligence.news.news.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intelligence.browser.R;
import com.intelligence.news.news.mode.NewsData;

public class ImageCardView extends BaseCardView {
    public ImageCardView(Context context) {
        super(context);
    }

    @Override
    protected void initHeaderView(ViewGroup headerParent) {
        View vHeaderView = LayoutInflater.from(headerParent.getContext()).inflate(R.layout.browser_card_header, null);
        headerParent.addView(vHeaderView);
    }

    @Override
    protected void initBodyView(ViewGroup bodyParent) {
        View vHeaderView = LayoutInflater.from(bodyParent.getContext()).inflate(R.layout.browser_card_body_image, null);
        bodyParent.addView(vHeaderView);
    }

    @Override
    protected void initChildView(ViewGroup viewGroup) {
        super.initChildView(viewGroup);
        mTitle = viewGroup.findViewById(R.id.browser_card_title);
        mImage = viewGroup.findViewById(R.id.browser_card_image);
    }

    @Override
    public void updateData(NewsData newsData) {
        super.updateData(newsData);
    }
}

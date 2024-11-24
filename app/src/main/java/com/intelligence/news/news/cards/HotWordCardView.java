package com.intelligence.news.news.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.news.NetConfig;
import com.intelligence.news.news.mode.NewsData;

import java.util.ArrayList;

public class HotWordCardView extends BaseCardView {
    private ViewPager mViewPager;
    private View mMore;
    private ArrayList<HotwordCardItemView> mList = new ArrayList();

    public HotWordCardView(Context context) {
        super(context);
    }

    public HotWordCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HotWordCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initHeaderView(ViewGroup headerParent) {
        View vHeaderView = LayoutInflater.from(headerParent.getContext()).inflate(R.layout.browser_card_hotword_header, null);
        headerParent.addView(vHeaderView);
    }

    @Override
    protected void initBodyView(ViewGroup headerParent) {
        View vHeaderView = LayoutInflater.from(headerParent.getContext()).inflate(R.layout.browser_card_hotword_body, null);
        headerParent.addView(vHeaderView);
    }

    @Override
    protected void initChildView(ViewGroup viewGroup) {
        super.initChildView(viewGroup);
        mViewPager = viewGroup.findViewById(R.id.hotword_viewpager);
        mMore = viewGroup.findViewById(R.id.browser_card_more);
        mMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SchemeUtil.jumpToScheme(getContext(),SchemeUtil.BROWSER_SCHEME_PATH_HOT_WORDS);
            }
        });
        HotwordCardItemView baiduItemView = new HotwordCardItemView(getContext());
        baiduItemView.setType(NetConfig.PAGE_BAIDU);
        mList.add(baiduItemView);
        HotwordCardItemView toutiaoItemView = new HotwordCardItemView(getContext());
        toutiaoItemView.setType(NetConfig.PAGE_DOUYIN);
        mList.add(toutiaoItemView);
        HotwordCardItemView weiboItemView = new HotwordCardItemView(getContext());
        weiboItemView.setType(NetConfig.PAGE_WEIBO);
        mList.add(weiboItemView);
        HotwordPagerAdapter hotwordPagerAdapter = new HotwordPagerAdapter();
        hotwordPagerAdapter.setData(mList);
        mViewPager.setAdapter(hotwordPagerAdapter);
    }

    @Override
    protected void initFooterView(ViewGroup footerView) {
//        super.initFooterView(footerView);
    }

    @Override
    public void updateData(NewsData newsData) {
    }

    class HotwordPagerAdapter extends PagerAdapter {
        private ArrayList<HotwordCardItemView> mList;

        public void setData(ArrayList<HotwordCardItemView> mList) {
            this.mList = mList;
        }

        @Override
        public int getCount() {
            return mList.isEmpty() ? 0 : mList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mList.get(position));
            return mList.get(position);
        }
    }
}

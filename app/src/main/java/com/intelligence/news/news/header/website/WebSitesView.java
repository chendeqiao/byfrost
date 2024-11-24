package com.intelligence.news.news.header.website;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.componentlib.sugar.CommonNavigator;
import com.intelligence.componentlib.sugar.CommonNavigatorAdapter;
import com.intelligence.componentlib.sugar.DummyPagerTitleView;
import com.intelligence.componentlib.sugar.IPagerIndicator;
import com.intelligence.componentlib.sugar.IPagerTitleView;
import com.intelligence.componentlib.sugar.LinePagerIndicator;
import com.intelligence.componentlib.sugar.MagicIndicator;
import com.intelligence.componentlib.sugar.UIUtil;
import com.intelligence.componentlib.sugar.ViewPagerHelper;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;
import java.util.List;

public class WebSitesView extends FrameLayout implements View.OnClickListener {
    private Context mContext;
    private ViewPager mSugarViewPager;
    private MagicIndicator mMagicIndicator;
    public static final int COLUMN_NUM = 10;
    private List<WebSiteData> mData;
    private ArrayList<SugarPageView> mList;
    private CommonNavigator commonNavigator;

    public WebSitesView(@NonNull Context context) {
        this(context, null, 0);
    }

    public WebSitesView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebSitesView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init(mContext);
    }


    private void init(Context context) {
        View.inflate(getContext(), R.layout.layout_website, this);
        mSugarViewPager = findViewById(R.id.sugar_viewpager);
        mMagicIndicator = findViewById(R.id.sugar_indicator);

        int height = (int) ScreenUtils.dpToPx(getContext(),52.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * 2);
        mSugarViewPager.setLayoutParams(layoutParams);
    }


    public void bindData(List<WebSiteData> mTopicData,boolean isEdit) {
        if(CollectionUtils.isEmpty(mTopicData)){
            setVisibility(GONE);
            return;
        }
        mData = mTopicData;
        int pageNum = mTopicData.size() / COLUMN_NUM;
        if (mTopicData.size() % COLUMN_NUM > 0) pageNum++;
        mList = new ArrayList<>();
        for (int i = 0; i < pageNum; i++) {
            int fromIndex = i * COLUMN_NUM;
            int toIndex = (i + 1) * COLUMN_NUM;
            if (toIndex > mTopicData.size()) toIndex = mTopicData.size();
            ArrayList<WebSiteData> menuItems = new ArrayList<WebSiteData>(mTopicData.subList(fromIndex, toIndex));

            SugarPageView sugarPageView = new SugarPageView(getContext());
            sugarPageView.setData(menuItems,isEdit);
            mList.add(sugarPageView);
        }
        if(mList.size()>1) {
//            reportPv(mList.get(0).getData(),0);
        }
        WebSitesPagerAdapter menuViewPagerAdapter = new WebSitesPagerAdapter(mList);
        mSugarViewPager.setAdapter(menuViewPagerAdapter);

        commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setAdjustMode(true);
        final int finalPageNum = pageNum;

        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return finalPageNum;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                return new DummyPagerTitleView(context);
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(UIUtil.dip2px(context, 3));
                indicator.setLineWidth(UIUtil.dip2px(context, 18 / finalPageNum));
                indicator.setRoundRadius(UIUtil.dip2px(context, 0));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2));
                int funcballbarcolor_select = getResources().getColor(R.color.browser_title_blue);
                int funcballbarcolor_noSelect = getResources().getColor(R.color.indicator_bg);
                mMagicIndicator.setDrawable(funcballbarcolor_noSelect);
                indicator.setColors(funcballbarcolor_select);
                return indicator;
            }
        });
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mSugarViewPager);
        mSugarViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    @Override
    public void onClick(View v) {
    }
}
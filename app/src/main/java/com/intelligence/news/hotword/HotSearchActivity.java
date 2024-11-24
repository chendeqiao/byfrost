package com.intelligence.news.hotword;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.R;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.componentlib.viewpagetabbar.PagerSlidingTabStrip;
import com.intelligence.news.NetConfig;
import com.intelligence.news.news.mode.HotWordData;
import com.intelligence.browser.ui.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HotSearchActivity extends BaseActivity {
    private PagerSlidingTabStrip mTabbar;
    private ViewPager mViewPager;
    private List<HotWordFragment> mList = new ArrayList<>();
    private String[] mTitles;
    private BasePagerAdapter mAdapter;
    private LinearLayout mScrollParent;
    private View mBack;
    private TextView mUpdateTime;
    private View mUpdateTips;
    private int mPagerIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
//                .FLAG_FULLSCREEN);
//        AppBarThemeHelper.setStatusBarDarkMode(this);
        setContentView(R.layout.browser_hot_search_page);
        initView();
        Global.addActivity(this);
        SharedPreferencesUtils.put(SharedPreferencesUtils.IS_OPEN_HOTWORD_TODAY,System.currentTimeMillis());

        String page = "0";
        if(getIntent() != null && getIntent().getData() != null) {
            page = getIntent().getData().getQueryParameter("page");
        }
        mViewPager.setCurrentItem(StringUtil.getInt(page, 0));
    }

    private String getUpdateTime(){
        Long timeStamp = (long) SharedPreferencesUtils.get(HotWordHttpRequest.CACHE_HOT_WORD_TIME_KEY + 0, 0l);
        SimpleDateFormat sdf2 = new SimpleDateFormat(" HH:mm");
        String sd2 = sdf2.format(new Date(Long.parseLong(String.valueOf(timeStamp))));
        return sd2;
    }

    private void initView() {
        mTabbar = findViewById(R.id.browser_hotword_tabbar);
        mTabbar.setTextSize(20,18);
        mViewPager = findViewById(R.id.browser_hotword_viewpager);
        mViewPager.setOffscreenPageLimit(3);

        mScrollParent = findViewById(R.id.browser_hotword_scroll_parent);
        mUpdateTips = findViewById(R.id.browser_last_url_tips);
//        mUpdateTips.setVisibility(View.VISIBLE);
        mUpdateTime = findViewById(R.id.browser_hot_word_update_time);
        mUpdateTime.setText("每30分钟自动更新  最近更新:"+getUpdateTime());

        mBack = findViewById(R.id.browser_hot_word_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mList.add(new HotWordFragment().setType(NetConfig.PAGE_BAIDU));
        mList.add(new HotWordFragment().setType(NetConfig.PAGE_DOUYIN));
        mList.add(new HotWordFragment().setType(NetConfig.PAGE_WEIBO));
        for (HotWordFragment hotWordFragment : mList){
            hotWordFragment.setClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view == null || view.getTag() == null || view.getTag() instanceof HotWordData){
                        HotWordData hotWordData = (HotWordData) view.getTag();
                        if (hotWordData.pageType == NetConfig.PAGE_WEIBO) {
                            SchemeUtil.openNewWebView(HotSearchActivity.this, BrowserInitDataContants.WEIBO_SEARCH_URL + hotWordData.hotword);
                        } else if (hotWordData.pageType == NetConfig.PAGE_BAIDU) {
                            SchemeUtil.openNewWebView(HotSearchActivity.this, "https://m.baidu.com/s?wd="+ hotWordData.hotword+"");
                        } else {
                            SchemeUtil.openNewWebView(HotSearchActivity.this, hotWordData.hotword);
                        }
                        Global.clearActivity();
                    }
                }
            });
        }
        mTitles = new String[]{"实时热点", "头条", "微博"};

        mAdapter = new BasePagerAdapter(getSupportFragmentManager(), mTitles);
        mViewPager.setAdapter(mAdapter);
        mTabbar.setViewPager(mViewPager);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hintTips();
            }
        },5000);
    }

    private void hintTips(){
        if(mUpdateTips != null){
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f,0f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mUpdateTips.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mUpdateTips.startAnimation(alphaAnimation);
        }
    }

    class BasePagerAdapter extends FragmentPagerAdapter {
        String[] mTitles;

        public BasePagerAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
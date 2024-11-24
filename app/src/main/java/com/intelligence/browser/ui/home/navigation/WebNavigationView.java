package com.intelligence.browser.ui.home.navigation;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.adapter.RecommendAdapter;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.componentlib.photoview.HackyViewPager;
import com.intelligence.componentlib.sugar.CommonNavigator;
import com.intelligence.componentlib.sugar.CommonNavigatorAdapter;
import com.intelligence.componentlib.sugar.DummyPagerTitleView;
import com.intelligence.componentlib.sugar.IPagerIndicator;
import com.intelligence.componentlib.sugar.IPagerTitleView;
import com.intelligence.componentlib.sugar.LinePagerIndicator;
import com.intelligence.componentlib.sugar.MagicIndicator;
import com.intelligence.componentlib.sugar.UIUtil;
import com.intelligence.componentlib.sugar.ViewPagerHelper;

import java.util.List;

public class WebNavigationView extends RelativeLayout implements RecommendAdapter.AdapterItemListener,
        ViewPager.OnPageChangeListener,
        DatabaseManager.DataChangeListener<RecommendUrlEntity> {
    private static final int RECOMMEND_COLUMN_PORTRAIT = 5;
    private static final int NAVIGATION_MARGIN_TOP_ON_OFFSET = DisplayUtil.dip2px(BrowserApplication.getInstance(), 8f);
    private static final int INDEX_OF_INPUT_PAGE = 2;

    private FragmentActivity mActivity;
    private LayoutInflater mInflater;
    private RelativeLayout mRootView;
    private HackyViewPager mViewPager;
    private RecommendPagerAdapter mPagerAdapter;
    private MagicIndicator mIndicator;
    private CommonNavigator mCommonNavigator;
    final int finalPageNum = 2;
    public boolean isCanScroll;
    private CardTipsView mCardTipsView;

    public static final int RECMMEND_ITEM_COUNT = 10;
    private BaseUi mUi;
    public WebNavigationView(FragmentActivity activity, BaseUi baseUi) {
        super(activity);
        this.mActivity = activity;
        mUi = baseUi;
        init(activity);
    }

    private void init(FragmentActivity activity) {
        mInflater = LayoutInflater.from(activity);
        removeAllViews();
        mRootView = (RelativeLayout) mInflater.inflate(R.layout.browser_main_navigation_portrait, this, false);
        addView(mRootView);
        mRootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPagerAdapter != null) {
                    mPagerAdapter.exitEditState();
                }
//                if(mUi != null){
//                    mUi.isCanScrollWebPage(false);
//                }
            }
        });

        MarginLayoutParams layoutParams = (MarginLayoutParams) mRootView.getLayoutParams();
        layoutParams.topMargin = ScreenUtils.getHeaderHeight(getContext()) + NAVIGATION_MARGIN_TOP_ON_OFFSET;
        mRootView.setLayoutParams(layoutParams);

        mViewPager = mRootView.findViewById(R.id.recommend_viewpager);
        mCardTipsView = mRootView.findViewById(R.id.browser_card_tips);
        mCardTipsView.setCardTipsNotifyState(new CardTipsView.CardTipsNotifyState() {
            @Override
            public void cardTipsNotifyState() {
                showCardTips(mCardTipsView.isShowCardTips());
            }
        });
        mIndicator = mRootView.findViewById(R.id.recommend_indicator);

        mCommonNavigator = new CommonNavigator(getContext());
        mCommonNavigator.setAdjustMode(true);

        mCommonNavigator.setAdapter(new CommonNavigatorAdapter() {
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
                indicator.setLineHeight(getContext().getResources().getDimension(R.dimen.browser_indicator_height));
                indicator.setLineWidth(getContext().getResources().getDimension(R.dimen.browser_indicator_width) / 2);
                indicator.setRoundRadius(UIUtil.dip2px(context, 0));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2));
                int funcballbarcolor_select = getResources().getColor(R.color.grid_common_text_color);
                int funcballbarcolor_noSelect = getResources().getColor(R.color.indicator_bg);
                mIndicator.setDrawable(funcballbarcolor_noSelect);
                indicator.setColors(funcballbarcolor_select);
                return indicator;
            }
        });
        mIndicator.setNavigator(mCommonNavigator);
        ViewPagerHelper.bind(mIndicator, mViewPager);
        mViewPager.addOnPageChangeListener(this);
        refreshInputUrlAndRecommend(true);
        RecommendUrlUtil.addContentObserver(this);
    }

    public boolean isShowTipsCard(){
        return mCardTipsView.getVisibility() == View.VISIBLE;
    }

    public void refreshInputUrlAndRecommend(boolean isInit) {
        new AsyncTask<Void, Void, Void>() {
            List<RecommendUrlEntity> recommendUrls = null;
            //
            @Override
            protected Void doInBackground(Void... vs) {
                recommendUrls = RecommendUrlUtil.getLocalRecommendInfos();
                return null;
            }
            //
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                replaceData(recommendUrls,isInit);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void replaceData(List<RecommendUrlEntity> list, boolean isInit) {
        if (list == null) {
            return;
        }
        boolean isTwoLine = list.size() > RECMMEND_ITEM_COUNT - 1;
        isCanScroll = isTwoLine;
        mIndicator.setVisibility(isTwoLine ? VISIBLE : GONE);
        mViewPager.setPagingEnabled(isTwoLine);
        if(!isInit) {
            mViewPager.setCurrentItem(list.size() > RECMMEND_ITEM_COUNT ? 1 : 0);
        }
        if(!isCanScroll){
            mViewPager.setCurrentItem(0);
        }
        if (mPagerAdapter == null) {
            mPagerAdapter = new RecommendPagerAdapter(mActivity, handleListCount(list), this);
            mViewPager.setAdapter(mPagerAdapter);
        } else {
            mPagerAdapter.updateData(handleListCount(list));
        }
        if(isInit){
            notifyCardTips();
        }
    }

    public void notifyCardTips() {
        try {
            if (mCardTipsView != null) {
                showCardTips(mCardTipsView.notifyTipsCardState());
            }
        } catch (Exception e) {
        }
    }

    public void showCardTips(boolean isShowCardTips) {
        mCardTipsView.setVisibility(isShowCardTips ? VISIBLE : GONE);
        if (mUi != null) {
            mUi.showCardTipsView(isShowCardTips);
        }
        MarginLayoutParams layoutParams = (MarginLayoutParams) mRootView.getLayoutParams();
        if (isShowCardTips) {
            layoutParams.topMargin = ScreenUtils.getHeaderHeightForCardTips(getContext()) + NAVIGATION_MARGIN_TOP_ON_OFFSET;
        } else {
            layoutParams.topMargin = ScreenUtils.getHeaderHeight(getContext()) + NAVIGATION_MARGIN_TOP_ON_OFFSET;
        }
        mRootView.setLayoutParams(layoutParams);

        ViewGroup.MarginLayoutParams indicatorParams = (ViewGroup.MarginLayoutParams) mIndicator.getLayoutParams();
        indicatorParams.topMargin = (int) getContext().getResources().getDimension(isShowCardTips ? R.dimen.browser_indicator_margin_top_tiny : R.dimen.browser_indicator_margin_top);
        mIndicator.setLayoutParams(indicatorParams);
        mIndicator.requestLayout();
    }

    public boolean isCanScroll(MotionEvent ev) {
        if (!isCanScroll) {
            return false;
        }
        if (isCanScroll && mCurrentPosition == 1) {
            return false;
        }
        int[] location = new int[2];
        mViewPager.getLocationOnScreen(location);
        int y = location[1];
        if (ev.getY() > y && ev.getY() < y + mViewPager.getHeight()) {
            return true;
        }
        return false;
    }

    private List handleListCount(List<RecommendUrlEntity> list) {
        return list.size() > 2 * RECMMEND_ITEM_COUNT ? list.subList(0, 2 * RECMMEND_ITEM_COUNT) : list;
    }

    @Override
    public void onInsertToDB(RecommendUrlEntity entity) {
//        if (mPagerAdapter != null) {
//            mPagerAdapter.onInsertToDB(entity);
//        }
        refreshInputUrlAndRecommend(false);
    }

    @Override
    public void onUpdateToDB(RecommendUrlEntity entity) {
//        if (mPagerAdapter != null) {
//            mPagerAdapter.onUpdateToDB(entity);
//        }
        refreshInputUrlAndRecommend(true);
    }

    @Override
    public void onDeleteToDB(RecommendUrlEntity entity) {
//        if (mPagerAdapter != null) {
//            mPagerAdapter.onDeleteToDB(entity);
//        }
        refreshInputUrlAndRecommend(true);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    public void onResume() {
        if (mPagerAdapter != null) {
            if (mPagerAdapter.isDeleteMode()) {
                mPagerAdapter.setDeleteMode(false);
            } else {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    public boolean onBackKey() {
        if (mPagerAdapter.isDeleteMode()) {
            mPagerAdapter.setDeleteMode(false);
            return true;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public int mCurrentPosition = 0;

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
        if (position != INDEX_OF_INPUT_PAGE) {
            InputMethodUtils.hideKeyboard(mActivity);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void openUrl(RecommendAdapter.CommonUrlItemViewHolder holder) {
        String url = holder.data.getUrl();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        SchemeUtil.openWebView(getContext(), url);
    }

    @Override
    public void editNavigation(RecommendAdapter.CommonUrlItemViewHolder viewHolder) {

    }

    @Override
    public void onClickAdd(RecommendAdapter.CommonUrlItemViewHolder viewHolder) {
        SchemeUtil.jumpToScheme(mActivity, "yunxin://browser/editnavigation?");
//                transferOnEditStatus(DisplayUtil.isScreenPortrait(getContext()));
    }

    @Override
    public void onDataSetChange() {
        refreshInputUrlAndRecommend(true);
    }

    @Override
    public void onDeleteMode() {
        if(mPagerAdapter != null){
            mPagerAdapter.setDeleteMode(true);
        }
    }

    @Override
    public void offDeleteMode() {
        if(mPagerAdapter != null){
            mPagerAdapter.setDeleteMode(false);
        }
    }

}

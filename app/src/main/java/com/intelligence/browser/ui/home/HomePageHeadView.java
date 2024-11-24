package com.intelligence.browser.ui.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.settings.BrowserAboutFragment;
import com.intelligence.browser.settings.BrowserSettingActivity;
import com.intelligence.browser.ui.search.SearchEnginePreference;
import com.intelligence.browser.utils.AnimationUtils;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.RedSystemControll;
import com.intelligence.browser.view.SearchCardView;
import com.intelligence.commonlib.baseUi.MultiTextScrollTextView;
import com.intelligence.commonlib.net.OkHttpException;
import com.intelligence.commonlib.net.ResponseCallback;
import com.intelligence.commonlib.service.LocationService;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.componentlib.badge.BadgeView;
import com.intelligence.news.hotword.HotWordHttpRequest;
import com.intelligence.news.news.header.WebLableView;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.news.mode.HotWordData;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;

public class HomePageHeadView extends RelativeLayout implements HotWordHttpRequest.RefreshHotWordListener, View.OnClickListener, LocationService.LocationChangeListener {

    private static final int LOGOTYPE_HAVE = 0;//有搜索引擎的logo
    private static final int LOGOTYPE_TO_DEFAULT = 1;//替换成默认logo
    public static final int LOGOTYPE_DEFAULT = 2;//已经是默认logo

    public LinearLayout mSearchEngineLayout;
    private SearchCardView mSearchCardView;
    private BrowserMainPageController mBrowserMainPageController;
    private ViewGroup mSearchEngineLogo;
    private Context mContext;
    private boolean mIncognito;
    private boolean mIsShowTipsCard;
    private int mLogoType = LOGOTYPE_HAVE;
//    private TimeController timeWeatherController;
//    private ImageView mBrowserTitle;
    private View mSettingLayout;
    private MultiTextScrollTextView mHeaderHotWordTitle;
    private TextView mWeatherTem;
    private ViewGroup mRootView;
//    private View mHeaderTools;
    private BadgeView mLogoBadge;
    private BadgeView mSettingBadge;
    private ImageView mHotLogo;

    public HomePageHeadView(Context context, BrowserMainPageController browserMainPageController, boolean isShowTipsCard, boolean incognito) {
        super(context);
        mContext = context;
        mIsShowTipsCard = isShowTipsCard;
        mBrowserMainPageController = browserMainPageController;
        mIncognito = incognito;
        initView(mIsShowTipsCard,incognito);
    }

    public FrameLayout.LayoutParams createLayoutParams() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void initView(boolean isShowTipsCard,boolean incognito) {
        if (mSearchEngineLayout != null) {
            mSearchEngineLayout.removeAllViews();
        }
        removeAllViews();
        View rootView;
        rootView = View.inflate(getContext(), R.layout.browser_head_title_view_portrait, this);
        mSearchEngineLayout = rootView.findViewById(R.id.search_engine_layout);
        mRootView = rootView.findViewById(R.id.top_container);
        mHeaderHotWordTitle = rootView.findViewById(R.id.browser_header_hotword);
        mSettingLayout = rootView.findViewById(R.id.browser_header_setting_layout);
        mLogoBadge = rootView.findViewById(R.id.logo_badgeview);
        mHotLogo = rootView.findViewById(R.id.logo_hot);
        mHotLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SchemeUtil.jumpToScheme(getContext(), getContext().getResources().getString(R.string.byfrost_hot_url));
                SharedPreferencesUtils.put(SharedPreferencesUtils.TIME_HOT_CURRENT_CLICK,System.currentTimeMillis());
            }
        });
        mLogoBadge.showTextBadge("new", true);
        mSettingBadge = rootView.findViewById(R.id.setting_badgeview);
        mSettingLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowUpdate) {
                    BrowserSettingActivity.startPreferenceFragmentForResult((Activity) getContext(), BrowserAboutFragment.class.getName(), 1007);
                } else {
                    SchemeUtil.jumpToScheme(getContext(), SchemeUtil.BROWSER_SCHEME_PATH_SETTING);
                }
            }
        });
//        mBrowserTitle.setTextColor();
        mSearchEngineLogo = rootView.findViewById(R.id.search_engine_logo);
        mSearchEngineLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mHotLogo != null && mHotLogo.getVisibility() == View.VISIBLE){
                    SchemeUtil.jumpToScheme(getContext(), getContext().getResources().getString(R.string.byfrost_hot_url));
                    SharedPreferencesUtils.put(SharedPreferencesUtils.TIME_HOT_CURRENT_CLICK,System.currentTimeMillis());
                } else if (isShowUpdate) {
                    BrowserSettingActivity.startPreferenceFragmentForResult((Activity) getContext(), BrowserAboutFragment.class.getName(), 1007);
                } else {
                    SchemeUtil.jumpToScheme(getContext(), getContext().getResources().getString(R.string.byfrost_hot_url));
                    SharedPreferencesUtils.put(SharedPreferencesUtils.TIME_HOT_CURRENT_CLICK,System.currentTimeMillis());
                }
                if (mLogoBadge != null && mLogoBadge.getVisibility() == HomePageHeadView.VISIBLE) {
                    mLogoBadge.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLogoBadge.setVisibility(GONE);
                        }
                    }, 500);

                }
            }
        });

        mSearchCardView = new SearchCardView(mContext, mBrowserMainPageController);
        mSearchEngineLayout.addView(mSearchCardView);
        mSearchCardView.getLayoutParams().height = getResources().getDimensionPixelOffset(R.dimen.search_engine_height);
        updateSearchEngineLogo(false);

        if(isShowTipsCard) {
            mHeaderHeight = ScreenUtils.getHeaderHeightForCardTips(getContext());
        }else {
            mHeaderHeight = ScreenUtils.getHeaderHeight(getContext());
        }
        if (SharedPreferencesUtils.getRecommonWebsites()) {
            WebLableView webLableView = new WebLableView(mContext,new WebLableView.OnWebLableClickListener(){
                @Override
                public void onWebLableClick(View view,WebSiteData webSiteData) {
                    if(mBrowserMainPageController != null && mBrowserMainPageController.mController != null) {
                        mBrowserMainPageController.mController.openSelectWebSiteView(view,webSiteData);
                    }
                }
            });
            mSearchEngineLayout.addView(webLableView);
//            mRootView.getLayoutParams().height = (int) getContext().getResources().getDimension(R.dimen.main_uncard_height);
            mRootView.getLayoutParams().height = mHeaderHeight + ScreenUtils.dpToPxInt(getContext(),10);
        } else {
//            mRootView.getLayoutParams().height = (int) getContext().getResources().getDimension(R.dimen.main_uncard_simple_height);
            mRootView.getLayoutParams().height = mHeaderHeight + ScreenUtils.dpToPxInt(getContext(),20);
        }
        mSearchCardView.setTransitionName("TRANSITIONIMAGE");
        mSearchCardView.onIncognito(incognito);
        initHeaderView((ViewGroup) rootView);
    }

    public void showCardTips(boolean isShowCardTips) {
        if(mRootView != null)
        if(isShowCardTips){
            mHeaderHeight = ScreenUtils.getHeaderHeightForCardTips(getContext());
        }else {
            mHeaderHeight = ScreenUtils.getHeaderHeight(getContext());
        }
        if (SharedPreferencesUtils.getRecommonWebsites()) {
            mRootView.getLayoutParams().height = mHeaderHeight + ScreenUtils.dpToPxInt(getContext(), 10);
        } else {
            mRootView.getLayoutParams().height = mHeaderHeight + ScreenUtils.dpToPxInt(getContext(), 20);
        }
        mRootView.requestLayout();
    }

    private int mHeaderHeight = 0;

    private View mWeatherLayout;
    private void initHeaderView(ViewGroup view){
        mWeatherLayout = view.findViewById(R.id.browser_header_navigation);
        mWeatherLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SchemeUtil.jumpToWord(getContext(),getContext().getResources().getString(R.string.lable_weather));
            }
        });
        mWeatherTem = view.findViewById(R.id.screenlock_weather_text);
        initControll(view);
        initScrollHotword();
//        requestWeatherData();
//        LocationService.setLocationListener(this);
    }

    private void requestWeatherData(){
        if(BrowserApplication.getInstance().isSimpleVersion()){
//            setVisibility(GONE);
            return;
        }
        WeatherHttpRequest weatherHttpRequest = new WeatherHttpRequest();
        weatherHttpRequest.getWeatherData(new ResponseCallback() {
            @Override
            public void onSuccess(Object responseObj) {
                if(responseObj == null || !(responseObj instanceof DataResult)){
                    return;
                }
                if(CollectionUtils.isEmpty(((DataResult)responseObj).newsList) || !(((DataResult)responseObj).newsList.get(0) instanceof WeatherData)){
                    return;
                }
                WeatherData weatherData = (WeatherData) ((DataResult)responseObj).newsList.get(0);
                updateWeatherData(weatherData);
            }

            @Override
            public void onFailure(OkHttpException failuer) {

            }
        });
    }

    private void updateWeatherData(WeatherData weatherData) {
//        if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_qing))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_setting_sun));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_yin))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_yin));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_yun))
//                || weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_duoyun))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_yun));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_yu))
//                || weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_dayu))
//                || weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_leiyu))
//                || weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_zhongyu))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_yu));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_xue))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_xue));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_bingbao))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_bingbao));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_yangcheng))
//                || weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_fucheng))
//                || weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_yangcheng))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_shachengbao));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_mai))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_mai));
//        } else if (weatherData.weatherimg.contains(mContext.getResources().getString(R.string.weather_wu))) {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.weather_wu));
//        } else {
//            mWeatherIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.browser_setting_weather));
//        }
        mWeatherTem.setText(weatherData.weather);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.layout_header_time:
                SchemeUtil.jumpToWord(getContext(),"日历");
                break;
            case R.id.layout_header_weather:
                SchemeUtil.jumpToWord(getContext(),"天气");
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        requestPermissions();
                    }
                },1000);

                break;
            case R.id.layout_header_hotword:
                SchemeUtil.jumpHotWorsPage(getContext(), 0);
                break;
//            case R.id.browser_header_setting_tools:
//                if(mBrowserMainPageController != null && mBrowserMainPageController.mController != null) {
//                    mBrowserMainPageController.mController.showTools(true);
//                }
////                SchemeUtil.jumpHotWorsPage(getContext(), 0);
//                break;
        }
    }

    private boolean mIsRequestPerssion;
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ((Activity) mContext).requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, 10002);
            mIsRequestPerssion = true;
        }
    }

    @Override
    public void onChangeCity(String cityName) {
//        SharedPreferencesUtils.put(SharedPreferencesUtils.WEATHER_CITY,cityName);
//        requestWeatherData();
    }

    @Override
    public void onRefreshHotWord() {
        initScrollHotword();
    }

    private void initScrollHotword(){
        try {
            DataResult dataResult = HotWordHttpRequest.getData(0);
            ArrayList<String> list = new ArrayList();
            list.add(((HotWordData) dataResult.newsList.get(0)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(1)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(2)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(3)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(4)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(5)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(6)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(7)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(8)).hotword);
            list.add(((HotWordData) dataResult.newsList.get(9)).hotword);


            mHeaderHotWordTitle.setTextList(list);
            mHeaderHotWordTitle.setTextStillTime(5000);//设置停留时长间隔
            mHeaderHotWordTitle.setAnimTime(500);
            mHeaderHotWordTitle.setText(12, 4, getResources().getColor(R.color.browser_tips_color));
            mHeaderHotWordTitle.startAutoScroll();
        }catch (Exception e){
            //to do nothing.
        }
    }

    private void initControll(ViewGroup viewGroup){
//        timeWeatherController = new TimeController(getContext());
//        timeWeatherController.apply(viewGroup);
//        timeWeatherController.setTimeInfo();
    }

    public View getBrowserLogo() {
        return mSearchEngineLogo;
    }

    public LinearLayout getSearchBar() {
        return mSearchEngineLayout;
    }

    public void setHeaderVisibleHint(boolean isVisible) {
        mSearchEngineLayout.setVisibility(isVisible ? VISIBLE : INVISIBLE);
//        if(BrowserSettings.getInstance().getHeaderNotifiction()) {
//            mHeaderLayout.setVisibility(isVisible ? VISIBLE : INVISIBLE);
////            mHeaderLayout.setVisibility(GONE);
//        }
    }

    public void isRecommendEdit(boolean isEdit) {
        if (mSettingLayout != null) {
            mSettingLayout.setVisibility(isEdit ? GONE : VISIBLE);
        }
    }

    public void setState(float state) {
        mSearchCardView.setState(state);
    }

    public void onResume() {
//        mHeaderLayout.setVisibility(BrowserSettings.getInstance().getHeaderNotifiction() ? VISIBLE : GONE);
        mSearchCardView.onResume();
        if(mIsRequestPerssion) {
//            mContext.startService(new Intent(mContext, LocationService.class));
        }
        mIsRequestPerssion = false;
        mHotLogo.setVisibility(GONE);
        mLogoBadge.setVisibility(GONE);
        isShowUpdate = false;
        if (mLogoBadge != null) {
            if (isShowUpdateTips()) {
                mLogoBadge.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLogoBadge.setVisibility(VISIBLE);
                        isShowUpdate = true;
                        if (!mIsShowAnim) {
                            mLogoBadge.startShakeAndRotateAnimation();
                        }
                        mIsShowAnim = true;
                    }
                }, 200);
            } else if (RedSystemControll.isCanShowHotRed()) {
                mLogoBadge.setVisibility(GONE);
                mHotLogo.setVisibility(VISIBLE);
                if(!isShowHotAnim) {
                    mHotLogo.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                AnimationUtils.startShakeAndRotateAnimation(mHotLogo);
                            }catch (Exception e){
                            }
                        }
                    },1000);

                }
                isShowHotAnim = true;
            } else {
                mLogoBadge.setVisibility(GONE);
                mHotLogo.setVisibility(GONE);
            }
        }
        mVisibleCount++;
    }

    private boolean isShowHotAnim;

    private boolean isShowUpdate;

    private int mVisibleCount = 0;

    private boolean mIsShowAnim;

    private boolean isShowUpdateTips(){
        try {
            int lastVersion = DeviceInfoUtils.getAppVersionCode(getContext());
            boolean isLastVersion = lastVersion < SharedPreferencesUtils.getVersionCode();
            boolean isClicked = (lastVersion != (int) SharedPreferencesUtils.get(BrowserAboutFragment.UPDATE_PAGE_CLICK, 0));
            return isLastVersion && isClicked && mVisibleCount > 1;
        }catch (Exception e){
        }
        return false;
    }

    public void onPause() {
    }

    public void setTouch(boolean isCanClick) {
        mSearchCardView.setIsCanClick(isCanClick);
    }

    public void updateSearchEngineLogo(boolean isRunAnima) {
        if (mSearchEngineLogo == null || mContext == null) {
            return;
        }

        final String searchEngineName = BrowserSettings.getInstance().getSearchEngineName();

        if (!SearchEnginePreference.isHaveSearchEngineLogo(mContext, searchEngineName)) {
            if (mLogoType == LOGOTYPE_DEFAULT) {
                return;
            } else { //这里不会出现logoType = LOGOTYPE_TO_DEFAULT情况，所以不用判断
                mLogoType = LOGOTYPE_TO_DEFAULT;
            }
        } else if (mLogoType == LOGOTYPE_DEFAULT) {
            mLogoType = LOGOTYPE_HAVE;
        }

        if (!isRunAnima) {
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator oldScaleX = ObjectAnimator.ofFloat(mSearchEngineLogo, "scaleX", 1, 0);
        ObjectAnimator newScaleX = ObjectAnimator.ofFloat(mSearchEngineLogo, "scaleX", 0, 1);
        oldScaleX.setInterpolator(new DecelerateInterpolator());
        oldScaleX.setDuration(100);
        newScaleX.setInterpolator(new DecelerateInterpolator());
        newScaleX.setDuration(100);
        animatorSet.play(newScaleX).after(oldScaleX);
    }

    public void onIncognito(boolean incognito) {
        mIncognito = incognito;
        mSearchCardView.onIncognito(incognito);
    }
}
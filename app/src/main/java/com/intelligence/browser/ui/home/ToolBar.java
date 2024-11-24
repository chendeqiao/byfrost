package com.intelligence.browser.ui.home;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.intelligence.browser.settings.multilanguage.LanguageSettingActivity;
import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.webview.BaseWebView;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.webview.Controller;
import com.intelligence.browser.webview.TabControl.OnTabCountChangeListener;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.ui.setting.BottomSettingplaneDialogFragment;
import com.intelligence.browser.ui.view.PageProgressView;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.view.ToolbarCenterView;
import com.intelligence.browser.webview.Tab;
import com.intelligence.commonlib.tools.BuildUtil;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.componentlib.badge.BadgeView;
import com.intelligence.news.hotword.HotWordHttpRequest;

public class ToolBar extends FrameLayout implements View.OnClickListener, OnTabCountChangeListener,
        BaseWebView.OnScrollChangedListener, View.OnLongClickListener , HotWordHttpRequest.RefreshHotWordListener {
    private static final int PROGRESS_MAX = 100;
    public static final int SCROLL_ANIMATOR_TIME = 200;

    public static final int STATE_DOWNING = 1;
    public static final int STATE_DOWN = 2; //Toolbar 展开状态
    public static final int STATE_UPPING = 3;
    public static final int STATE_UP = 4; //Toolbar 收起状态
    private int mState = STATE_DOWN;
    private boolean mIsTouching;

    private RelativeLayout mMenuButton;
    private FrameLayout mTabSwitcherButton;
    private TextView mTabPageNumber;
    private UiController mUiController;
    private boolean mIsShowToolBar;
    private Animation mShowOrHideAnimation;
    private ImageView mMenuButtonID, mTabSwitcherButtonID;

    private PageProgressView mProgress;
    private boolean mInLoad;
    private Animator mScrollAnimator;
    private boolean mIsDownScrollAnimator;
    private boolean mIsUpScrollAnimator;
    private int mScrollDistance;
    private BaseWebView mWebView;
    private static final int SCROLLING_NO_DIRECTION = 0;
    private static final int SCROLLING_VERTICAL = 1;
    private int mScrollDirection = SCROLLING_NO_DIRECTION;
    private ToolbarCenterView mToolbarCenterView;
    private boolean mIsSearchResultPage = false;
    private boolean mIsPageLoading; //有些网址会重定向
    private boolean mPageFinishedAnimation; //网页加载结束动画, true正在做动画

    private boolean mIsDoneScrollAnimation;
    private BadgeView mNavigationBadge;
//    private BadgeView mNewsBadge;
//    private BadgeView mWebNewsBadge;
    private LinearLayout mHomeToolbar;

    private LinearLayout mWebviewToolbar;
    private FrameLayout mWebviewHome;
    private FrameLayout mWebviewTab;
    private RelativeLayout mWebviewMenu;
    private TextView mWebviewTabNumber;
    private FrameLayout mNewsFragment;
    private FrameLayout mVoice;
    private FrameLayout mHomeWebsiteNavigation;
    private View mAllSitesLayout;

    private View mWebviewBackLayout;

    public ToolBar(Context context) {
        super(context);
    }

    public ToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initLayout();
        setOnClickListener(this);
    }

    private void initLayout() {
        mMenuButton = findViewById(R.id.menu_toolbar);
        mHomeToolbar = findViewById(R.id.browser_home_toolbar);

        mNavigationBadge = findViewById(R.id.toolbar_navigation_red);
        mNewsFragment = findViewById(R.id.toolbar_navigation);
        mNewsFragment.setOnClickListener(this);
//        mNewsBadge = findViewById(R.id.toolbar_news_red);
//        mWebNewsBadge = findViewById(R.id.browser_toolbar_news_red);
//        mNavigationBadge.showCircleDotBadge();
        long time = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_NEWS_TODAY, 0l);
        if (!DateUtils.isToday(time) && BrowserSettings.getInstance().getRedPointNotifiction()) {
//            mNewsBadge.showCircleDotBadge();
//            mWebNewsBadge.showCircleDotBadge();
        }
        mTabSwitcherButton = findViewById(R.id.tabswitcher_toolbar);
        mTabPageNumber = findViewById(R.id.page_number_tab_id);
        mVoice = findViewById(R.id.toolbar_voice);
        mVoice.setOnClickListener(this);
        mTabSwitcherButton.setOnClickListener(this);
        mTabSwitcherButton.setOnLongClickListener(this);
        mMenuButtonID = findViewById(R.id.menu_toolbar_id);
        mTabSwitcherButtonID = findViewById(R.id.tabswitcher_toolbar_id);
        mToolbarCenterView = findViewById(R.id.toolbar_center_view);
        mHomeWebsiteNavigation = findViewById(R.id.toolbar_channel);
        mHomeWebsiteNavigation.setOnClickListener(this);
        mProgress = findViewById(R.id.progress_view);


        mWebviewBackLayout = findViewById(R.id.browser_webview_back_layout);
        mBackIcon = findViewById(R.id.browser_webview_back);
        mForWardIcon = findViewById(R.id.browser_webview_forward);
        mBackIcon.setOnClickListener(this);
        mForWardIcon.setOnClickListener(this);
        mForWardIcon.setAlpha(0.1f);

        mMenuButton.setOnClickListener(this);
        mToolbarCenterView.setOnItemClickListener(this);
        setOnClickListener(this);

        initWebViewToolbar();
        HotWordHttpRequest.registerListener(this);

        hideHomeButton();
    }

    private void hideHomeButton(){
        mNewsFragment.setVisibility(VISIBLE);
        mVoice.setVisibility(GONE);
        mHomeWebsiteNavigation.setVisibility(VISIBLE);
        setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(), ScreenUtils.dpToPxInt(getContext(),7));
    }

    private ImageView mSettingIcon;
    private ImageView mTabSwitcherIcon;
    private ImageView mForWardIcon;
    private ImageView mBackIcon;
    private ImageView mHomeIcon;
    private ImageView mNavigationIcon;
    private ImageView mDownloadIcon;
    private ImageView mAllSitesIcon;

    private void initWebViewToolbar(){
        mSettingIcon = findViewById(R.id.browser_webview_menu_toolbar_id);
        mTabSwitcherIcon = findViewById(R.id.browser_tabswitcher_toolbar_id);
        mHomeIcon = findViewById(R.id.browser_webview_toolbar_home_icon);
        mNavigationIcon = findViewById(R.id.navigation_toolbar_id);
        mDownloadIcon = findViewById(R.id.channel_toolbar_id);
        mAllSitesIcon = findViewById(R.id.allsites_toolbar_id);

        ImageUtils.tinyIconColor(mNavigationIcon);
        ImageUtils.tinyIconColor(mDownloadIcon);
//        ImageUtils.tinyIconColor(mAllSitesIcon);

        ImageUtils.tinyIconColor(mSettingIcon);
        ImageUtils.tinyIconColor(mTabSwitcherIcon);
        ImageUtils.tinyIconColor(mForWardIcon);
        ImageUtils.tinyIconColor(mBackIcon);
        ImageUtils.tinyIconColor(mHomeIcon);

        mWebviewToolbar = findViewById(R.id.browser_webview_toolbar);
        mWebviewHome = findViewById(R.id.browser_webview_toolbar_home);
        mWebviewHome.setOnClickListener(this);
        mWebviewTab = findViewById(R.id.browser_tabswitcher_toolbar);
        mWebviewTabNumber = findViewById(R.id.browser_page_number_tab_id);
        mWebviewMenu = findViewById(R.id.browser_webview_menu_toolbar);
        mAllSitesLayout = findViewById(R.id.toolbar_allsites);
        mAllSitesLayout.setOnClickListener(this);
        mWebviewMenu.setOnClickListener(this);
        mWebviewTab.setOnClickListener(this);

        mWebviewHome.setClickable(true);
        mWebviewTab.setClickable(true);
        mWebviewMenu.setClickable(true);
    }

    public void setUicontroller(UiController uiController) {
        mUiController = uiController;
        mUiController.getTabControl().registerTabChangeListener(this);
        if (mToolbarCenterView != null) {
            mToolbarCenterView.setUiController(uiController);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setUrlTitle(Tab tab) {
        if (!tab.isNativePage()) {
            String url = tab.getUrl();
            String title = tab.getTitle();
            if (TextUtils.isEmpty(title)) {
                title = url;
            }
            if (tab.inForeground()) {
                mToolbarCenterView.setUrlTitle(title);
            }
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (view.getVisibility() != VISIBLE) {
            return;
        }
        if ((id == R.id.tool_bar || id == R.id.title_loading_view) && mState == STATE_UP) {
            scrollAnimation(DIRECTION.DOWN);
            if (mUiController != null && mUiController.getUi() != null) {
                ((BaseUi)mUiController.getUi()).changeWebViewHeight();
            }
            return;
        }
        if ((id == R.id.tabswitcher_toolbar || id == R.id.menu_toolbar) && mState == STATE_UP) {
            return;
        }
        switch (id) {
            case R.id.menu_toolbar:
            case R.id.browser_webview_menu_toolbar:
                BottomSettingplaneDialogFragment bottomDialogFragment = new BottomSettingplaneDialogFragment();
                bottomDialogFragment.setItemClickListener(mUiController);
                mUiController.showCommonMenu(bottomDialogFragment);
                break;
            case R.id.browser_webview_toolbar_home:
                ((BrowserPhoneUi) mUiController.getUi()).panelSwitchHome(mUiController.getTabControl()
                        .getCurrentPosition(), true);
                break;
            case R.id.browser_webview_back:
                if(mUiController != null) {
                    mUiController.goBack();
                }
                break;
            case R.id.browser_webview_forward:
                if(mUiController != null) {
                    mUiController.goForward();
                }
                break;
            case R.id.toolbar_channel:
                ActivityUtils.startDownloadActivity((Activity) getContext(), 0);
                break;
            case R.id.toolbar_allsites:
//                LanguageSettingActivity.launch((Activity) getContext());
                SchemeUtil.jumpToScheme(getContext(),SchemeUtil.BROWSER_SCHEME_PATH_NEWS);
                break;

//                if(SharedPreferencesUtils.getRecommonWebsites()) {
//                    SchemeUtil.jumpToScheme(getContext(), SchemeUtil.BROWSER_SCHEME_PATH_NEWS);
//                    mNewsBadge.setVisibility(GONE);
//                    mWebNewsBadge.setVisibility(GONE);
//                }else {
//                    if (mUiController != null) {
//                        mUiController.shareCurrentPage();
//                    }
//                }
////                WebsiteNavigationFragment websiteNavigationFragment = new WebsiteNavigationFragment();
////                mUiController.showNavigationPage(websiteNavigationFragment);
//                break;
            case R.id.toolbar_navigation:
//                NewsFragment newsFragment = new NewsFragment();
//                mUiController.showNavigationPage(newsFragment);
//                SchemeUtil.jumpHotWorsPage(getContext(),0);
//                mNavigationBadge.setVisibility(GONE);
//                mUiController.showTools(true);
                ActivityUtils.startComboViewActivity((Activity) getContext(), Controller.COMBO_VIEW, 0, 1);
                break;
            case R.id.toolbar_voice:
                mUiController.startVoiceRecognizer();
                break;
            default:
                mUiController.onToolBarItemClick(view.getId());
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }

    @Override
    public void onTabCountUpdate(int tabCount) {
        DisplayUtil.resetTabSwitcherTextSize(mTabPageNumber, mUiController.getTabControl().getTabCount());
        DisplayUtil.resetTabSwitcherTextSize(mWebviewTabNumber, mUiController.getTabControl().getTabCount());
    }

    public void setToolbarStyle(boolean incognito, boolean isNativePage) {
        setBackgroundResource(0);
        if(isNativePage){
            SharedPreferencesUtils.put(BrowserPhoneUi.BROWSER_LAST_TIME_URL,"");
            mUiController.showWebViewPage(false);
        }else {
            SharedPreferencesUtils.put(BrowserPhoneUi.BROWSER_LAST_TIME_URL,mUiController.getCurrentTab().getUrl());
            mUiController.showWebViewPage(true);
        }
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.web_view_toolbar_background_color));
//        if (incognito) {
//            mTabSwitcherButtonID.setImageResource(R.drawable.ic_browser_incognito_label);
//            mTabPageNumber.setTextColor(Color.WHITE);
//            if (Build.VERSION.SDK_INT < BuildUtil.VERSION_CODES.LOLLIPOP) {
//                mTabSwitcherButton.setBackgroundResource(R.drawable.browser_common_menu_item_bg_incognito);
//            }
//        } else {
            mTabPageNumber.setTextColor(getContext().getResources().getColor(R.color.toolbar_page_number_color));
            mTabSwitcherButtonID.setImageResource(R.drawable
                    .browser_label);
            if (Build.VERSION.SDK_INT < BuildUtil.VERSION_CODES.LOLLIPOP) {
                mTabSwitcherButton.setBackgroundResource(R.drawable.browser_common_menu_item_bg);
            }
//        }
//        if (incognito) {
//            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.toolbar_incognito_background_color));
//        } else {
            if (isNativePage) {
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
            } else {
                setBackgroundColor(ContextCompat.getColor(getContext(), R.color.web_view_toolbar_background_color));
            }
//        }

        updateLayout(isNativePage);
        mToolbarCenterView.updateStyle(incognito);
        if (isNativePage) {
            mProgress.setVisibility(GONE);
        } else if (!mIsPageLoading) {
            mProgress.setVisibility(GONE);
        } else {
            mProgress.setVisibility(VISIBLE);
        }
    }

    public boolean updateToolBarVisibility() {
        return updateToolBarVisibility(false, false);
    }

    /**
     * @param animate          是否做toolbar的动画
     * @param isShowCommonMenu //是否需要隐藏弹出菜单
     * @return
     */
    public boolean updateToolBarVisibility(boolean animate, boolean isShowCommonMenu) {
        mIsShowToolBar = true;
        if (((BrowserPhoneUi) mUiController.getUi()).showingNavScreen() && ((BrowserPhoneUi) mUiController.getUi())
                .showingNavScreenForExit()) {
            //navscreen界面
            mIsShowToolBar = false;
        } else if (((BrowserPhoneUi) mUiController.getUi()).showingNavScreen() && !((BrowserPhoneUi) mUiController.getUi())
                .showingNavScreenForExit()) {
            mIsShowToolBar = true;
        }

        if (mShowOrHideAnimation != null) {
            mShowOrHideAnimation.cancel();
            clearAnimation();
            mShowOrHideAnimation.setAnimationListener(null);
            setAnimation(null);
        }
        if (mIsShowToolBar) {
            if (mUiController.getCurrentTab() != null) {
                setToolbarStyle(mUiController.getTabControl().isIncognitoShowing(), mUiController.getCurrentTab()
                        .isNativePage());
            }
            if (animate) {
                mTabSwitcherButton.clearAnimation();
                mMenuButton.clearAnimation();
                mShowOrHideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.browser_fade_in_bottom);
                mShowOrHideAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mShowOrHideAnimation = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                startAnimation(mShowOrHideAnimation);
            } else {
                setVisibility(View.VISIBLE);
            }

        } else {
            if (animate) {
                mTabSwitcherButton.clearAnimation();
                mMenuButton.clearAnimation();
                mShowOrHideAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.browser_fade_out_bottom);
                mShowOrHideAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        setVisibility(View.GONE);
                        mShowOrHideAnimation = null;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                startAnimation(mShowOrHideAnimation);
            } else {
                setVisibility(View.GONE);
            }
        }
        return mIsShowToolBar;
    }

    public boolean isShowToolBar() {
        return mIsShowToolBar;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    public void switchHome() {
        mIsSearchResultPage = false;
        clearAnimation();
        setVisibility(VISIBLE);
        updateLayout(true);
        doUpAnimator(DIRECTION.DOWN, true);
    }

    public boolean isProcessing(float x, float y) {
        Rect outRect = new Rect();
        int[] location = new int[2];
        getDrawingRect(outRect);
        getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return mScrollDirection == SCROLLING_VERTICAL || outRect.contains((int) x, (int) y);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    public void setProgress(int newProgress) {
        if (mProgress == null
                || (mUiController != null
                && mUiController.getCurrentTab() != null
                && mUiController.getCurrentTab().getTitle().equals(getContext().getResources().getString(R.string
                .home_page)))) {
            return;
        }
        if (mUiController != null
                && mUiController.getCurrentTab() != null
                && mUiController.getCurrentTab().isNativePage()) {
            mProgress.setVisibility(GONE);
        } else if (newProgress >= PROGRESS_MAX) {
            mProgress.setVisibility(View.GONE);
            mProgress.setProgress(PageProgressView.MAX_PROGRESS);
            // check if needs to be hidden
        } else {
            if (!mInLoad) {
                mProgress.setVisibility(View.VISIBLE);
                mInLoad = true;
            }
            mProgress.setProgress(newProgress * PageProgressView.MAX_PROGRESS
                    / PROGRESS_MAX);
        }
    }

    public void setWebView(BaseWebView webView) {
        if (webView == null) {
            return;
        }
        mWebView = webView;
        mWebView.setOnScrollChangedListener(this);
    }

    public enum DIRECTION {
        UP, DOWN
    }

    public void resetDirection() {
        mIsTouching = false;
    }

    public int getToolbarState() {
        return mState;
    }

    public void doScrollAnimation(int downY, int lastMoveY, int moveY) {
        if ((mToolbarCenterView != null && mIsSearchResultPage)
                || mPageFinishedAnimation
                || (mUiController != null && mUiController.getUi() != null
                && ((BrowserPhoneUi) mUiController.getUi()).showingNavScreenForExit())) {
            return;
        }
        if (mScrollDistance == 0) {
            mScrollDistance = getContext().getResources().getDimensionPixelOffset(R.dimen
                    .bottom_toolbar_scroll_animator_distance);
        }
        DIRECTION direction = null;
        int distance = moveY - downY;
        if (mState == STATE_UP && moveY - lastMoveY < 0 && mIsTouching) {
            direction = DIRECTION.UP;
        } else if (mState == STATE_DOWN && moveY - lastMoveY > 0 && mIsTouching) {
            direction = DIRECTION.DOWN;
        } else if (mState == STATE_UP && moveY - lastMoveY > 0 && mIsTouching) {
            direction = DIRECTION.DOWN;
        } else if (mState == STATE_DOWN && moveY - lastMoveY < 0 && mIsTouching) {
            direction = DIRECTION.UP;
        } else if (distance > mScrollDistance && !mIsTouching) {
            direction = DIRECTION.DOWN;
        } else if (distance < -mScrollDistance && !mIsTouching) { //&& mScrollState != STATE_DOWN && mScrollState !=
            direction = DIRECTION.UP;
        }
        scrollAnimation(direction);
    }

    public void doTouchScrollAnimation(int downTouchY, int lastTouchY, int moveTouchY) {
        if (mPageFinishedAnimation
                || (mUiController != null && mUiController.getUi() != null
                && ((BrowserPhoneUi) mUiController.getUi()).showingNavScreenForExit())) {
            return;
        }
        if (mScrollDistance == 0) {
            mScrollDistance = getContext().getResources().getDimensionPixelOffset(R.dimen
                    .bottom_toolbar_scroll_animator_distance);
        }
        DIRECTION direction = null;
        int distance = moveTouchY - downTouchY;
        if (distance > mScrollDistance) {
            direction = DIRECTION.DOWN;
        } else if (distance < -mScrollDistance) {
            direction = DIRECTION.UP;
        }
        scrollAnimation(direction);
    }

    public void scrollAnimation(DIRECTION direction) {
        if (direction == null || (mToolbarCenterView != null && mIsSearchResultPage && mState != STATE_UP)
                || (mUiController != null && mUiController.getUi() != null
                && ((BrowserPhoneUi) mUiController.getUi()).showingNavScreenForExit())) {
            return;
        }
        mIsDoneScrollAnimation = true;
        switch (direction) {
            case UP:
                doDownAnimation(direction);
                break;
            case DOWN:
                doUpAnimator(direction);
                break;
        }
    }

    private void doDownAnimation(DIRECTION direction) {
        if (mIsUpScrollAnimator || mState == STATE_UP || mState == STATE_UPPING
                || mIsDownScrollAnimator || mPageFinishedAnimation) {
            return;
        }
        mScrollAnimator = ObjectAnimator.ofFloat(this,
                "translationY",
                0, mScrollDistance);
        setUpAnimator(DIRECTION.UP, false);
        startTitleAnimator(direction, false);
        mScrollAnimator.start();
    }

    private void doUpAnimator(DIRECTION direction) {
        doUpAnimator(direction, false);
    }

    private void doUpAnimator(DIRECTION direction, boolean isHomePage) {
        if ((mIsDownScrollAnimator || mState == STATE_DOWN || mState == STATE_DOWNING
                || mIsUpScrollAnimator || mPageFinishedAnimation)
                && (mUiController.getCurrentTab() != null && !mUiController.getCurrentTab().isNativePage())) {
            return;
        }
        mScrollAnimator = ObjectAnimator.ofFloat(this,
                "translationY",
                mScrollDistance, 0);
        setUpAnimator(DIRECTION.DOWN, isHomePage);
        startTitleAnimator(direction, isHomePage);
        mScrollAnimator.start();
    }

    private void setUpAnimator(final DIRECTION direction, boolean isHomePage) {
        if (direction == null) {
            return;
        }
        if (isHomePage) {
            mScrollAnimator.setDuration(0); //不需要动画，但是需要view升上来
        } else {
            mScrollAnimator.setDuration(SCROLL_ANIMATOR_TIME);
        }
        mScrollAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (direction == DIRECTION.DOWN) {
                    mIsDownScrollAnimator = true;
                    mIsUpScrollAnimator = false;
                    mState = STATE_DOWNING;
                } else if (direction == DIRECTION.UP) {
                    mIsDownScrollAnimator = false;
                    mIsUpScrollAnimator = true;
                    mState = STATE_UPPING;
                }
                mIsTouching = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsDownScrollAnimator = false;
                mIsUpScrollAnimator = false;
                if (direction == DIRECTION.UP) {
                    mState = STATE_UP;
//                    mUiController.toobarunfold(1);
                } else if (direction == DIRECTION.DOWN) {
//                    mUiController.toobarunfold(0);
                    mState = STATE_DOWN;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void startTitleAnimator(final DIRECTION direction, boolean isHomePage) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentValue = (Integer)animator.getAnimatedValue();
                float fraction = currentValue / 100f;
                mToolbarCenterView.startScrollAnimation(direction, fraction);
                switch (direction) {
                    case DOWN:
                        mUiController.toobarunfold(1.0f - fraction);
                        mWebviewHome.setAlpha(fraction);
                        mWebviewTab.setAlpha(fraction);
//                        mWebviewNavigation.setAlpha(fraction);
                        mWebviewMenu.setAlpha(fraction);
                        break;
                    case UP:
                        mUiController.toobarunfold(fraction);
                        fraction = 1.0f - fraction;
                        mWebviewHome.setAlpha(fraction);
                        mWebviewTab.setAlpha(fraction);
//                        mWebviewNavigation.setAlpha(fraction);
                        mWebviewMenu.setAlpha(fraction);
                        break;
                }
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }
            @Override
            public void onAnimationEnd(Animator animator) {
                switch (direction) {
                    case DOWN:
//                        if (mState == STATE_DOWN) {
//                            return;
//                        }
                        mWebviewHome.setAlpha(1);
                        mWebviewTab.setAlpha(1);
//                        mWebviewNavigation.setAlpha(1);
                        mWebviewMenu.setAlpha(1);
                        mWebviewHome.setClickable(true);
                        mWebviewTab.setClickable(true);
                        mBackIcon.setClickable(true);
                        mForWardIcon.setClickable(true);
//                        mWebviewNavigation.setClickable(true);
                        mWebviewMenu.setClickable(true);
                        mWebviewBackLayout.setAlpha(1);

                        break;
                    case UP:
//                        if (mState == STATE_UP) {
//                            return;
//                        }
                        mWebviewHome.setClickable(false);
                        mWebviewTab.setClickable(false);
//                        mWebviewNavigation.setClickable(false);
                        mWebviewMenu.setClickable(false);
                        mBackIcon.setClickable(false);
                        mForWardIcon.setClickable(false);
                        mWebviewHome.setAlpha(0);
                        mWebviewTab.setAlpha(0);
                        mWebviewBackLayout.setAlpha(0);
//                        mWebviewNavigation.setAlpha(0);
                        mWebviewMenu.setAlpha(0);
                        break;
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.setDuration(200).start();
    }

    @Override
    public void onPageTop(boolean istop) {
        if (mUiController != null && mUiController.getUi() != null) {
            ((BaseUi)mUiController.getUi()).changeWebViewIsTop(istop);
        }
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        try {
            if (mScrollDistance == 0) {
                mScrollDistance = getContext().getResources().getDimensionPixelOffset(R.dimen
                        .bottom_toolbar_scroll_animator_distance);
            }

            if (t == 0) {
                scrollAnimation(DIRECTION.DOWN);
                if (mUiController != null && mUiController.getUi() != null) {
                    ((BaseUi)mUiController.getUi()).changeWebViewHeight();
                }
            } else if (mWebView.getContentHeight() * mWebView.getScaleY() - (mWebView.getHeight() + t) == 0) {
                scrollAnimation(DIRECTION.UP);
            }
        } catch (Exception e) {
        }
    }

    private boolean isSearchResultPage() {
        return false;
    }

    public void onPageLoadStarted(Tab tab) {
        if (tab != null && tab.isNativePage()) {
            return;
        }

        scrollAnimation(DIRECTION.DOWN);
        ((BaseUi) mUiController.getUi()).changeWebViewHeight();
        mIsPageLoading = true;

        mMenuButtonID.setVisibility(VISIBLE);
        mProgress.setVisibility(VISIBLE);
        mInLoad = true;
        mForWardIcon.setAlpha(0.1f);

    }

    public void onPageLoadFinished(Tab tab) {
        if (!mIsPageLoading) {
            return;
        }
        mIsPageLoading = false;
        mInLoad = false;
        mPageFinishedAnimation = true;

        if (tab != null && !tab.isNativePage()) {
            mToolbarCenterView.onPageLoadFinished(mIsSearchResultPage, tab);
        }
        mPageFinishedAnimation = false;
        mProgress.setVisibility(GONE);
        if (mUiController != null) {
            mForWardIcon.setImageResource(R.drawable.browser_webview_forward);
            if(mUiController.canGoForward()){
                mForWardIcon.setAlpha(1.0f);
            }else {
                mForWardIcon.setAlpha(0.1f);
            }

        }
    }

    public void onPageLoadStopped(Tab tab) {
        mIsPageLoading = false;
        mMenuButtonID.setVisibility(VISIBLE);
    }

    public boolean getIsSearchResultPage() {
        return mIsSearchResultPage;
    }

    public void updateLayout(boolean isNativePage) {
        setHomeStyle(isNativePage);
    }

    public void updateToolbarBtnState() {
        mIsSearchResultPage = isSearchResultPage();
        mToolbarCenterView.setIsSearchResultPage(mIsSearchResultPage);
        Tab tab = mUiController == null ? null : mUiController.getCurrentTab();
        if (tab == null) {
            return;
        }
        if (tab.inPageLoad() && !tab.isNativePage()) {
            mInLoad = true;
            mProgress.setVisibility(VISIBLE);
            mIsPageLoading = true;
            setProgress(tab.getLoadProgress());
        } else {
            mInLoad = false;
            mIsPageLoading = false;
            mProgress.setVisibility(GONE);
        }
    }

    public void setIsDoneScrollAnimation(boolean isDoneScrollAnimation) {
        mIsDoneScrollAnimation = isDoneScrollAnimation;
    }

    public boolean getIsDoneScrollAnimation() {
        return mIsDoneScrollAnimation;
    }

    public int getToolBarCenterViewHeight() {
        switch (getToolbarState()) {
            case STATE_DOWN:
            case STATE_DOWNING:
                return getHeight();
            case STATE_UP:
            case STATE_UPPING:
                return getHeight() / 2;
        }

        return 0;
    }

    private void setHomeStyle(boolean isHome) {
        mWebviewToolbar.setVisibility(isHome ? GONE : VISIBLE);
        mHomeToolbar.setVisibility(isHome ? VISIBLE : GONE);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), ScreenUtils.dpToPxInt(getContext(), isHome ? 7 : 0));
    }

    public void onResume(){
        if(!BrowserSettings.getInstance().getRedPointNotifiction()){
            mNavigationBadge.setVisibility(GONE);
//            mNewsBadge.setVisibility(GONE);
//            mWebNewsBadge.setVisibility(GONE);
        } else {
            long time = (long)SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_NEWS_TODAY, 0l);
            if (!DateUtils.isToday(time) && BrowserSettings.getInstance().getRedPointNotifiction()) {
//                mNewsBadge.showCircleDotBadge();
//                mWebNewsBadge.showCircleDotBadge();
                mNavigationBadge.setVisibility(VISIBLE);
//                mNewsBadge.setVisibility(VISIBLE);
//                mWebNewsBadge.setVisibility(VISIBLE);
            }
        }
    }
    @Override
    public void onRefreshHotWord() {
        if(!BrowserSettings.getInstance().getRedPointNotifiction()){
            return;
        }
        if(mNavigationBadge != null){
            mNavigationBadge.setVisibility(VISIBLE);
            mNavigationBadge.showTextBadge("新",true);
        }
    }
}

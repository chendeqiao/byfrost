
package com.intelligence.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.R;
import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.ui.home.ToolBar;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.ui.widget.AnimationListener;
import com.intelligence.commonlib.tools.ScreenUtils;

public class ScrollFrameLayout extends FrameLayout {
    public static final int TOUCH_SHAKE = 10; // 防抖动

    private ToolBar mToolBar;
    private UiController mUiController;
    int mDownScrollY, mScrollY, mLastScrollMoveY;
    int mDownTouchY, mLastTouchY, mTouchY;
    private ImageView mSlideLeft, mSlideRight;
//    private ImageView mSlideLeft;
    private int mSlideDistance;
    private boolean mSlideSuccess = false;
    private IScrollListener mScrollListener;
    private float mDragLastTouchY, mDragLastTouchX;
    private static final int DRAG_ANIMATOR_TIME = 50;
    private boolean mDraggingFlag = false;
    private boolean mDragDisable = false;
    private int mDragDistanceX, mDragDistanceY;
    private int mTouchSlop;
    private int mIconWidth;

    public ScrollFrameLayout(Context context) {
        super(context);
    }

    public ScrollFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mToolBar = findViewById(R.id.bottom_bar).findViewById(R.id.tool_bar);

        mSlideLeft = findViewById(R.id.slide_left);
        mSlideRight = findViewById(R.id.slide_right);
        mIconWidth = getResources().getDimensionPixelSize(R.dimen.scroll_swipe_icon_width);
        mSlideDistance = mIconWidth * 2 - ScreenUtils.dpToPxInt(getContext(),5);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop() * 10;
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    public void setUiController(UiController controller) {
        mUiController = controller;
    }

    boolean isSupportSwipe;
    public void onResume(){
        isSupportSwipe = BrowserSettings.getInstance().getSwipeState();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mToolBar == null || !mToolBar.isShown() || !mToolBar.isProcessing(ev.getX(), ev.getY())) {
            handleScrollEvent(ev);
        }
        handleSwipeEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    private boolean isCanSwipe(){
        return !mUiController.getCurrentTab().isNativePage() && isSupportSwipe;
    }

    private void handleSwipeEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            mDragDisable = true;
            if (mDraggingFlag) {
                hideDragView();
            }
            return;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDragLastTouchX = ev.getX();
                mDragLastTouchY = ev.getY();
                mDraggingFlag = false;
                mDragDistanceX = 0;
                mDragDistanceY = 0;
                mDragDisable = !isInDragForwardBackRect(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                final float dx = ev.getX() - mDragLastTouchX;
                final float dy = ev.getY() - mDragLastTouchY;
                mDragDistanceX += dx;
                mDragDistanceY += dy;
                if (Math.abs(mDragDistanceY) > 1 || Math.abs(mDragDistanceX) > 1) {
                    if (Math.abs(mDragDistanceY) > mSlideDistance){
                        mDragDisable = true;
                        if (mDraggingFlag) {
                            hideDragView();
                        }
                        return;
                    } else if (!mDraggingFlag && Math.abs(dx) < Math.abs(dy)) {
                        mDragDisable = true;
                    }
                    if (!mDragDisable) {
                        mDraggingFlag = true;
                        if (mDragDistanceX > 0) {
                            if (mUiController.canGoBack()) {
                                mSlideRight.setVisibility(VISIBLE);
                                int moveX = mDragDistanceX / 2;
                                if (moveX > mIconWidth) {
                                    moveX = mIconWidth;
                                }
                                mSlideRight.setTranslationX(moveX);
                                mSlideLeft.setVisibility(GONE);
                                mSlideSuccess = mDragDistanceX > mSlideDistance;
                            }
                        } else {
                            if (mUiController.canGoForward()) {
                                mSlideLeft.setVisibility(VISIBLE);
                                int moveX = mDragDistanceX / 2;
                                if (-moveX > mIconWidth) {
                                    moveX = -mIconWidth;
                                }
                                mSlideLeft.setTranslationX(moveX);
                                mSlideRight.setVisibility(GONE);
                                mSlideSuccess = -mDragDistanceX > mSlideDistance;
                            }else {
                                mSlideLeft.setVisibility(GONE);
                            }
                        }
                        invalidate();
                    }
                }
                mDragLastTouchX = ev.getX();
                mDragLastTouchY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mDraggingFlag) {
                    hideDragView();
                }
                mDragLastTouchX = ev.getX();
                mDragLastTouchY = ev.getY();
                break;
        }
    }

    private void hideDragView() {
        AlphaAnimation alphaAnimation =  new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(DRAG_ANIMATOR_TIME);
        if (!mDragDisable) {
            if (mSlideRight.getVisibility() == VISIBLE) {
                alphaAnimation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSlideLeft.setVisibility(GONE);
                        mSlideRight.setVisibility(GONE);
                        if (mSlideSuccess) {
                            mUiController.goBack();
                        }
                    }
                });
                mSlideRight.startAnimation(alphaAnimation);
            } else if (mSlideLeft.getVisibility() == VISIBLE) {
                if (mSlideLeft.getVisibility() == VISIBLE) {
                    alphaAnimation.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mSlideLeft.setVisibility(GONE);
                            mSlideRight.setVisibility(GONE);
                            if (mSlideSuccess) {
                                mUiController.goForward();
                            }
                        }
                    });
                    mSlideLeft.startAnimation(alphaAnimation);
                }
            } else {
                alphaAnimation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSlideLeft.setVisibility(GONE);
                        mSlideRight.setVisibility(GONE);
                    }
                });
                if (mSlideRight.getVisibility() == VISIBLE) {
                    mSlideRight.startAnimation(alphaAnimation);
                } else {
                    if(mSlideLeft.getVisibility() == VISIBLE) {
                        mSlideLeft.startAnimation(alphaAnimation);
                    }
                }
            }
            mDragDisable = true;
            mDraggingFlag = false;
        }
    }


    private boolean isInDragForwardBackRect(MotionEvent ev) {
        if(!isCanSwipe()){
            return false;
        }
        if (mUiController.getUi() instanceof BrowserPhoneUi && ((BrowserPhoneUi)  mUiController.getUi()).showingNavScreen()) {
            return false;
        }
        return (mToolBar != null && ev.getY() < mToolBar.getTop()) && (ev.getX() < mTouchSlop || ev.getX() + mTouchSlop > getMeasuredWidth());
    }

    private void handleScrollEvent(MotionEvent ev) {
        if (mUiController == null || mToolBar == null) {
            return;
        }

        Tab current = mUiController.getCurrentTab();
        if (current != null && !current.isNativePage() && current.getWebView() != null) {
            WebView webView = current.getWebView().getBaseWebView();
            if (webView == null) {
                return;
            }
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownScrollY = webView.getScrollY();
                    mLastScrollMoveY = mDownScrollY;
                    mScrollY = mDownScrollY;
                    mToolBar.resetDirection();
                    //网页长度不够的情况下
                    mDownTouchY = (int) ev.getRawY();
                    mLastTouchY = mDownTouchY;
                    mTouchY = mDownTouchY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(webView.getScrollY() - mLastScrollMoveY) > TOUCH_SHAKE) {
                        mLastScrollMoveY = mScrollY;
                        mScrollY = webView.getScrollY();
                    }
                    if (Math.abs(ev.getRawY() - mLastTouchY) > TOUCH_SHAKE) {
                        mLastTouchY = mTouchY;
                        mTouchY = (int) ev.getRawY();
                    }
                    if (Math.abs(mTouchY - mLastTouchY) > TOUCH_SHAKE && webView.getContentHeight() * webView.getScale() <= webView.getHeight()) {
                        //网页长度不够的情况
                        mToolBar.doTouchScrollAnimation(mDownTouchY, mLastTouchY, mTouchY);
                    } else if (Math.abs(webView.getScrollY() - mLastScrollMoveY) > TOUCH_SHAKE || webView.getScrollY() == 0
                            || (webView.getContentHeight() * webView.getScale() - (webView.getHeight() + webView.getScrollY()) == 0)) {
                        mToolBar.doScrollAnimation(mDownTouchY, mLastTouchY, mTouchY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mScrollListener != null && mToolBar.getIsDoneScrollAnimation()) {
                        mScrollListener.onToolbarStateChanged();
                        mToolBar.setIsDoneScrollAnimation(false);
                    }
            }
        }
    }

    public void registerScrollListener(IScrollListener scrollListener) {
        this.mScrollListener = scrollListener;
    }

    public void unRegisterScrollListener() {
        this.mScrollListener = null;
    }

    public interface IScrollListener {
        void onToolbarStateChanged();
    }
}

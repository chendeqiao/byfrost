package com.intelligence.browser.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.ScreenUtils;

public class InputWordDeleteView extends FrameLayout implements View.OnClickListener {
    private ViewGroup.LayoutParams mRootViewlayoutParams;
    private MarginLayoutParams mLiveContentLayoutParams;
    private onDeleteWordClick mOnLiveAccessClick;
    public static final int STYLE_SEACH_PAGE = 001;
    public static final int STYLE_TABS_PAGE = 002;
    private RelativeLayout mRootView;
    private int mRootViewWidthDiff;
    private TextView mLiveContent;
    private boolean mIsClose = true;
    private ImageView mLiveIcon;
    private int mRootLayoutSize = 30;

    public InputWordDeleteView(@NonNull Context context) {
        this(context, null, 0);
    }

    public InputWordDeleteView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOnclickListener(onDeleteWordClick onclickListener){
        mOnLiveAccessClick = onclickListener;
    }

    public InputWordDeleteView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_input_word_delete_view, this);
        initView(view);
    }

    public void setTipsContent(String content) {
        if (mLiveContent != null) {
            mLiveContent.setText(content);
        }
    }

    private void initView(View view) {
        mRootView = view.findViewById(R.id.article_live_access_rootview);
        mRootView.setOnClickListener(this);
        mLiveContent = view.findViewById(R.id.article_live_access_content);
        mLiveIcon = view.findViewById(R.id.article_live_access_icon);
        mRootViewWidthDiff = ((int)getContext().getResources().getDimension(R.dimen.browser_delete_all_width)) - ScreenUtils.dpToPxInt(getContext(), mRootLayoutSize);
    }

    public void setStyle(int i) {
        if (i == STYLE_TABS_PAGE) {
            mRootLayoutSize = 36;
            mRootView.getLayoutParams().width = ScreenUtils.dpToPxInt(getContext(),mRootLayoutSize);
            mRootView.getLayoutParams().height = ScreenUtils.dpToPxInt(getContext(),mRootLayoutSize);
            mLiveIcon.getLayoutParams().height = ScreenUtils.dpToPxInt(getContext(),24);
            mLiveIcon.getLayoutParams().width = ScreenUtils.dpToPxInt(getContext(),24);
            mLiveContent.setTextSize(12);
            mLiveContent.setTextColor(Color.WHITE);
            mLiveIcon.setBackground(getResources().getDrawable(R.drawable.browser_trashcan));
            mRootViewWidthDiff = (int) getContext().getResources().getDimension(R.dimen.browser_delete_all_width_nav) - ScreenUtils.dpToPxInt(getContext(), mRootLayoutSize);
        }
    }

    private boolean mIsOpenLayout;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.article_live_access_rootview:
                if(mIsOpenLayout) {
                    if (mOnLiveAccessClick != null) {
                        mOnLiveAccessClick.DeleteAllData();
                    }
                }else {
                    startAnim(true);
                    if (mOnLiveAccessClick != null) {
                        mOnLiveAccessClick.clickIcon();
                    }
                }
                break;
        }
    }

    public void restoreState(){
        mIsOpenLayout = false;
        calculationLayoutParams(0);
    }
    public boolean mIsEdit(){
        return mIsOpenLayout;
    }
    public void startAnim(final boolean isclose) {
        if(mIsOpenLayout == isclose){
            return;
        }
        mIsOpenLayout = isclose;
        mIsClose = isclose;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(250);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                calculationLayoutParams(isclose ? animation.getAnimatedFraction() : 1.0f - animation.getAnimatedFraction());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        valueAnimator.start();
    }

    private void calculationLayoutParams(float value) {
        if (mRootViewlayoutParams == null) {
            mRootViewlayoutParams = mRootView.getLayoutParams();
        }
        mRootViewlayoutParams.width = (int) (ScreenUtils.dpToPxInt(getContext(), mRootLayoutSize) + (mRootViewWidthDiff * value));
        mRootView.setLayoutParams(mRootViewlayoutParams);
    }

    public interface onDeleteWordClick {
        void DeleteAllData();

        void clickIcon();
    }
}
package com.intelligence.news.news.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;

/**
 * 场景分发入口 9.13.0
 */
public class AppSitesView extends FrameLayout {
    private static final int FUNCTION_ENTRY_SHADOW_PADDING = 8;
    private static final int FUNCTION_ENTRY_SHADOW_COUNT = 5;
    private LinearLayout mRootView;
    private int mScreenWidth;
    private int mItemWidth;

    public AppSitesView(@NonNull Context context) {
        this(context, null, 0);
    }

    public AppSitesView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppSitesView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_function_entry_new_view, this);
        initView(view);
    }

    private void initView(View view) {
        mRootView = view.findViewById(R.id.function_layout_root);
        initItemView();
    }

    private void initItemView() {
        mScreenWidth = ScreenUtils.getScreenWidth(getContext()) - (ScreenUtils.dpToPxInt(getContext(), 12) * 2);
        mItemWidth = (mScreenWidth) / FUNCTION_ENTRY_SHADOW_COUNT;
        for (int i = 0; i < FUNCTION_ENTRY_SHADOW_COUNT; i++) {
            AppSitesItemView functionEntryItemView = new AppSitesItemView(getContext());
            functionEntryItemView.setLayout(mItemWidth,mItemWidth);
            functionEntryItemView.setVisibility(VISIBLE);
            mRootView.addView(functionEntryItemView);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setData(final ArrayList<WebSiteData> appSites) {
        if(CollectionUtils.isEmpty(appSites)){
            setVisibility(GONE);
            return;
        }
        setViewHint();
        for (int i = 0; i < FUNCTION_ENTRY_SHADOW_COUNT; i++) {
            if (mRootView.getChildCount() > i && mRootView.getChildAt(i) instanceof AppSitesItemView) {
                AppSitesItemView functionEntryItemView = (AppSitesItemView) mRootView.getChildAt(i);
                functionEntryItemView.setData(appSites.get(i),i);
            }
        }
    }

    private void setViewHint() {
        if (mRootView != null && mRootView.getChildCount() > 1) {
            for (int i = 0; i < mRootView.getChildCount(); i++) {
                View view = mRootView.getChildAt(i);
                if (view instanceof AppSitesItemView) {
//                    view.setVisibility(INVISIBLE);
                }
            }
        }
    }
}
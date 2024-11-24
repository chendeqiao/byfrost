package com.intelligence.commonlib.baseUi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;

public class AdvertiseFrameLayout extends FrameLayout {
    private boolean mShowClose;
    private View mAdBottomClose;
    private View mAdTopClose;
    private FrameLayout mAdLayout;

    public AdvertiseFrameLayout(Context context) {
        this(context, null);
    }

    public AdvertiseFrameLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvertiseFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_ad_layout, this);
        initView(view);
    }

    private void initView(View view){
        mAdBottomClose = view.findViewById(R.id.browser_bottom_ad_close);
        mAdTopClose = view.findViewById(R.id.browser_top_ad_close);
        mAdLayout = view.findViewById(R.id.browser_ad);
        mAdBottomClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibleHint(false);
            }
        });
        mAdTopClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibleHint(false);
            }
        });
    }

    private void setVisibleHint(boolean isVisible) {
        setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void addView(View child) {
        mAdLayout.addView(child);
        if (mPosition == 0) {
//            mAdBottomClose.setVisibility(VISIBLE);
        } else if (mPosition == 1) {
//            mAdTopClose.setVisibility(VISIBLE);
        }
    }

    @Override
    public void removeAllViews() {
        mAdLayout.removeAllViews();
    }

    private int mPosition = -1;
    public void setClosePosition(int position){
        mPosition = position;
    }
}

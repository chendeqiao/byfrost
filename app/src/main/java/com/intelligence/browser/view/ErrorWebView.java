package com.intelligence.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;

public class ErrorWebView extends FrameLayout {
    private View mErrorView;
    private OnRefreshListener mOnRefreshListener;
    public interface OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        void onRefresh();
    }

    public void setOnRefreshClick(OnRefreshListener onRefreshListener){
        mOnRefreshListener = onRefreshListener;
    }

    public ErrorWebView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ErrorWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ErrorWebView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_error_view, this);
        initView(view);
    }

    private void initView(View view) {
        mErrorView = view.findViewById(R.id.layout_error_view);
        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnRefreshListener != null){
                    mOnRefreshListener.onRefresh();
                }
            }
        });
    }
}
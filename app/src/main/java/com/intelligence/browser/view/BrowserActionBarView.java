package com.intelligence.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;

public class BrowserActionBarView extends RelativeLayout {

    public BrowserActionBarView(@NonNull Context context) {
        this(context, null, 0);
    }

    public BrowserActionBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrowserActionBarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_browser_actionbar, this);
        initView(view);
    }

    private View mBackLayout;
    private TextView mActionbarTitle;
    private ImageView mRightIcon;
    private ImageView mLeftIcon;
    private void initView(View view) {
        mBackLayout = findViewById(R.id.actionbar_left);
        mLeftIcon = findViewById(R.id.actionbar_left_icon);
        mActionbarTitle = findViewById(R.id.actionbar_title);
        mRightIcon = findViewById(R.id.actionbar_right_icon);
    }

    public TextView getActionbarTitle(){
        return mActionbarTitle;
    }
    public void setTitle(String title){
        mActionbarTitle.setText(title);
    }

    public void setRightIconVisible(){
        if(mRightIcon != null){
            mRightIcon.setVisibility(VISIBLE);
        }
    }

    public void setLeftIconDrawable(int resId){
        if(mLeftIcon != null){
            mLeftIcon.setImageDrawable(getResources().getDrawable(resId));
        }
    }

    public void setOnclickListener(View.OnClickListener onclickListener){
        if(mBackLayout != null){
            mBackLayout.setOnClickListener(onclickListener);
        }
        if (mRightIcon != null) {
            mRightIcon.setOnClickListener(onclickListener);
        }
    }

}

package com.intelligence.news.news.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.news.websites.bean.WebSiteData;

import java.util.ArrayList;

public class WebLableView extends FrameLayout implements View.OnClickListener {
    private OnWebLableClickListener mOnWebLableClickListener;

    public WebLableView(@NonNull Context context,OnWebLableClickListener onClickListener) {
        this(context, null, 0);
        mOnWebLableClickListener = onClickListener;
    }

    public WebLableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebLableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_web_lable_view, this);
        initView(view);
    }

    private TextView mWeblable1;
    private TextView mWeblable2;
    private TextView mWeblable3;
    private TextView mWeblable4;
    private TextView mWeblable5;

    private void initView(View view) {
        mWeblable1 = view.findViewById(R.id.weblabel1);
        mWeblable2 = view.findViewById(R.id.weblabel2);
        mWeblable3 = view.findViewById(R.id.weblabel3);
        mWeblable4 = view.findViewById(R.id.weblabel4);
        mWeblable5 = view.findViewById(R.id.weblabel5);
        mWeblable1.setOnClickListener(this);
        mWeblable2.setOnClickListener(this);
        mWeblable3.setOnClickListener(this);
        mWeblable4.setOnClickListener(this);
        mWeblable5.setOnClickListener(this);
        requestData();
    }

    private void requestData() {
        if (BrowserApplication.getInstance().isSimpleVersion()) {
            setVisibility(GONE);
            return;
        }
        ArrayList<WebSiteData> webSiteData;
        webSiteData = RecommendUrlUtil.getWebsites(getContext(), R.array.weblables2);
        if (webSiteData != null) {
            if (CollectionUtils.isEmpty(webSiteData)) {
                setVisibility(GONE);
            } else {
                setData(webSiteData);
            }
        }
    }

    public interface OnWebLableClickListener{
        void onWebLableClick(View view,WebSiteData webSiteData);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setData(final ArrayList<WebSiteData> browserHistory) {
        for (int i = 0; i < browserHistory.size(); i++) {
            WebSiteData bh = browserHistory.get(i);
            if (i == 0) {
                mWeblable1.setText(bh.title);
                mWeblable1.setTag(bh);
            } else if (i == 1) {
                mWeblable2.setText(bh.title);
                mWeblable2.setTag(bh);
            }else if (i == 2) {
                mWeblable3.setText(bh.title);
                mWeblable3.setTag(bh);
            }else if (i == 3) {
                mWeblable4.setText(bh.title);
                mWeblable4.setTag(bh);
            }else if (i == 4) {
                mWeblable5.setText(bh.title);
                mWeblable5.setTag(bh);
            }
        }
    }

    @Override
    public void onClick(View view) {
        try {
            WebSiteData webSiteData = (WebSiteData) view.getTag();
            if (mOnWebLableClickListener != null) {
                mOnWebLableClickListener.onWebLableClick(view, webSiteData);
            }
        }catch (Exception e){
        }
    }
}
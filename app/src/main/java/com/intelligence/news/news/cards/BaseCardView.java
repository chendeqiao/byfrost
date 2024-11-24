package com.intelligence.news.news.cards;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.baseUi.CircleImageView;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.download.util.StringUtils;
import com.intelligence.news.news.mode.NewsData;

public abstract class BaseCardView extends LinearLayout implements View.OnClickListener {
    protected TextView mTitle;
    public Context mContext;
    protected TextView mDescrip1;
    protected TextView mDescrip2;
    protected View mLine1;
    protected View mLine2;
    protected TextView mTime;
    protected CircleImageView mImage;

    protected int mStyle = 0;

    public BaseCardView(Context context) {
        this(context, null);
        this.mContext = context;
        this.initView();
    }
    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.initView();
    }

    private void initView() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        this.setOrientation(LinearLayout.VERTICAL);
        this.initHeaderView(this);
        this.initBodyView(this);
        this.initFooterView(this);
        initChildView(this);
        setOnClickListener(this);
    }

    protected abstract void initHeaderView(ViewGroup var1);

    protected abstract void initBodyView(ViewGroup var1);

    protected void initFooterView(ViewGroup footerView){
        View vBottomView = LayoutInflater.from(footerView.getContext()).inflate(R.layout.browser_card_footer, null);
        footerView.addView(vBottomView);
    }

    protected void initChildView(ViewGroup viewGroup) {
        mDescrip1 = viewGroup.findViewById(R.id.browser_card_footer_des1);
        mDescrip2 = viewGroup.findViewById(R.id.browser_card_footer_des2);
        mLine1 = viewGroup.findViewById(R.id.browser_card_footer_line1);
        mLine2 = viewGroup.findViewById(R.id.browser_card_footer_line2);
        mTime = viewGroup.findViewById(R.id.browser_card_footer_time);
    }

    public void updateData(NewsData newsData) {
        resetCardUI();
        setTag(newsData.url);
        if (mTitle != null && newsData != null && !TextUtils.isEmpty(newsData.title)) {
            mTitle.setText(newsData.title);
        }
        if(mTime != null) {
            String time = StringUtils.stringToDate1(newsData.time);
            if (!TextUtils.isEmpty(time)) {
                mTime.setText(time);
            } else {
                mTime.setText("更多");
            }
            mTime.setVisibility(VISIBLE);
        }
        if(mDescrip1 != null) {
            mDescrip1.setText(newsData.source);
            mDescrip1.setVisibility(VISIBLE);
        }

        if(mImage != null && !TextUtils.isEmpty(newsData.picUrl)) {
//            ImageLoaderEngine.getInstance().load(mImage, R.drawable.browser_card_image_bg, newsData.picUrl);
        }
    }

    private void resetCardUI(){
        if(mImage != null){
            mImage.setCornerRadius(8);
            mImage.setImageBitmap(null);
        }
    }

    public int getStyle() {
        return this.mStyle;
    }

    public void setStyle(int style) {
        this.mStyle = style;
    }

    @Override
    public void onClick(View view) {
        SchemeUtil.jumpToScheme(getContext(),getTag()+"");
    }
}

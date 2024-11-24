package com.intelligence.news.news.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.intelligence.browser.R;

public class NewsItemView extends LinearLayout implements View.OnClickListener{
    private BaseCardView mBaseCardView;
    public View mRefreshView;
    /**在首页列表中实际位置（包含广告的）*/
    public NewsItemView(Context context) {
        this(context,null);
    }
    public NewsItemView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public NewsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundResource(R.drawable.browser_list_item_selector);
        mRefreshView = LayoutInflater.from(this.getContext()).inflate(R.layout.browser_fresh_item, this,false);
        mRefreshView.setOnClickListener(this);
        addView(mRefreshView);
    }

    public BaseCardView getBaseCardView(){
        return mBaseCardView;
    }

    public void setBaseCardView(BaseCardView cardView){
        if(cardView == null){
            return;
        }
        mBaseCardView = cardView;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.addView(mBaseCardView,params);

        addExtentionView();
    }

    private void addExtentionView(){
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(this.getVisibility() == View.GONE){
            //解决ListView中item设置GONE不起作用问题
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View v) {
        //点击“上次看到这里，请刷新”,触发refrshListview 下拉刷新
    }
}

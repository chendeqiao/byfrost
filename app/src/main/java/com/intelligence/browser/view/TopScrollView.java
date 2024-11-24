package com.intelligence.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class TopScrollView extends ScrollView {

    private float startY;  // 记录触摸的起始Y坐标
    private OnScrollToTopListener onScrollToTopListener;

    public TopScrollView(Context context) {
        super(context);
    }

    public TopScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(getScrollY() != 0){
                    startY = 100000;
                }else {
                    startY = ev.getRawY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (getScrollY() == 0 && ev.getRawY() - startY > 200) {
                    if (onScrollToTopListener != null) {
                        onScrollToTopListener.onScrollToTop();
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // 设置监听器
    public void setOnScrollToTopListener(OnScrollToTopListener listener) {
        this.onScrollToTopListener = listener;
    }

    // 定义接口
    public interface OnScrollToTopListener {
        void onScrollToTop();
    }
}
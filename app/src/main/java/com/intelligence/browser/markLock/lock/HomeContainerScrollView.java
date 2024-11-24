package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.intelligence.browser.markLock.GenericCallback;
import com.intelligence.commonlib.tools.ScreenUtils;

import java.util.LinkedList;


public class HomeContainerScrollView extends ScrollView{


    public HomeContainerScrollView(Context context) {
        super(context);
    }

    public HomeContainerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeContainerScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    public final LinkedList<View>  relativeStaticViewList = new LinkedList<>();




    public GenericCallback<Integer> scrollTopChangeListener;
    private void callScrollTopChangeListener(){
        if(scrollTopChangeListener!=null)
            scrollTopChangeListener.call(getScrollY());

        if(!relativeStaticViewList.isEmpty()){
            for (View view : relativeStaticViewList)
                view.setTranslationY(getScrollY());
        }
    }



    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        callScrollTopChangeListener();
    }

    private boolean shouldBlock;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getX() > getRight() - mEdgeSize) {
            shouldBlock = true;
        }
        if (shouldBlock) {
            return detector.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }

    private final int minScrollToLeftDis = (int)ScreenUtils.dpToPx(getContext(),50);
    private final int mEdgeSize = (int)ScreenUtils.dpToPx(getContext(),20);
    private GestureDetector detector = new GestureDetector(BrowserApplication.getInstance(), new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1==null || e2==null)
                return true;
            float dis = e1.getX() - e2.getX();
            if(e1.getX() > getRight() - mEdgeSize && onHorizontalFlingToLeft != null && dis > minScrollToLeftDis && velocityX < -300)
                onHorizontalFlingToLeft.run();
            shouldBlock = false;
            return true;
        }
    });


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public Runnable onHorizontalFlingToLeft;


}

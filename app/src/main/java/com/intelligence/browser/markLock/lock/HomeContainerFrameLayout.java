package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.intelligence.browser.markLock.GenericCallback;

import java.util.ArrayList;


public class HomeContainerFrameLayout extends FrameLayout{

    public HomeContainerFrameLayout(Context context) {
        super(context);
    }

    public HomeContainerFrameLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeContainerFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private static final ArrayList<GenericCallback<Integer>> visibilityListeners = new ArrayList<>();

    public static void registerVisibilityListener(GenericCallback<Integer> run){
        if(visibilityListeners.contains(run))
            return ;
        visibilityListeners.add(run);
    }
    public static void unRegisterVisibilityListener(GenericCallback<Integer> run){
        visibilityListeners.remove(run);
    }





    public Runnable onVisibilityChangedByUser;
    private int nowVisibility = View.VISIBLE;

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        ViewParent parent = getParent();
        if(parent != null)
            ((View)parent).setVisibility(visibility);

        if(nowVisibility==visibility)
            return ;
        nowVisibility = visibility;
        if(onVisibilityChangedByUser!=null)
            onVisibilityChangedByUser.run();

        for (GenericCallback<Integer> item : visibilityListeners) {
            item.call(getVisibility());
        }
    }



}

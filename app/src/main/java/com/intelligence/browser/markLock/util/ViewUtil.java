package com.intelligence.browser.markLock.util;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;

public class ViewUtil {


    public static void toggleVisibleAndGone(View... views){
        for (View view : views) {
            view.setVisibility(view.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
        }
    }
    public static void toggleVisibleAndInvisible(View... views){
        for (View view : views) {
            view.setVisibility(view.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE);
        }
    }


    public static boolean isViewAttachedActivityFinished(View view){
        Activity act = getActivityFromView(view);
        if(act==null)
            return false;
        return act.isFinishing();
    }


    /**
     * 获取View的activity
     * 如果是由ApplicationContext或者service创建的View, 返回null
     */
    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity)
                return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

}

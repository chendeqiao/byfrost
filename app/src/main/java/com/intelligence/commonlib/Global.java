package com.intelligence.commonlib;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Global {
    private static Context mContext;

    private static List<Activity> lists = new ArrayList<>();

    public static void clearActivity() {
        if (lists != null) {
            for (Activity activity : lists) {
                activity.finish();
            }
            lists.clear();
        }
    }

    public static void addActivity(Activity activity) {
        lists.add(activity);
    }

    public static void init(Context context){
        mContext = context;
//        TTAdManagerHolder.init(context);
    }

    public static Context getInstance() {
        if(mContext == null)
            throw new NullPointerException("Global must be inited");
        return mContext;
    }
}

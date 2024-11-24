package com.intelligence.browser.markLock.base;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.SystemTintBarUtils;


public class LockBaseActivity extends FragmentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemTintBarUtils.setSystemBarColor(this, R.color.status_bar_homepage);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(getActionBar() != null)
            getActionBar().hide();
        setInitBackEvent();
    }


    private volatile Handler inActivityLifeHandler;

    public synchronized Handler getInActivityLifeHandler(){
        if(inActivityLifeHandler==null)
            inActivityLifeHandler = new Handler(Looper.getMainLooper());
        return inActivityLifeHandler;
    }


    @Override
    protected void onDestroy() {
        if(inActivityLifeHandler!=null)
            inActivityLifeHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }




    protected void setInitBackEvent(){
        int[] res = new int[]{R.id.iv_left_up};
        for (int item : res) {
            View view = findViewById(item);
            if(view != null)
                view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        boolean i_do = onBackClick();
                        if(!i_do)
                            finish();
                    }});
        }
    }


    protected boolean onBackClick(){
        return false;
    }


}

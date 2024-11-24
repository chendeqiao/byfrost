package com.intelligence.browser.ui;


import android.view.View;

import com.intelligence.browser.R;
import com.intelligence.browser.view.BrowserActionBarView;

public class ActionBarActivity extends BaseActivity {

    protected BrowserActionBarView actionBarView;

    public void back(View view) {
        onBackPressed();
    }

    public BrowserActionBarView getBrowserActionBar() {
        return actionBarView;
    }

    protected void setBrowserActionBar(View.OnClickListener onclickListener) {
        actionBarView = findViewById(R.id.setting_action_bar);
        actionBarView.setOnclickListener(onclickListener);
        actionBarView.setLeftIconDrawable(R.drawable.browser_back);
    }

    public void setPageTitle(String title){
        if(actionBarView != null){
            actionBarView.setTitle(title);
        }
    };

    public void setRightIconVisible(){
        if(actionBarView != null){
            actionBarView.setRightIconVisible();
        }
    }

}

package com.intelligence.browser.ui.home.navigation;

import android.os.Bundle;
import android.view.View;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.browser.ui.ActionBarActivity;

public class EditNavigationActivity extends ActionBarActivity implements View.OnClickListener {
    private NavigationEditView navigationEditView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_edit_navigation_page);
        setBrowserActionBar(this);
        setPageTitle(getResources().getString(R.string.add));
        initView();
        SharedPreferencesUtils.put(SharedPreferencesUtils.IS_OPEN_EDIT, DeviceInfoUtils.getAppVersionCode(this));
    }
    private void initView(){
        findViewById(R.id.navigation_edit_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(navigationEditView != null){
                    navigationEditView.showEditCardView(true);
                }
            }
        });
        navigationEditView = findViewById(R.id.navigation_edit_view);
    }

    @Override
    public void onBackPressed() {
        if(navigationEditView != null){
            if(navigationEditView.isAutoEdit()){
                navigationEditView.showEditCardView(false);
                return;
            }
            if(navigationEditView.onBackKey()){
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (R.id.actionbar_left == v.getId()) {
            if (navigationEditView != null) {
                if (navigationEditView.isAutoEdit()) {
                    navigationEditView.showEditCardView(false);
                    return;
                }
                if(navigationEditView.onBackKey()){
                    return;
                }
            }
            finish();
        }
    }
}
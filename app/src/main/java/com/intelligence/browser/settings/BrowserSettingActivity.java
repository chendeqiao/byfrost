/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligence.browser.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.DefaultBrowserSetUtils;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.ui.ActionBarActivity;

public class BrowserSettingActivity extends ActionBarActivity implements View.OnClickListener {
    private OnRightIconClickListener mOnRightIconClickListener;

    @Override
    public void setCurrentLanguage() {
        super.setCurrentLanguage();
        setTitle(R.string.menu_preferences);
    }

    public static void startPreferencesForResult(Activity callerActivity, int requestCode) {
        final Intent intent = new Intent(callerActivity, BrowserSettingActivity.class);
        startBrowserActivity(callerActivity, intent, requestCode);
    }

    public static void startPreferencesToSetDefaultBrowserForResult(Activity callerActivity, int requestCode) {
        Intent intent = new Intent(callerActivity, BrowserSettingActivity.class);
        intent.putExtra(DefaultBrowserSetUtils.KEY_DEFAULT_BROWSER_SETTING, true);
        startBrowserActivity(callerActivity, intent, requestCode);
    }

    public static void startPreferenceFragmentForResult(Activity callerActivity, String fragmentName, int requestCode) {
        final Intent intent = new Intent(callerActivity, BrowserSettingActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, fragmentName);
        startBrowserActivity(callerActivity, intent, requestCode);
    }

    public static void startPreferenceFragmentExtraForResult(Activity callerActivity,
                                                             String fragmentName,
                                                             Bundle bundle,
                                                             int requestCode) {
        final Intent intent = new Intent(callerActivity, BrowserSettingActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, fragmentName);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle);
        startBrowserActivity(callerActivity, intent, requestCode);
    }

    private static void startBrowserActivity(Activity callerActivity, Intent intent, int requestCode) {
        callerActivity.startActivityForResult(intent, requestCode);
        callerActivity.overridePendingTransition(R.anim.browser_zoom_in, R.anim.browser_zoom_out);
    }


    public static void startPreferenceFragmentExtra(Context callerActivity,
                                                    String fragmentName) {
        final Intent intent = new Intent(callerActivity, BrowserSettingActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, fragmentName);
        startBrowserActivity(callerActivity, intent);
    }

    private static void startBrowserActivity(Context callerActivity, Intent intent) {
        callerActivity.startActivity(intent);
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.browser_preferences_page);
        setBrowserActionBar(this);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                startFragment(R.id.content_layout, new SettingsPreferenecesFragment());
                return;
            }

            String fragment = (String) extras.getCharSequence(PreferenceActivity.EXTRA_SHOW_FRAGMENT);
            if (!TextUtils.isEmpty(fragment)) {
                if (fragment.equals(SettingsPreferenecesFragment.class.getName())) {
                    startFragment(R.id.content_layout, new SettingsPreferenecesFragment());
                } else if (fragment.equals(BrowserClearDataPreferencesFragment.class.getName())) {
                    startFragment(R.id.content_layout, new BrowserClearDataPreferencesFragment());
                } else if(fragment.equals(BrowserFeedbackFragment.class.getName())){
                    startFragment(R.id.content_layout, new BrowserFeedbackFragment());
                }  else if(fragment.equals(BrowserAboutFragment.class.getName())){
                    startFragment(R.id.content_layout, new BrowserAboutFragment());
                }else {
                    startFragment(R.id.content_layout, new SettingsPreferenecesFragment());
                }
                return;
            }
        }

        startFragment(R.id.content_layout, new SettingsPreferenecesFragment());
    }

    public void startFeedback(){
        startFragment(R.id.content_layout, new BrowserFeedbackFragment());
    }

    private void startFragment(int rid, Fragment fragment) {
        getFragmentManager().beginTransaction().replace(rid, fragment).commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.content_layout);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void disableRightIcons() {
//        mRightActionbarIcon.setVisibility(View.GONE);
//        mSecondRightActionbarIcon.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left:
                back();
                break;
            case R.id.actionbar_right_icon:
                if (mOnRightIconClickListener != null) {
                    mOnRightIconClickListener.onRightIconClick(v);
                }
                break;
            case R.id.actionbar_second_right_icon:
                if (mOnRightIconClickListener != null) {
                    mOnRightIconClickListener.onSecondRightIconClick(v);
                }
                break;
        }
    }

    public void back() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            InputMethodUtils.hideKeyboard(this);
            getFragmentManager().popBackStack();
            disableRightIcons();
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.browser_zoom_in, R.anim.browser_zoom_out);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BrowserSettings.getInstance().getShowStatusBar()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void setRightIcon(@DrawableRes int iconRes) {
//        mRightActionbarIcon.setImageResource(iconRes);
//        mRightActionbarIcon.setVisibility(View.VISIBLE);
    }

    public void setSecondRightIcon(@DrawableRes int iconRes) {
//        mSecondRightActionbarIcon.setImageResource(iconRes);
//        mSecondRightActionbarIcon.setVisibility(View.VISIBLE);
    }

    public void setOnRightIconClickListener(OnRightIconClickListener onRightIconClickListener) {
        this.mOnRightIconClickListener = onRightIconClickListener;
    }

    public void setRightIconEnable(boolean enable) {
//        mRightActionbarIcon.setEnabled(enable);
    }

    public void setSecondRightIconEnable(boolean enable) {
//        mSecondRightActionbarIcon.setEnabled(enable);
    }

    public ImageView getRightActionbarIcon() {
        return null;
    }

    public interface OnRightIconClickListener {
        void onRightIconClick(View v);

        void onSecondRightIconClick(View v);
    }

}

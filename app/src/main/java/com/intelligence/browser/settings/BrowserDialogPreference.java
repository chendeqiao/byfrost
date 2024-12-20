package com.intelligence.browser.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.intelligence.browser.R;
import com.intelligence.browser.ui.search.SearchEngines;
import com.intelligence.browser.ui.widget.BrowserDialog;

public class BrowserDialogPreference extends DialogPreference {
    private BrowserDialog mDialog;
    private int mVisibility;

    public BrowserDialogPreference(Context context) {
        this(context, null);
    }

    public BrowserDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        ImageView imageView = view.findViewById(R.id.divider);
        if (imageView != null) {
            imageView.setVisibility(mVisibility);
        }
        return view;
    }

    public void setDeviderVisibility(int visibility) {
        this.mVisibility = visibility;
    }

    @Override
    protected void showDialog(Bundle state) {
        mDialog = new BrowserDialog(getContext(), getDialogMessage().toString()) {
            @Override
            public void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                if (getOnPreferenceChangeListener() != null) {
                    getOnPreferenceChangeListener().onPreferenceChange(BrowserDialogPreference.this, null);
                }
                clear();
            }
        };
        mDialog.setBrowserTitle(getDialogTitle().toString())
                .setBrowserNegativeButton(getNegativeButtonText().toString())
                .setBrowserPositiveButton(getPositiveButtonText().toString())
                .show();
    }

    private void clear() {
        BrowserSettings settings = BrowserSettings.getInstance();
        switch (getKey()) {
            case PreferenceKeys.PREF_PRIVACY_CLEAR_PASSWORDS:
                settings.clearPasswords();
                break;
            case PreferenceKeys.PREF_PRIVACY_CLEAR_GEOLOCATION_ACCESS:
                settings.clearLocationAccess();
                break;
            case PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES:
                settings.resetDefaultPreferences();
                setEnabled(true);
                SearchEngines.resetEngines();
                break;
        }
    }

    public void onPause() {
        if (mDialog == null || !mDialog.isShowing()) {
            return;
        }
        mDialog.dismiss();
    }
}

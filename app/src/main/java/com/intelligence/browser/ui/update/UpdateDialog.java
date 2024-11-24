package com.intelligence.browser.ui.update;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.ui.widget.BrowserDialog;

public class UpdateDialog extends BrowserDialog {
    private Button mPositiveBotton;
    private Button mNegativeeBotton;
    private View mGooglePlayIcon;

    public UpdateDialog(Context context) {
        super(context, R.style.FiveStarDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.browser_update_dialog, null);
        setBrowserContentView(view);

        mPositiveBotton = view.findViewById(R.id.star_positive);
        mNegativeeBotton = view.findViewById(R.id.star_negative);
        mGooglePlayIcon = view.findViewById(R.id.googleplay_icon);
        mGooglePlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceInfoUtils.openAppMark(getContext());
            }
        });
        mNegativeeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mPositiveBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                DeviceInfoUtils.openAppMark(getContext());
            }
        });
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }

    @Override
    public void show() {
        setAttributes();
        super.show();
    }

    public void onConfigurationChanged() {
        setAttributes();
    }

    private void setAttributes() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER);
    }
}

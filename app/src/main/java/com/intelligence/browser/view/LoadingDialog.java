package com.intelligence.browser.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.intelligence.browser.R;

public class LoadingDialog extends ProgressDialog {
    private TextView mText;
    private String mContent = "";
    private boolean mCancel = true;

    public LoadingDialog(Context context) {
        super(context, R.style.dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_loading_dialog);
        setCanceledOnTouchOutside(mCancel);
        mText = findViewById(R.id.tv_content);
        mText.setText(mContent);
    }

    public void setContent(String content) {
        mContent = content;
        if(mText != null) {
            mText.setText(content);
        }
    }

    public void setCancel(boolean cancel) {
        this.mCancel = cancel;
    }

    public void setContext(int resId) {
        setContent(getContext().getString(resId));
    }
}

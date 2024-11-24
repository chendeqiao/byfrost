package com.intelligence.browser.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.InputMethodUtils;


public class BrowserFindPopDialog extends Dialog implements View.OnClickListener, WebView.FindListener {
    private WebView mWebView;
    private View mView;
    private EditText mInputView;
    private TextView mFindNumber;
    private String mFindString;

    public BrowserFindPopDialog(Context context, int theme, WebView webView) {
        super(context, theme);
        this.mWebView = webView;
        this.mView = View.inflate(context, R.layout.browser_webfind_popview, null);
        initView();
    }

    public void setWebView(WebView webView) {
        this.mWebView = webView;
        this.mWebView.setFindListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.mView);
    }

    public void initView() {
        mInputView = (EditText) mView.findViewById(R.id.find_text);
        mFindNumber = (TextView) mView.findViewById(R.id.find_number);

        this.mWebView.setFindListener(this);
        mInputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mFindNumber.setText("");
                mFindString = s.toString();
                if (TextUtils.isEmpty(mFindString)) {
                    mWebView.clearMatches();
                    mWebView.findAll("");
                } else {
                    mWebView.findAllAsync(mFindString);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mView.findViewById(R.id.close_icon).setOnClickListener(this);
        mView.findViewById(R.id.forward_btn).setOnClickListener(this);
        mView.findViewById(R.id.back_btn).setOnClickListener(this);
    }

    public EditText getmInputView(){
       return mInputView;
    }

    public void clear() {
        mFindString = null;
        mInputView.setText("");
        mWebView.clearMatches();
        mWebView.findAll("");
        mFindNumber.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_icon:
                clear();
                InputMethodUtils.hideKeyboard(this);
                BrowserFindPopDialog.this.dismiss();
                break;
            case R.id.forward_btn:
                InputMethodUtils.hideKeyboard(this);
                mWebView.findNext(true);
                break;
            case R.id.back_btn:
                InputMethodUtils.hideKeyboard(this);
                mWebView.findNext(false);
                break;
        }
    }

    @Override
    public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
        if (numberOfMatches != 0) {
            mFindNumber.setText((activeMatchOrdinal + 1) + "/" + numberOfMatches);
        } else {
            if (TextUtils.isEmpty(mFindString)) {
                mFindNumber.setText("");
            } else {
                mFindNumber.setText("0/0");
            }
        }
    }

    @Override
    public void show() {
        super.show();
        if (mInputView != null) {
            mInputView.setFocusable(true);
            mInputView.requestFocus();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            clear();
            InputMethodUtils.hideKeyboard(this);
            BrowserFindPopDialog.this.dismiss();
        }catch (Exception e){
        }
    }
}

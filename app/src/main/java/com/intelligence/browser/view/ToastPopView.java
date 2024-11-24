package com.intelligence.browser.view;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.ScreenUtils;

public class ToastPopView {
    private View mView;
    private TextView mText;
    private PopupWindow mPopWindow;
    private TextView mTextButton;

    public ToastPopView(Context context) {
        this.mView = View.inflate(context, R.layout.browser_toast_pop_view, null);
        mText = mView.findViewById(R.id.toast_text);
        mTextButton = mView.findViewById(R.id.toast_text_button);
        mPopWindow = new PopupWindow(mView, ScreenUtils.dpToPxInt(context,150),  ScreenUtils.dpToPxInt(context,48), true);
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setAnimationStyle(R.style.popwindow_push_bottom);
    }

    public void show(View parentView, int height) {
        if (parentView == null) return;
        int nav = DisplayUtil.getNavBarHeight(BrowserApplication.getInstance().getApplicationContext());
        mPopWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, nav + height);
    }

    public void dismiss() {
        mPopWindow.dismiss();
    }

    public ToastPopView setText(int text) {
        mText.setText(text);
        return this;
    }

    public ToastPopView setButtonText(int text) {
        mTextButton.setText(text);
        return this;
    }

    public ToastPopView setTextOnClickListener(View.OnClickListener listener) {
        mView.setOnClickListener(listener);
        return this;
    }

    public ToastPopView setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mPopWindow.setOnDismissListener(listener);
        return this;
    }

}

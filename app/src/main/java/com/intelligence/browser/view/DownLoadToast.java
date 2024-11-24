package com.intelligence.browser.view;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.ScreenUtils;

public class DownLoadToast {
    private View mView;
    private TextView mText;
    private PopupWindow mPopWindow;
    private TextView mTextButton;
    private TextView mDownloadTips;
    private ImageView mDownloadIcon;
    private View mBrowserDownloadMessage;
    private View mBrowserDownloadColse;

    public DownLoadToast(Context context) {
        this.mView = View.inflate(context, R.layout.browser_download_toast, null);
        mText = mView.findViewById(R.id.toast_text);
        mTextButton = mView.findViewById(R.id.browser_download_message);
        mDownloadIcon = mView.findViewById(R.id.browser_download_tips_icon);
        mDownloadTips = mView.findViewById(R.id.browser_download_tips);
        mBrowserDownloadMessage = mView.findViewById(R.id.browser_download_message);
        mBrowserDownloadColse = mView.findViewById(R.id.browser_last_url_tips_close);
        mPopWindow = new PopupWindow(mView, ScreenUtils.dpToPxInt(context, ViewGroup.LayoutParams.MATCH_PARENT),  (int)context.getResources().getDimension(R.dimen.browser_download_tips_height), true);
        mPopWindow.setFocusable(true);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopWindow.setAnimationStyle(R.style.popwindow_push_bottom);
        mBrowserDownloadColse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show(View parentView, int height) {
        if (parentView == null) return;
        int nav = DisplayUtil.getNavBarHeight(BrowserApplication.getInstance().getApplicationContext());
        mPopWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, nav + height);
    }

    public void dismiss() {
        mPopWindow.dismiss();
    }

    public DownLoadToast setText(String text) {
        mText.setText(text);
        return this;
    }

    public DownLoadToast setDownState(boolean isDownloading) {
        if(isDownloading){
            mDownloadIcon.setImageResource(R.drawable.browser_download_tips_icon);
            mDownloadTips.setText(R.string.download_pending);
        }else {
            mDownloadIcon.setImageResource(R.drawable.browser_download_finish_icon);
            mDownloadTips.setText(R.string.notification_download_finish);
        }

        return this;
    }

    public DownLoadToast setButtonText(int text) {
        mTextButton.setText(text);
        return this;
    }

    public DownLoadToast setTextOnClickListener(View.OnClickListener listener) {
        mBrowserDownloadMessage.setOnClickListener(listener);
        return this;
    }

    public DownLoadToast setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mPopWindow.setOnDismissListener(listener);
        return this;
    }

}

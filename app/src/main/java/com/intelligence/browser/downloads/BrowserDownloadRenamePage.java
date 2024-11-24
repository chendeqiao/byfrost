package com.intelligence.browser.downloads;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.util.FileUtils;
import com.intelligence.browser.ui.ActionBarActivity;

import java.io.File;
import java.util.List;

public class BrowserDownloadRenamePage extends ActionBarActivity implements View.OnClickListener {

    private Bundle mMap;
    private long mReferance;
    private DownloadInfo mDownloadItem;
    private EditText mEdit;
    private String mFilePath;
    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_download_rename_page);
        setBrowserActionBar(this);
        setPageTitle(getResources().getString(R.string.rename));
        setRightIconVisible();
        mMap = getIntent().getExtras();
        if (mMap != null) {
            mReferance = mMap.getLong(Constants.DOWNLOAD_REFERANCE, 0);
            mDownloadItem = getDownloadItem(mReferance);
        }
        initActionBar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BrowserSettings.getInstance().getShowStatusBar()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams
                    .FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void initView() {
        mEdit = findViewById(R.id.filename);
        if (mDownloadItem != null) {
            mFilePath = mDownloadItem.getLocalFilePath();
            mFileName = mDownloadItem.getTitle();
            mEdit.setText(mFileName);
            if (mFileName.lastIndexOf(".") > 0) {
                mEdit.setSelection(mFileName.lastIndexOf("."));
            }
        }

        InputMethodUtils.showKeyboard(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void initActionBar() {
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setCustomView(R.layout.layout_custom_actionbar);
//        View actionbarView = actionBar.getCustomView();
//        actionbarView.findViewById(R.id.actionbar_left_icon).setOnClickListener(this);
//        TextView actionBarTitle = actionbarView.findViewById(R.id.actionbar_title);
//        actionBarTitle.setText(R.string.rename);
//        ImageView actionBarRightIcon = actionbarView.findViewById(R.id.actionbar_right_icon);
//        actionBarRightIcon.setVisibility(View.VISIBLE);
//        actionBarRightIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (R.id.actionbar_right_icon == v.getId()) {
            updateData();
        } else if (R.id.actionbar_left == v.getId()) {
            finish();
        }
    }

    public void updateData(){
        updateDownload();
    }

    private void updateDownload() {
        if (mDownloadItem == null) return;

        if (!TextUtils.isEmpty(mFilePath)) {
            String newName = mEdit.getText().toString();
            if (TextUtils.isEmpty(newName)) {
                ToastUtil.showShortToast(this, R.string.filename_empty);
                return;
            }
            String newPath = mFilePath.replace(mFileName, newName);
            if (FileUtils.fileIsExists(newPath)) {
                ToastUtil.showShortToast(this, R.string.file_exist);
                return;
            }
            if (!mFilePath.equals(newPath)) {
                File file = new File(mFilePath);
                boolean ret = file.renameTo(new File(newPath));
                if (ret) {
                    mDownloadItem.setTitle(newName);
                    mDownloadItem.setLocalFilePath(newPath);
                    Wink.get().updateDownloadInfo(mDownloadItem);
                    ToastUtil.showShortToast(this, R.string.rename_success);
                    finish();
                } else {
                    ToastUtil.showShortToast(this, R.string.rename_failed);
                }
            } else {
                finish();
            }
        }
    }

    private DownloadInfo getDownloadItem(long id) {
        List<DownloadInfo> list = Wink.get().getDownloadedResources();

        if (list != null && list.size() > 0) {
            for (DownloadInfo info :
                    list) {
                if (info.getId() == id) {
                    return info;
                }
            }
        }
        return null;
    }
}

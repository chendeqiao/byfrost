/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.intelligence.browser.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.downloads.BrowserDownloadPage;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.downloads.BrowserSnapshotPage;
import com.intelligence.browser.R;
import com.intelligence.browser.base.CombinedBookmarksCallbacks;
import com.intelligence.browser.ui.media.PhotoViewPagerActivity;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.utils.StorageUtils;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.browser.ui.widget.BrowserMultiselectDialog;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.componentlib.viewpagetabbar.PagerSlidingTabStrip;
import com.intelligence.browser.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class BrowserDownloadActivity extends BaseActivity implements CombinedBookmarksCallbacks, View.OnClickListener {
    public static final String EXTRA_OPEN_SNAPSHOT = "snapshot_id";

    private PagerSlidingTabStrip mIndicator;
    private ViewPager mViewPager;
    private List<Fragment> mList;
    private String[] mTitles;
    private ImageView mComboBack;
    private ImageView mHistoryDelete;
    private BasePagerAdapter adapter;
    private ImageView mSelectAll;
    private TextView mStorage;

    private BrowserDownloadPage mBrowserDownloadPage;
    private BrowserSnapshotPage mBrowserSnapshotPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_combo_down_layout);
        mList = new ArrayList<>();
        init();
    }

    public void setDeleteIconVisible(boolean isVisible){
        if(mHistoryDelete != null && mViewPagerPosition != 0){
            mHistoryDelete.setVisibility(isVisible?View.VISIBLE:View.GONE);
        }
    }

    private void init() {
        mBrowserSnapshotPage = new BrowserSnapshotPage();
        mBrowserDownloadPage = new BrowserDownloadPage();
        mBrowserDownloadPage.setDownloadBack(this);
        mList.add(mBrowserDownloadPage);
        mList.add(mBrowserSnapshotPage);
        mTitles = new String[]{getResources().getString(R.string.download), getResources().getString(R.string
                .tab_snapshots)};
        mIndicator = findViewById(R.id.tab_page_indicator_combo);
        mComboBack =  findViewById(R.id.combo_back);

        mComboBack.setOnClickListener(this);
        mViewPager = findViewById(R.id.viewpager_combo);
        mStorage = findViewById(R.id.storage_space);
        mStorage.setText(StorageUtils.getAllSDSpace(this));
        mSelectAll = findViewById(R.id.combo_bookmark_selectall);
        mSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBrowserDownloadPage != null){
                    mBrowserDownloadPage.selectAll();
                }
            }
        });
        adapter = new BasePagerAdapter(getSupportFragmentManager(), mTitles);
        mViewPager.setAdapter(adapter);
        mIndicator.setOnPageChangeListener(mOnPageChangeListener);
        mIndicator.setViewPager(mViewPager);
        String page = "0";
        if(getIntent() != null) {
            try {
                page = getIntent().getStringExtra(Constants.DOWNLOAD_STATE);
                if (page == null) {
                    Uri uri = Uri.parse(getIntent().getData().toString());
                    page = uri.getQueryParameter(Constants.DOWNLOAD_STATE);
                }
            }catch (Exception e){

            }
        }
        mViewPager.setCurrentItem(StringUtil.getInt(page, 0));
        mHistoryDelete =  findViewById(R.id.combo_history_delete);
        mHistoryDelete.setVisibility(View.GONE);
        mHistoryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserDialog dialog = new BrowserDialog(BrowserDownloadActivity.this, R.style.DownloadDialog) {
                    @Override
                    public void onPositiveButtonClick() {
                        super.onPositiveButtonClick();
                        if (mBrowserSnapshotPage != null) {
                            mBrowserSnapshotPage.removeAllSelected();
                        }
                        if(mHistoryDelete != null) {
                            mHistoryDelete.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNegativeButtonClick() {
                        super.onNegativeButtonClick();
                    }
                };
                dialog.setBrowserTitle("");
                dialog.setBrowserMessage(R.string.title_clear_off_line);
                dialog.setBrowserPositiveButton(R.string.delete);
                dialog.setBrowserNegativeButton(R.string.cancel);
                dialog.show();
            }
        });
    }

    private BrowserMultiselectDialog mExitDialog;

    public ImageView getDeleteIcon(){
        return mHistoryDelete;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putInt(STATE_SELECTED_TAB,
//                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Override
    public void openUrl(String url) {
//        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        setResult(RESULT_OK, i);
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void openInNewTab(String... urls) {
        Intent i = new Intent();
        i.putExtra(EXTRA_OPEN_ALL, urls);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void onListViewScroll(){
        if(mHistoryDelete != null){
//            mHistoryDelete.startAnim(false);
        }
    }

    public boolean isEditMode;
    @Override
    public void onBookmarkEditMode(boolean isEdit) {
        isEditMode = isEdit;
        if(mSelectAll != null) {
            mSelectAll.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        }
        if(mComboBack != null){
            mComboBack.setImageDrawable(isEdit?getResources().getDrawable(R.drawable.browser_select_all_cancle):getResources().getDrawable(R.drawable.browser_back));
        }
        if (isEdit) {
            mStorage.setVisibility(View.GONE);
        } else {
            mStorage.setVisibility(View.VISIBLE);
        }
    }

    public static final String EXTRA_OPEN_ALL = "open_all";
    @Override
    public void openSnapshot(long id) {
        Intent i = new Intent();
        i.putExtra(EXTRA_OPEN_SNAPSHOT, id);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_combined, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isEditMode && mBrowserDownloadPage != null && mViewPagerPosition == 0){
            if (mBrowserDownloadPage != null) {
                boolean isExit = mBrowserDownloadPage.exitEditMode();
                if (!isExit) {
                    super.onBackPressed();
                }
            }
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        if (R.id.combo_back == view.getId()) {
            if(isEditMode && mBrowserDownloadPage != null && mViewPagerPosition == 0){
                mBrowserDownloadPage.exitEditMode();
                return;
            }
            this.finish();
        }
    }

    class BasePagerAdapter extends FragmentPagerAdapter {
        String[] mTitles;

        public BasePagerAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            this.mTitles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    private int mViewPagerPosition = 0;
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mViewPagerPosition = position;
            if (position == 0) {
                if(mHistoryDelete != null) {
                    mHistoryDelete.setVisibility(View.GONE);
                }
                if (mSelectAll != null) {
                    mSelectAll.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
                }
                if (mComboBack != null) {
                    mComboBack.setImageDrawable(isEditMode ? getResources().getDrawable(R.drawable.browser_select_all_cancle) : getResources().getDrawable(R.drawable.browser_back));
                }
            } else {
                if(mHistoryDelete != null && !mBrowserSnapshotPage.isEmpty()) {
                    mHistoryDelete.setVisibility(View.VISIBLE);
                }else {
                    if(mHistoryDelete != null) {
                        mHistoryDelete.setVisibility(View.GONE);
                    }
                }
                if(mSelectAll != null) {
                    mSelectAll.setVisibility(View.GONE);
                }
                if (mComboBack != null) {
                    mComboBack.setImageDrawable(getResources().getDrawable(R.drawable.browser_back));
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PhotoViewPagerActivity.FINISH_CODE && data != null && resultCode == RESULT_OK && mBrowserDownloadPage != null){
            String[] url = data.getStringArrayExtra(PhotoViewPagerActivity.EXTRA_ALL);
            mBrowserDownloadPage.deleteFile(url);
        }
    }

}

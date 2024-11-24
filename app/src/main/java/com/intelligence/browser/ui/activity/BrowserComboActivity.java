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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.historybookmark.BrowserBookmarksPage;
import com.intelligence.browser.historybookmark.BrowserHistoryPage;
import com.intelligence.browser.base.CombinedBookmarksCallbacks;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.view.InputWordDeleteView;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.browser.ui.widget.BrowserMultiselectDialog;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.componentlib.viewpagetabbar.PagerSlidingTabStrip;
import com.intelligence.browser.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class BrowserComboActivity extends BaseActivity implements CombinedBookmarksCallbacks, View.OnClickListener {
    private static final String STATE_SELECTED_TAB = "tab";
    public static final String EXTRA_COMBO_ARGS = "combo_args";
    public static final String EXTRA_INITIAL_VIEW = "initial_view";
    public static final String EXTRA_OPEN_SNAPSHOT = "snapshot_id";
    public static final String EXTRA_OPEN_ALL = "open_all";
    public static final String EXTRA_CURRENT_URL = "url";
    private static final int INDICATOR_TEXT_SIZE = 14;

    private PagerSlidingTabStrip mIndicator;
    private ViewPager mViewPager;
    private List<Fragment> mList;
    private String[] mTitles;
    private boolean mEdit = false;
    private ImageView mComboBack;
    private ImageView mHistoryDelete;
    private BasePagerAdapter adapter;
    private ImageView mSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_combo_page);
        mList = new ArrayList<>();
        init();
//        DisplayUtil.changeScreenBrightnessIfNightMode(this);
    }

    private BrowserBookmarksPage mBrowserBookmarksPage;
    private BrowserHistoryPage mBrowserHistoryPage;
    private void init() {
        mBrowserHistoryPage = new BrowserHistoryPage();
        mBrowserBookmarksPage = new BrowserBookmarksPage();
        mList.add(mBrowserBookmarksPage);
        mList.add(mBrowserHistoryPage);
        mTitles = new String[]{getResources().getString(R.string.bookmark_list), getResources().getString(R.string
                .history)};
        mIndicator = findViewById(R.id.tab_page_indicator_combo);
        mComboBack =  findViewById(R.id.combo_back);

        mComboBack.setOnClickListener(this);
        mViewPager = findViewById(R.id.viewpager_combo);
        mSelectAll = findViewById(R.id.combo_bookmark_selectall);
        mSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBrowserBookmarksPage != null){
                    mBrowserBookmarksPage.clickSeleteAll();
                }
            }
        });
        adapter = new BasePagerAdapter(getSupportFragmentManager(), mTitles);
        mViewPager.setAdapter(adapter);
        mIndicator.setOnPageChangeListener(mOnPageChangeListener);
        mIndicator.setViewPager(mViewPager);
        String page = "1";
        if(getIntent() != null) {
            try {
                page = getIntent().getStringExtra(Constants.PAGE_INDEX);
                if (page == null) {
                    Uri uri = Uri.parse(getIntent().getData().toString());
                    page = uri.getQueryParameter(Constants.PAGE_INDEX);
                }
            }catch (Exception e){

            }
        }
        int index = StringUtil.getInt(page, 1);
        if (index != 0) {
            mBrowserHistoryPage.setIndex(index);
            mViewPager.setCurrentItem(1);
        }else {
            mViewPager.setCurrentItem(0);
        }
        mHistoryDelete =  findViewById(R.id.combo_history_delete);
        mHistoryDelete.setVisibility(View.GONE);
        mHistoryDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrowserDialog dialog = new BrowserDialog(BrowserComboActivity.this, R.style.DownloadDialog) {
                    @Override
                    public void onPositiveButtonClick() {
                        super.onPositiveButtonClick();
                        if (adapter != null) {
                            Fragment fragment = adapter.getItem(mViewPagerPosition);
                            if (fragment instanceof InputWordDeleteView.onDeleteWordClick) {
                                ((InputWordDeleteView.onDeleteWordClick) fragment).DeleteAllData();
                            }
                        }
                    }

                    @Override
                    public void onNegativeButtonClick() {
                        super.onNegativeButtonClick();
                    }
                };
                dialog.setBrowserTitle("");
                dialog.setBrowserMessage(R.string.title_clear_all_history);
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

    public void setDeleteIconVisible(boolean isVisible){
        if(mHistoryDelete != null){
            mHistoryDelete.setVisibility(isVisible?View.VISIBLE:View.GONE);
        }
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
        SchemeUtil.jumpToScheme(this,url);
        finish();
    }

    @Override
    public void openInNewTab(String... urls) {
//        Intent i = new Intent();
//        i.putExtra(EXTRA_OPEN_ALL, urls);
//        setResult(RESULT_OK, i);
        SchemeUtil.jumpToScheme(this,urls[0]);
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
    }

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
        if(isEditMode && mBrowserBookmarksPage != null && mViewPagerPosition == 0){
            mBrowserBookmarksPage.restoreNormorState();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        if (R.id.combo_back == view.getId()) {
            if(isEditMode && mBrowserBookmarksPage != null && mViewPagerPosition == 0){
                mBrowserBookmarksPage.restoreNormorState();
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
        public androidx.fragment.app.Fragment getItem(int position) {
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

}

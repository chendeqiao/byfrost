/*
 Copyright 2011, 2012 Chris Banes.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.intelligence.browser.ui.media;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.util.CollectionUtils;
import com.intelligence.browser.ui.BaseActivity;
import com.intelligence.browser.ui.BaseBlackActivity;
import com.intelligence.browser.ui.activity.BrowserComboActivity;
import com.intelligence.browser.ui.activity.BrowserDownloadActivity;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.webview.Controller;
import com.intelligence.browser.downloads.DownloadHandler;
import com.intelligence.browser.R;
import com.intelligence.browser.manager.WallpaperHandler;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.SharePageDialog;
import com.intelligence.commonlib.download.util.StringUtils;
import com.intelligence.commonlib.tools.AppBarThemeHelper;
import com.intelligence.componentlib.photoview.PhotoItemView;
import com.intelligence.browser.ui.ActionBarActivity;

import java.util.ArrayList;

public class PhotoViewPagerActivity extends BaseBlackActivity implements View.OnClickListener {
    public static final String IMAGE_URL_PRAMAS = "Extras";
    public static final String IMAGE_FROM_NET = "from";
    public static final String IMAGE_POSITION = "position";
    public static final String EXTRA_ALL = "extra_all";
    public static final int FINISH_CODE = 101;
    private View mShareLayout;
    private View mDeleteLayout;
    private View mDownLayout;
    private ArrayList<PhotoInfo> mImageUrls;
    private View mWallView;
    private View mPositionLayout;
    private View mBottomLayout;

    @Override
    public void onClick(View v) {
        if (R.id.actionbar_left == v.getId()) {
            finish();
        }
    }

    private ImageView mDeleteIcon;
    private ImageView mDownloadIcon;
    private ImageView mWallIcon;
    private ImageView mShareIcon;
    private View actionBar;
    private void initView(){
        mDeleteIcon = findViewById(R.id.image_delete);
        mDownloadIcon = findViewById(R.id.image_down);
        mWallIcon = findViewById(R.id.image_wall);
        mShareIcon = findViewById(R.id.image_share);

        ImageUtils.tinyIconColor(mDeleteIcon,R.color.white);
        ImageUtils.tinyIconColor(mDownloadIcon,R.color.white);
//        ImageUtils.tinyIconColor(mWallIcon,R.color.white);
        ImageUtils.tinyIconColor(mShareIcon,R.color.white);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_photoview_page);
        mWallView = findViewById(R.id.wall_layout);
        mBottomLayout = findViewById(R.id.bottom_layout);
        actionBar = findViewById(R.id.actionbar_layout);
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean hasPermission = DownloadHandler.hasReadWritePermissions(PhotoViewPagerActivity.this);
                    if (hasPermission) {
                        new WallpaperHandler(PhotoViewPagerActivity.this, getCurrentImageUrl()).onMenuItemClick(!mIsFromNet);
                    } else {
                        DeviceInfoUtils.verifyStoragePermissions(PhotoViewPagerActivity.this, DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_IMAGE_WALL);
                    }
                } catch (Exception e) {

                }
            }
        });
        initView();
        mDeleteLayout = findViewById(R.id.delete_layout);
        mPositionLayout = findViewById(R.id.position_layout);
        mDeleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCurrentPage();
            }
        });
        mShareLayout = findViewById(R.id.share_layout);
        mShareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharePageDialog dialog = new SharePageDialog(PhotoViewPagerActivity.this);
                    dialog.showShareDialog(mShareLayout,
                            DeviceInfoUtils.getAppName(PhotoViewPagerActivity.this), getCurrentImageUrl(), null);
                } catch (Exception e) {

                }
            }
        });
        mDownLayout = findViewById(R.id.down_layout);
        mDownLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String path = getCurrentImageUrl();
                    if (StringUtils.isLocalImage(path)) {
                        syncPicToDownloaded(path);
                        return;
                    }
                    DownloadHandler.onDownloadStart(PhotoViewPagerActivity.this, path, null, null, null,path);
                } catch (Exception e) {

                }
            }
        });
        mViewPager = findViewById(R.id.view_pager);
        mPhotoPosition = findViewById(R.id.photo_position);
        mphotoTotalCount = findViewById(R.id.photo_total_count);
        initData();
    }

    private void syncPicToDownloaded(String path) {
//        Wink.get().wink()
    }

    private void deleteCurrentPage() {
        if (!mImageUrls.get(mCurrentPosition).mIsAd) {
            mDeleteUrl.add(mImageUrls.get(mCurrentPosition).mImageUrl);
            mImageUrls.remove(mCurrentPosition);
            mViewPager.setAdapter(new SamplePagerAdapter(mImageUrls));
            mViewPager.setCurrentItem(mCurrentPosition >= mImageUrls.size() ? mCurrentPosition - 1 : mCurrentPosition);
            mCurrentPosition = mViewPager.getCurrentItem();
            mphotoTotalCount.setText("/" + mImageUrls.size() + "");
            setPagePosition(mCurrentPosition);
            ToastUtil.showShortToast(PhotoViewPagerActivity.this, R.string.download_removed);
        }
    }

    ArrayList<String> mDeleteUrl = new ArrayList<>();

    private ViewPager mViewPager;

    public void setPagePosition(int position) {
        if (mImageUrls.size() == 0) {
            mPositionLayout.setVisibility(View.GONE);
        } else {
            mPositionLayout.setVisibility(View.VISIBLE);
        }
        mPhotoPosition.setText((position + 1) + "");
        mDownLayout.setVisibility(mIsFromNet && mImageUrls.get(mCurrentPosition).isCanDownload ? View.VISIBLE : View.GONE);
        mIsFullMode = true;
        fullScreenMode();
    }

    private TextView mPhotoPosition;
    private TextView mphotoTotalCount;

    private int mCurrentPosition = 0;

    private boolean mIsFromNet;

    private ArrayList<PhotoInfo> getImagesUrls(String[] ImageUrls) {
        return getArrayForStrings(ImageUrls);
    }

    public ArrayList<PhotoInfo> getArrayForStrings(String[] ImageUrls) {
        ArrayList<PhotoInfo> arrayList = new ArrayList();
        try {
            for (String s : ImageUrls) {
                if (!TextUtils.isEmpty(s)) {
                    PhotoInfo photoInfo = new PhotoInfo();
                    if (StringUtils.isBase64(s)) {
                        photoInfo.mImageUrl = StringUtils.saveBase64Image(PhotoViewPagerActivity.this, s);
                        photoInfo.isCanDownload = false;
                    } else {
                        photoInfo.mImageUrl = s;
                        if (StringUtils.isLocalImage(s)) {
                            photoInfo.isCanDownload = false;
                        } else {
                            photoInfo.isCanDownload = true;
                        }
                    }
                    photoInfo.mIsAd = false;
                    photoInfo.isFromNet = mIsFromNet;
                    arrayList.add(photoInfo);
                }
            }
        } catch (Exception e) {
        }
        return arrayList;
    }

    private SamplePagerAdapter mSamplePagerAdapter;

    private void initData() {
        int position = 0;
        try {
            String[] ImageUrls = getIntent().getStringArrayExtra(IMAGE_URL_PRAMAS);
            String fromNet = (getIntent().getStringExtra(IMAGE_FROM_NET));
            if (!TextUtils.isEmpty(fromNet)) {
                mIsFromNet = fromNet.equals("1");
            }
            position = (getIntent().getIntExtra(IMAGE_POSITION, 0));
            mImageUrls = getImagesUrls(ImageUrls);
            if (CollectionUtils.isEmpty(mImageUrls)) {
                Intent intent = getIntent();
                String action = intent.getAction();
                Uri data = intent.getData();
                if (Intent.ACTION_VIEW.equals(action) && data != null && !TextUtils.isEmpty(data.toString())) {
                    PhotoInfo photoInfo = new PhotoInfo();
                    photoInfo.isCanDownload = false;
                    photoInfo.mImageUrl = data.toString();
                    photoInfo.isFromNet = false;
                    photoInfo.mIsAd = false;
                    mImageUrls.add(photoInfo);
                    mIsFromNet = false;
                }
            }
        } catch (Exception e) {

        }
        if (CollectionUtils.isEmpty(mImageUrls)) {
            ToastUtil.show(R.string.download_failed);
            finish();
            return;
        }
        mSamplePagerAdapter = new SamplePagerAdapter(mImageUrls);
        mViewPager.setAdapter(mSamplePagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                setPagePosition(position);
            }
        });

        if (mImageUrls != null && mImageUrls.size() > mCurrentPosition) {
            mDownLayout.setVisibility(mIsFromNet && mImageUrls.get(mCurrentPosition).isCanDownload ? View.VISIBLE : View.GONE);
        } else {
            mDownLayout.setVisibility(mIsFromNet ? View.VISIBLE : View.GONE);

        }
        mDeleteLayout.setVisibility(mIsFromNet ? View.GONE : View.VISIBLE);
        mViewPager.setCurrentItem(position);
        setPagePosition(position);
        mphotoTotalCount.setText("/" + mImageUrls.size() + "");
    }

    private String getCurrentImageUrl() {
        return mImageUrls.get(mCurrentPosition).mImageUrl;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_IMAGE_DOWN
                || requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_FIRST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DownloadHandler.onDownloadStart(PhotoViewPagerActivity.this, getCurrentImageUrl(), null, null, null,getCurrentImageUrl());
            } else {
                showPermissionDeniedDialog(R.string.browser_permission_strone);
            }
        } else if (requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_IMAGE_WALL) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new WallpaperHandler(PhotoViewPagerActivity.this, getCurrentImageUrl()).onMenuItemClick();
            } else {
                showPermissionDeniedDialog(R.string.browser_permission_setwall);
            }
        }
    }

    private void showPermissionDeniedDialog(int message) {
        new AlertDialog.Builder(PhotoViewPagerActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用程序的权限设置页面
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        switch (requestCode) {
            case Controller.COMBO_VIEW:
                if(data.hasExtra(BrowserComboActivity.EXTRA_OPEN_SNAPSHOT)) {
                    long id = data.getLongExtra(
                            BrowserComboActivity.EXTRA_OPEN_SNAPSHOT, -1);
                    Intent i = new Intent();
                    i.putExtra(BrowserDownloadActivity.EXTRA_OPEN_SNAPSHOT, id);
                    setResult(RESULT_OK, i);
                    finish();
                }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void finish() {
        if (!CollectionUtils.isEmpty(mDeleteUrl)) {
            try {
                Intent i = new Intent();
                String[] array1 = new String[mDeleteUrl.size()];
                i.putExtra(EXTRA_ALL, mDeleteUrl.toArray(array1));
                setResult(RESULT_OK, i);
            } catch (Exception e) {
            }
        }
        super.finish();
    }

    private boolean mIsFullMode;

    private void fullScreenMode() {
        mIsFullMode = !mIsFullMode;
        mBottomLayout.setVisibility(mIsFullMode ? View.GONE : View.VISIBLE);
        actionBar.setVisibility(mIsFullMode ? View.GONE : View.VISIBLE);
    }

    class SamplePagerAdapter extends PagerAdapter {
        private ArrayList<PhotoInfo> mImageUrls;

        public SamplePagerAdapter(ArrayList<PhotoInfo> imageUrls) {
            this.mImageUrls = imageUrls;
        }

        public void updateDatas(ArrayList<PhotoInfo> imageUrls) {
            this.mImageUrls = imageUrls;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mImageUrls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoItemView photoView = new PhotoItemView(container.getContext());
            // Now just add PhotoView to ViewPager and return it
            photoView.setImageUrl(getCurrentImageUrl(position), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fullScreenMode();
                }
            });
            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return photoView;
        }

        private PhotoInfo getCurrentImageUrl(int position) {
            return mImageUrls.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}

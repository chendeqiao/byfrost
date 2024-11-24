package com.intelligence.browser.downloads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.FileProvider;

import com.intelligence.browser.R;
import com.intelligence.browser.downloads.support.WinkEvent;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.DownLoadToast;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.commonlib.notification.NotificationCenter;
import com.intelligence.commonlib.notification.Subscriber;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.download.Resource;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.commonlib.download.WinkError;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.request.ResourceStatus;
import com.intelligence.commonlib.download.request.SimpleURLResource;
import com.intelligence.commonlib.download.util.FileUtils;

import java.io.File;
import java.util.List;

public class BrowserDownloadManager {
    private DownloadScanner mScanner;
    private DownLoadToast mToast;
    private Activity mActivity;

    public static BrowserDownloadManager getInstance() {
        return InstanceHolder.instance;
    }

    public static class InstanceHolder {
        static final BrowserDownloadManager instance = new BrowserDownloadManager();
    }


    public void onDestroy() {
        if (mScanner != null) {
            mScanner.shutdown();
        }
        if (mToast != null && mActivity != null && !mActivity.isFinishing()) {
            mToast.dismiss();
            mToast = null;
        }
        mActivity = null;
    }

    public void apkInfo(String absPath, Context context, DownloadInfo entity) {
        if (context == null) return;

        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(absPath, PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = absPath;
            appInfo.publicSourceDir = absPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
            Drawable icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
            Drawable icon2 = appInfo.loadIcon(pm);

            if (icon1 != null) {
                entity.setApkIcon(icon1);
            } else if (icon2 != null) {
                entity.setApkIcon(icon2);
            }

            if (!TextUtils.isEmpty(appName)) {
                entity.setApkName(appName);
            }
        }
    }

    public boolean isInternalStorageAvailable(Context context) {
        return context.getFilesDir() != null;
    }

    public void startDownload(final Activity activity, final String url,
                              String userAgent, String contentDisposition, final String mimetype,
                              String referer, String filename) {
//        if (!StorageUtils.isSDExist(activity, BrowserSettings.getInstance().getDownloadPath())) {
//            try {
//                BrowserSettings.getInstance().setDownloadPath(FileUtils.getLocalDir());
//            } catch (Exception e) {
//            }
//        }
        if (!isInternalStorageAvailable(activity)) {
//            ToastUtil.showLongToast(activity, R.string.download_no_internal_storage);
            return;
        }

        if (mActivity != null && !activity.equals(mActivity)) {
            if (mToast != null) {
                mToast.dismiss();
                mToast = null;
            }
        }
        mActivity = activity;
        NotificationCenter.defaultCenter().subscriber(WinkEvent.class, mEventSubscriber);

        if (mScanner == null) {
            mScanner = new DownloadScanner(BrowserApplication.getInstance().getApplicationContext());
        }
        String memiType = FileUtils.getUrlType(url);
        int err = Wink.get().wink(url, filename, mimetype, referer);
        if (TextUtils.isEmpty(memiType) || !"image/jpeg".equals(memiType)) {
            downloadError(err, mActivity, url, mimetype, filename, referer);
        }
    }


    private void downloadError(final int err, final Activity activity, final String url,
                               final String mimetype, final String apkname, final String referer) {

        Handler handler = new Handler(activity.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (err == WinkError.SUCCESS) {
                    showPopToast(activity, url+"", false);
                } else if (err == WinkError.EXIST) {
                    ToastUtil.showLongToastByString(activity, apkname + " " + activity.getString(R.string
                            .downloading) + "?");
                } else if (err == WinkError.ALREADY_COMPLETED) {
                    BrowserDialog dialog = new BrowserDialog(activity, R.style.DownloadDialog) {
                        @Override
                        public void onPositiveButtonClick() {
                            super.onPositiveButtonClick();

                            try {
                                Wink.get().delete(new SimpleURLResource(url).getKey(), true);
                                int err = Wink.get().wink(url, apkname, mimetype, referer);
                                downloadError(err, activity, url, mimetype, apkname, referer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNegativeButtonClick() {
                            super.onNegativeButtonClick();
                        }
                    };
                    dialog.setBrowserMessage(apkname + " " + activity.getResources().getString(R.string.downloaded));
                    dialog.setBrowserPositiveButton(R.string.replace_file);
                    dialog.setBrowserNegativeButton(R.string.cancel);
                    dialog.show();
                } else if (err == WinkError.INSUFFICIENT_SPACE) {
                    ToastUtil.showLongToast(activity, R.string.storage_space_lack);
//                } else if (err == NetworkError.NO_AVALIABLE_NETWORK) {
//                    ToastUtil.showLongToast(activity, R.string.check_network);
                }
            }
        });

    }

    private Subscriber<WinkEvent> mEventSubscriber = new Subscriber<WinkEvent>() {
        @Override
        public void onEvent(WinkEvent event) {
            if (mScanner != null && event.event == WinkEvent.EVENT_STATUS_CHANGE && event.entity.getDownloadState()
                    == ResourceStatus.DOWNLOADED) {
                mScanner.requestScan(event.entity);
                showPopToast(mActivity, event.entity.getUrl(), true);
                Resource res = event.entity.getResource();
                if (res instanceof SimpleURLResource) {
                    String mime = ((SimpleURLResource) res).getMimeType();
                    if ("application/vnd.android.package-archive".equalsIgnoreCase(mime)) {
                        installApk(event.entity.getLocalFilePath());
                    }
                }
            }
        }
    };


    private void showPopToast(final Activity activity, final String text, final boolean downloaded) {
        if (mActivity == null) return;
        Handler handler = new Handler(activity.getMainLooper());
        if (mToast == null) {
            mToast = new DownLoadToast(activity);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (activity == null || activity.isFinishing() || mToast == null) return;
                    mToast.dismiss();
                }
            });
        }

        int height = ScreenUtils.dpToPxInt(activity,55);
//        if (mController != null) height = mController.toolBarHeight();

        final int barHeight = height;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) return;
                mToast.setText(text).setDownState(!downloaded).setTextOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mToast.dismiss();
                        mToast = null;
                        if (downloaded) {
                            ActivityUtils.startDownloadActivity(activity, 0);
                        } else {
                            ActivityUtils.startDownloadActivity(activity,0);
                        }
                    }
                });
                if (activity != null && !activity.isFinishing()) {
                    try {
                        mToast.show(activity.getWindow().getDecorView(), barHeight);
                    } catch (Exception e) {
                    }
                }
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity != null && !activity.isFinishing() && mToast != null) {
                    mToast.dismiss();
                    mToast = null;
                }
            }
        }, 4000);
    }

    public void downloadAction(String key, int state) {
        if (key == null) return;

        DownloadInfo info = getDownloadInfo(key);
        switch (state) {
            case ResourceStatus.WAIT:
            case ResourceStatus.DOWNLOADING:
                if (info != null) {
                    Wink.get().stop(key);
                }
                break;
            case ResourceStatus.DOWNLOAD_FAILED:
                if (info != null) {
                    Wink.get().wink(info.getResource());
                }
                break;
            case ResourceStatus.INIT:
            case ResourceStatus.PAUSE:
                if (info != null) {
                    Wink.get().wink(info.getResource());
                }
                break;
            case ResourceStatus.DELETED:
                break;
            case ResourceStatus.DOWNLOADED:
                break;
        }
    }

    public DownloadInfo getDownloadInfo(String key) {
        Wink wink = Wink.get();
        List<DownloadInfo> download = wink.getDownloadingResources();
        if (download == null) return null;

        for (DownloadInfo data : download) {
            if (key.equals(data.getKey())) {
                return data;
            }
        }
        return null;
    }

    private void installApk(String apkPath) {
        if (apkPath == null) return;

        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
//            ToastUtil.showLongToast(BrowserApplication.getInstance(), R.string.apk_file_not_exist);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(BrowserApplication.getInstance(),
                    BrowserApplication.getInstance().getPackageName() + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        BrowserApplication.getInstance().startActivity(intent);
    }

}

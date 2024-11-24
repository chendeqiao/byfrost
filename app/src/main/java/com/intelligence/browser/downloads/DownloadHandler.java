/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.intelligence.browser.downloads;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.commonlib.download.util.FileUtils;

public class DownloadHandler {
    private static final String LOGTAG = "DownloadHandler";
    public static void onDownloadStart(Activity activity, String url,
                                       String userAgent, String contentDisposition, String mimetype,
                                       String referer) {
        // 处理下载类型和 MIME 类型
        String fileExtension = FileUtils.getFileExtensionFromUrl(url).toLowerCase();
        if (TextUtils.isEmpty(mimetype) && fileExtension != null) {
            mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }

        // 特殊处理 APK 文件的 MIME 类型
        if ("apk".equals(fileExtension) && !TextUtils.isEmpty(mimetype) && !mimetype.equals("application/vnd.android.package-archive")) {
            mimetype = "application/vnd.android.package-archive";
        }

        String apkname = FileUtils.getApkPath(url, contentDisposition, mimetype);
        String specialEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        String filename = apkname.replaceAll(specialEx, "");
        downloadDialog(activity, url, userAgent, contentDisposition, mimetype, referer, filename);
    }

    public  static Activity mActivity;

    private static void downloadDialog(final Activity activity, final String url,
                                       final String userAgent, final String contentDisposition, final String mimetype,
                                       final String referer, final String filename) {
        if (BrowserSettings.getInstance().getDownloadConfirm()) {
            Handler handler = new Handler(activity.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BrowserDialog dialog = new BrowserDialog(activity, R.style.DownloadDialog) {
                        @Override
                        public void onPositiveButtonClick() {
                            super.onPositiveButtonClick();
                            try {
                                download(activity, url, userAgent, contentDisposition,
                                        mimetype, referer, filename);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNegativeButtonClick() {
                            super.onNegativeButtonClick();
                        }
                    };
                    dialog.setBrowserContentView(getCheckBoxView(activity, activity.getResources().getString(R.string
                            .download) + " " + filename, new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            BrowserSettings.getInstance().setDownloadConfirm(!isChecked);
                        }
                    }));
                    dialog.setBrowserPositiveButton(R.string.download);
                    dialog.setBrowserNegativeButton(R.string.cancel);
                    dialog.show();
                }
            });
        } else {
            try {
                download(activity, url, userAgent, contentDisposition,
                        mimetype, referer, filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean mSelect;
    public static View getCheckBoxView(Context context, String text,
                                       final CompoundButton.OnCheckedChangeListener l) {
        mSelect = false;
        View view = LayoutInflater.from(context).inflate(
                R.layout.browser_checkbox_dialog, null);
        ((TextView) view.findViewById(R.id.content)).setText(text);
        RelativeLayout checkLayout = view.findViewById(R.id.browser_download_checkbox);
        final ImageView checkIcon = view.findViewById(R.id.image);
        checkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != l) {
                    mSelect = !mSelect;
                    checkIcon.setSelected(mSelect);
                    l.onCheckedChanged(null, mSelect);
                }
            }

        });
        return view;
    }

    private static final String DOWNLOAD_DIR = "browser_downloads";

    private static void download(final Activity activity, final String url,
                                 final String userAgent, final String contentDisposition, final String mimetype,
                                 final String referer, final String filename) {
        new Thread("Browser download") {
            public void run() {
                try {
                    BrowserDownloadManager.getInstance().startDownload(activity, url, userAgent, contentDisposition,
                            mimetype, referer,filename);
                } catch (Exception e) {
                }
            }
        }.start();
    }

    public static boolean hasReadWritePermissions(Context context) {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }
        int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return writePermission == PackageManager.PERMISSION_GRANTED ;
    }
}

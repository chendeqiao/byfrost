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

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.core.content.FileProvider;

import java.io.File;

public class DownloadOpenReceiver extends BroadcastReceiver {
    private static Handler sAsyncHandler;

    static {
        HandlerThread thr = new HandlerThread("Open browser download async");
        thr.start();
        sAsyncHandler = new Handler(thr.getLooper());
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (!DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            openDownloadsPage(context);
            return;
        }
        long[] ids = intent.getLongArrayExtra(
                DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
        if (ids == null || ids.length == 0) {
            openDownloadsPage(context);
            return;
        }
        final long id = ids[0];
        final PendingResult result = goAsync();
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                onReceiveAsync(context, id);
                result.finish();
            }
        };
        sAsyncHandler.post(worker);
    }

    private void onReceiveAsync(Context context, long id) {
        DownloadManager manager = (DownloadManager) context.getSystemService(
                Context.DOWNLOAD_SERVICE);
        Uri uri = manager.getUriForDownloadedFile(id);
        if (uri == null) {
            // Open the downloads page
            openDownloadsPage(context);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
//            launchIntent.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(id));
//            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName()+".fileprovider", new File(uri.getPath()));
                intent.setDataAndType(contentUri, manager.getMimeTypeForDownloadedFile(id));
            } else {
//                intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(id));
            }

            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                openDownloadsPage(context);
            }
        }
    }

    /**
     * Open the Activity which shows a list of all downloads.
     *
     * @param context
     */
    private void openDownloadsPage(Context context) {
        Intent pageView = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        pageView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(pageView);
        } catch (ActivityNotFoundException e) {
        }
    }
}

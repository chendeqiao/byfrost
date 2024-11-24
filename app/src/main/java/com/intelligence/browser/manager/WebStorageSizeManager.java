/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.intelligence.browser.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.StatFs;
import android.preference.PreferenceActivity;
import android.webkit.WebStorage;

import com.intelligence.browser.R;
import com.intelligence.browser.settings.BrowserSettingActivity;
import com.intelligence.browser.settings.BrowserClearDataPreferencesFragment;
import com.intelligence.browser.reflections.NotificationExtension;

import java.io.File;

public class WebStorageSizeManager {
    private final static String LOGTAG = "browser";
    // The default quota value for an origin.
    public final static long ORIGIN_DEFAULT_QUOTA = 3 * 1024 * 1024;  // 3MB
    // The default value for quota increases.
    public final static long QUOTA_INCREASE_STEP = 1 * 1024 * 1024;  // 1MB
    // Extra padding space for appcache maximum size increases. This is needed
    // because WebKit sends us an estimate of the amount of space needed
    // but this estimate may, currently, be slightly less than what is actually
    // needed. We therefore add some 'padding'.
    // TODO(andreip): fix this in WebKit.
    public final static long APPCACHE_MAXSIZE_PADDING = 512 * 1024; // 512KB
    // The system status bar notification id.
    private final static int OUT_OF_SPACE_ID = 1;
    // The time of the last out of space notification
    private static long mLastOutOfSpaceNotificationTime = -1;
    // Delay between two notification in ms
    private final static long NOTIFICATION_INTERVAL = 5 * 60 * 1000;
    // Delay in ms used when resetting the notification time
    private final static long RESET_NOTIFICATION_INTERVAL = 3 * 1000;
    // The application context.
    private final Context mContext;
    // The global Web storage limit.
    private final long mGlobalLimit;
    // The maximum size of the application cache file.
    private long mAppCacheMaxSize;

    /**
     * Interface used by the WebStorageSizeManager to obtain information
     * about the underlying file system. This functionality is separated
     * into its own interface mainly for testing purposes.
     */
    public interface DiskInfo {
        /**
         * @return the size of the free space in the file system.
         */
        long getFreeSpaceSizeBytes();

        /**
         * @return the total size of the file system.
         */
        long getTotalSizeBytes();
    }

    private DiskInfo mDiskInfo;

    // For convenience, we provide a DiskInfo implementation that uses StatFs.
    public static class StatFsDiskInfo implements DiskInfo {
        private StatFs mFs;

        public StatFsDiskInfo(String path) {
            mFs = new StatFs(path);
        }

        public long getFreeSpaceSizeBytes() {
            return (long) (mFs.getAvailableBlocks()) * mFs.getBlockSize();
        }

        public long getTotalSizeBytes() {
            return (long) (mFs.getBlockCount()) * mFs.getBlockSize();
        }
    }

    /**
     * Interface used by the WebStorageSizeManager to obtain information
     * about the appcache file. This functionality is separated into its own
     * interface mainly for testing purposes.
     */
    public interface AppCacheInfo {
        /**
         * @return the current size of the appcache file.
         */
        long getAppCacheSizeBytes();
    }

    // For convenience, we provide an AppCacheInfo implementation.
    public static class WebKitAppCacheInfo implements AppCacheInfo {
        // The name of the application cache file. Keep in sync with
        // WebCore/loader/appcache/ApplicationCacheStorage.cpp
        private final static String APPCACHE_FILE = "ApplicationCache.db";
        private String mAppCachePath;

        public WebKitAppCacheInfo(String path) {
            mAppCachePath = path;
        }

        public long getAppCacheSizeBytes() {
            File file = new File(mAppCachePath
                    + File.separator
                    + APPCACHE_FILE);
            return file.length();
        }
    }

    /**
     * Public ctor
     *
     * @param ctx          is the application context
     * @param diskInfo     is the DiskInfo instance used to query the file system.
     * @param appCacheInfo is the AppCacheInfo used to query info about the
     *                     appcache file.
     */
    public WebStorageSizeManager(Context ctx, DiskInfo diskInfo,
                                 AppCacheInfo appCacheInfo) {
        mContext = ctx.getApplicationContext();
        mDiskInfo = diskInfo;
        mGlobalLimit = getGlobalLimit();
        // The initial max size of the app cache is either 25% of the global
        // limit or the current size of the app cache file, whichever is bigger.
        mAppCacheMaxSize = Math.max(mGlobalLimit / 4,
                appCacheInfo.getAppCacheSizeBytes());
    }

    /**
     * Returns the maximum size of the application cache.
     */
    public long getAppCacheMaxSize() {
        return mAppCacheMaxSize;
    }

    /**
     * The origin has exceeded its database quota.
     *
     * @param url                the URL that exceeded the quota
     * @param databaseIdentifier the identifier of the database on
     *                           which the transaction that caused the quota overflow was run
     * @param currentQuota       the current quota for the origin.
     * @param estimatedSize      the estimated size of a new database, or 0 if
     *                           this has been invoked in response to an existing database
     *                           overflowing its quota.
     * @param totalUsedQuota     is the sum of all origins' quota.
     * @param quotaUpdater       The callback to run when a decision to allow or
     *                           deny quota has been made. Don't forget to call this!
     */
    public void onExceededDatabaseQuota(String url,
                                        String databaseIdentifier, long currentQuota, long estimatedSize,
                                        long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
        long totalUnusedQuota = mGlobalLimit - totalUsedQuota - mAppCacheMaxSize;

        if (totalUnusedQuota <= 0) {
            // There definitely isn't any more space. Fire notifications
            // if needed and exit.
            if (totalUsedQuota > 0) {
                // We only fire the notification if there are some other websites
                // using some of the quota. This avoids the degenerate case where
                // the first ever website to use Web storage tries to use more
                // data than it is actually available. In such a case, showing
                // the notification would not help at all since there is nothing
                // the user can do.
                scheduleOutOfSpaceNotification();
            }
            quotaUpdater.updateQuota(currentQuota);

            return;
        }

        // We have some space inside mGlobalLimit.
        long newOriginQuota = currentQuota;
        if (newOriginQuota == 0) {
            // This is a new origin, give it the size it asked for if possible.
            // If we cannot satisfy the estimatedSize, we should return 0 as
            // returning a value less that what the site requested will lead
            // to webcore not creating the database.
            if (totalUnusedQuota >= estimatedSize) {
                newOriginQuota = estimatedSize;
            } else {
                newOriginQuota = 0;
            }
        } else {
            // This is an origin we have seen before. It wants a quota
            // increase. There are two circumstances: either the origin
            // is creating a new database or it has overflowed an existing database.

            // Increase the quota. If estimatedSize == 0, then this is a quota overflow
            // rather than the creation of a new database.
            long quotaIncrease = estimatedSize == 0 ?
                    Math.min(QUOTA_INCREASE_STEP, totalUnusedQuota) :
                    estimatedSize;
            newOriginQuota += quotaIncrease;

            if (quotaIncrease > totalUnusedQuota) {
                // We can't fit, so deny quota.
                newOriginQuota = currentQuota;
            }
        }

        quotaUpdater.updateQuota(newOriginQuota);
    }

    /**
     * The Application Cache has exceeded its max size.
     *
     * @param spaceNeeded    is the amount of disk space that would be needed
     *                       in order for the last appcache operation to succeed.
     * @param totalUsedQuota is the sum of all origins' quota.
     * @param quotaUpdater   A callback to inform the WebCore thread that a new
     *                       app cache size is available. This callback must always be executed at
     *                       some point to ensure that the sleeping WebCore thread is woken up.
     */
    public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
                                         WebStorage.QuotaUpdater quotaUpdater) {

        long totalUnusedQuota = mGlobalLimit - totalUsedQuota - mAppCacheMaxSize;

        if (totalUnusedQuota < spaceNeeded + APPCACHE_MAXSIZE_PADDING) {
            // There definitely isn't any more space. Fire notifications
            // if needed and exit.
            if (totalUsedQuota > 0) {
                // We only fire the notification if there are some other websites
                // using some of the quota. This avoids the degenerate case where
                // the first ever website to use Web storage tries to use more
                // data than it is actually available. In such a case, showing
                // the notification would not help at all since there is nothing
                // the user can do.
                scheduleOutOfSpaceNotification();
            }
            quotaUpdater.updateQuota(0);

            return;
        }
        // There is enough space to accommodate spaceNeeded bytes.
        mAppCacheMaxSize += spaceNeeded + APPCACHE_MAXSIZE_PADDING;
        quotaUpdater.updateQuota(mAppCacheMaxSize);

    }

    // Reset the notification time; we use this iff the user
    // use clear all; we reset it to some time in the future instead
    // of just setting it to -1, as the clear all method is asynchronous
    public static void resetLastOutOfSpaceNotificationTime() {
        mLastOutOfSpaceNotificationTime = System.currentTimeMillis() -
                NOTIFICATION_INTERVAL + RESET_NOTIFICATION_INTERVAL;
    }

    // Computes the global limit as a function of the size of the data
    // partition and the amount of free space on that partition.
    private long getGlobalLimit() {
        long freeSpace = mDiskInfo.getFreeSpaceSizeBytes();
        long fileSystemSize = mDiskInfo.getTotalSizeBytes();
        return calculateGlobalLimit(fileSystemSize, freeSpace);
    }

    /*package*/
    static long calculateGlobalLimit(long fileSystemSizeBytes,
                                     long freeSpaceBytes) {
        if (fileSystemSizeBytes <= 0
                || freeSpaceBytes <= 0
                || freeSpaceBytes > fileSystemSizeBytes) {
            return 0;
        }

        long fileSystemSizeRatio =
                2 << ((int) Math.floor(Math.log10(
                        fileSystemSizeBytes / (1024 * 1024))));
        long maxSizeBytes = (long) Math.min(Math.floor(
                fileSystemSizeBytes / fileSystemSizeRatio),
                Math.floor(freeSpaceBytes / 2));
        // Round maxSizeBytes up to a multiple of 1024KB (but only if
        // maxSizeBytes > 1MB).
        long maxSizeStepBytes = 1024 * 1024;
        if (maxSizeBytes < maxSizeStepBytes) {
            return 0;
        }
        long roundingExtra = maxSizeBytes % maxSizeStepBytes == 0 ? 0 : 1;
        return (maxSizeStepBytes
                * ((maxSizeBytes / maxSizeStepBytes) + roundingExtra));
    }

    // Schedules a system notification that takes the user to the WebSettings
    // activity when clicked.
    private void scheduleOutOfSpaceNotification() {
        if ((mLastOutOfSpaceNotificationTime == -1) ||
                (System.currentTimeMillis() - mLastOutOfSpaceNotificationTime > NOTIFICATION_INTERVAL)) {
            // setup the notification boilerplate.
            int icon = android.R.drawable.stat_sys_warning;
            CharSequence title = mContext.getString(
                    R.string.webstorage_outofspace_notification_title);
            CharSequence text = mContext.getString(
                    R.string.webstorage_outofspace_notification_text);
            long when = System.currentTimeMillis();
            Intent intent = new Intent(mContext, BrowserSettingActivity.class);
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                    BrowserClearDataPreferencesFragment.class.getName());
            PendingIntent contentIntent =
                    PendingIntent.getActivity(mContext, 0, intent, 0);
            Notification notification = new Notification(icon, title, when);
            //notification.setLatestEventInfo(mContext, title, text, contentIntent);
            NotificationExtension.setLatestEventInfo(notification, mContext, title, text, contentIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            // Fire away.
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mgr =
                    (NotificationManager) mContext.getSystemService(ns);
            if (mgr != null) {
                mLastOutOfSpaceNotificationTime = System.currentTimeMillis();
                mgr.notify(OUT_OF_SPACE_ID, notification);
            }
        }
    }
}

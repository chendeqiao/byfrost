package com.intelligence.browser.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.intelligence.browser.downloads.BrowserDownloadManager;
import com.intelligence.browser.utils.Constants;

public class BrowserReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (Constants.DOWNLOAD_VIEW_ACTION.equals(action)) {
            String key = intent.getStringExtra(Constants.DOWNLOAD_KEY);
            int state = intent.getIntExtra(Constants.DOWNLOAD_STATE, 0);
            BrowserDownloadManager.getInstance().downloadAction(key, state);
        }
    }
}

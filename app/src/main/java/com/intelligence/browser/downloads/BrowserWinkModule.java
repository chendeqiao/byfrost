package com.intelligence.browser.downloads;

import android.content.Context;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.downloads.support.DownloadStat;
import com.intelligence.browser.downloads.support.NotificationControllerImpl;
import com.intelligence.browser.downloads.support.SimpleWinkCallback;
import com.intelligence.browser.downloads.support.WinkNetworkSensor;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.commonlib.download.WinkBuilder;
import com.intelligence.commonlib.download.module.WinkModule;

public class BrowserWinkModule implements WinkModule {

    @Override
    public void applyOptions(Context context, WinkBuilder builder) {
        String userAgent = "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MTC19T; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/44.0.2403.117 " +
                "Mobile Safari/537.36";
        builder.setUserAgent(userAgent);
        builder.setNeedCheckLengthBeforeDownload(true);
        builder.setNetworkSensor(new WinkNetworkSensor());
        builder.setSimpleResourceStoragePath(BrowserSettings.getInstance().getDownloadPath());
    }

    @Override
    public void registerComponents(Context context, Wink wink) {
        wink.setNotificationController(new NotificationControllerImpl(context));
        wink.setCallback(new SimpleWinkCallback());
        wink.setStatHandler(new DownloadStat());
    }
}

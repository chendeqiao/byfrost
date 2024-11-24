package com.intelligence.browser.downloads.support;


import com.intelligence.commonlib.notification.NotificationCenter;
import com.intelligence.commonlib.download.WinkCallback;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.request.DownloadState;

public class SimpleWinkCallback implements WinkCallback {
    @Override
    public void onAdd(DownloadInfo... entities) {
        NotificationCenter.defaultCenter().publish(
                new WinkManageEvent(WinkManageEvent.EVENT_ADD, entities));
    }

    @Override
    public void onRemove(DownloadInfo... entities) {
        NotificationCenter.defaultCenter().publish(
                new WinkManageEvent(WinkManageEvent.EVENT_DELETE, entities));
    }

    @Override
    public void onClear() {
        NotificationCenter.defaultCenter().publish(
                new WinkManageEvent(WinkManageEvent.EVENT_CLEAR, null));
    }

    @Override
    public void onProgressChanged(DownloadInfo entity) {
        NotificationCenter.defaultCenter().publish(WinkEvent.progress(entity));
    }

    @Override
    public void onStatusChanged(DownloadInfo entity, DownloadState state) {
        if (state == DownloadState.completed) {
            NotificationCenter.defaultCenter().publish(new WinkManageEvent(WinkManageEvent.EVENT_ADD, null));
        }
        NotificationCenter.defaultCenter().publish(WinkEvent.statusChanged(entity, state));
    }
}

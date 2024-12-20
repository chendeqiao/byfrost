package com.intelligence.browser.downloads.support;


import com.intelligence.commonlib.download.request.DownloadInfo;

public class WinkManageEvent {
    public static final int EVENT_ADD = 0;
    public static final int EVENT_DELETE = 1;
    public static final int EVENT_CLEAR = 2;

    public final int mEvent;
    public final DownloadInfo[] mEntities;

    WinkManageEvent(int e, DownloadInfo[] es) {
        this.mEvent = e;
        this.mEntities = es;
    }
}

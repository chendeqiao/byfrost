package com.intelligence.browser.ui.home.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.commonlib.imageloader.cached.MemoryCache;
import com.intelligence.commonlib.imageloader.cached.WeakMemoryCache;
import com.intelligence.commonlib.imageloader.cached.disk.DiskCache;
import com.intelligence.commonlib.imageloader.cached.disk.UnlimitedDiskCache;
import com.intelligence.commonlib.network.Network;

import java.io.File;
import java.io.IOException;

public class WebIconLoader {

    private static final String WEB_ICONS_CACHE_DIR = "web_icons_cache";

    private Context mContext;
    private MemoryCache mMemoryCache = new WeakMemoryCache();
    private DiskCache mDiskCache;

    public WebIconLoader(Context context) {
        this.mContext = context;
        mDiskCache = new UnlimitedDiskCache(new File(context.getCacheDir(),WEB_ICONS_CACHE_DIR));
    }

    public void syncLoadBitmap(String iconUrl,String webUrl,NavigationService.FaviconCallback faviconCallback) {
        Bitmap bitmap = null;
        if(!TextUtils.isEmpty(iconUrl)) {
            try {
                bitmap = Network.DownloadImage(mContext, iconUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
//        if(bitmap == null){
//            NavigationService.getFaviconFromUrl(mContext, webUrl,faviconCallback);
//        }else {
            faviconCallback.onFaviconReceived(bitmap);
//        }
//        return bitmap;
    }

    public void saveCache(String key, Bitmap bmp) {
        if (bmp == null) return;
        mMemoryCache.put(key, bmp);
        try {
            mDiskCache.save(key, bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap readCache(String key) {
        Bitmap bitmap = mMemoryCache.get(key);
        if (bitmap != null) {
            return bitmap;
        }
        File file = mDiskCache.get(key);
        if (file != null) {
            return ImageUtils.readImgFile(file);
        }
        return null;
    }

}

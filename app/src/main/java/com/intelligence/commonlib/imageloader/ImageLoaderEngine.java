package com.intelligence.commonlib.imageloader;

import android.content.Context;
import android.view.View;

import com.intelligence.commonlib.imageloader.impl.DefaultImageLoader;
import com.intelligence.commonlib.tools.NetworkUtils;

public class ImageLoaderEngine {
    private static ImageLoaderEngine sInstance = null;
    private ImageLoader mImageLoader;
    private Context mContext;
    private ThreadLocal<ImageConfig> mConfig = new ThreadLocal<>();

    private ImageLoaderEngine() {
        mImageLoader = new DefaultImageLoader();
    }

    public static final ImageLoaderEngine getInstance() {
        if (sInstance == null) {
            sInstance = new ImageLoaderEngine();
        }
        return sInstance;
    }

    private ImageConfig getConfig() {
        ImageConfig config = mConfig.get();
        if (config == null) {
            config = new ImageConfig();
            mConfig.set(config);
        }
        return config;
    }

    public void init(Context context) {
        this.mContext = context;
    }

    public ImageLoaderEngine into(View v) {
        getConfig().view = v;
        return this;
    }

    public ImageLoaderEngine errorView(int resId) {
        getConfig().errorRes = resId;
        return this;
    }

    public ImageLoaderEngine defView(int resId) {
        getConfig().defRes = resId;
        return this;
    }

    public ImageLoaderEngine loadUrl(String url) {
        getConfig().url = url;
        return this;
    }

    public ImageLoaderEngine registerLoadingListener(ImageLoader.ImageLoadingListener listener) {
        getConfig().netListener = listener;
        return this;
    }

    private void begin() {
        if (getConfig().checkParams()) {
            mImageLoader.load(getConfig());
        }
    }

    public void load(View v, int defRes, String url) {
        this.load(v, defRes, url, null);
    }

    public void load(View v, int defRes, String url, ImageLoader.ImageLoadingListener listener) {
        getConfig().view = v;
        getConfig().url = url;
        getConfig().defRes = defRes;
        this.begin();
    }

    public boolean syncLoad(String url) {
        getConfig().url = url;
        getConfig().isSync = true;
        // First check network is available, then sync load image.
        return NetworkUtils.isNetworkAvailable() && mImageLoader.syncLoad(getConfig());
    }

}

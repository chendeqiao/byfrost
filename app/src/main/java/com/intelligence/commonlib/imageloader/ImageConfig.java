package com.intelligence.commonlib.imageloader;

import android.content.Context;
import android.view.View;

public class ImageConfig {
    public Context context;
    public String url;
    public int defRes;
    public int errorRes;
    public View view;
    public ImageLoader.ImageLoadingListener netListener;
    public boolean isSync = false ;

    public boolean checkParams() {
        return view != null;
    }
}

package com.intelligence.commonlib.imageloader;

import java.io.InputStream;

public interface ImageLoader {

    void load(ImageConfig config);

    boolean syncLoad(ImageConfig config);

    String getFilePathByUrl(String url) ;

    interface ImageLoadingListener {
        void onLoadBefore();

        void onLoadProcess(int curr, int size);

        void onLoadFail(int error);

        void onLoadSuccess(InputStream data);
    }


}

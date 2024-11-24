package com.intelligence.browser.markLock.lock;


import android.graphics.Bitmap;

import java.lang.ref.WeakReference;

class BGBmp {


    private static WeakReference<Bitmap> commonBGBmp;



    static Bitmap getCommonBGBmp() {
        if(commonBGBmp==null)
            return null;
        return commonBGBmp.get();
    }



    static void setCommonBGBmp(Bitmap bmp) {
        commonBGBmp = new WeakReference<>(bmp);
    }


}

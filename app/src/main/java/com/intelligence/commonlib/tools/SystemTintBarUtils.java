package com.intelligence.commonlib.tools;

import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

public class SystemTintBarUtils {

    public static void setStyleVersionO(Activity activity) {
//        try {
//            if(Build.VERSION.SDK_INT>=28) {
//                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
//                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//                activity.getWindow().setAttributes(lp);
//                // 设置页面全屏显示
//                final View decorView = activity.getWindow().getDecorView();
//                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            }
//        } catch (Exception e) {
//            //to do nothing.
//        }
    }


    public static void setSystemBarColor(Activity activity, int colorId) {
        try {
            if (Build.VERSION.SDK_INT < BuildUtil.VERSION_CODES.LOLLIPOP) {
                return;
            }
            if (mCurrentColorId == colorId) {
                return;
            }
            mCurrentColorId = colorId;
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, colorId));
        }catch (Exception e){
        }
    }

    private static int mCurrentColorId;
    public static void setSystemBarColorByValue(Activity activity, int color) {
        //由于4.4以下的状态栏设置以及横竖屏切换问题比较多,暂时参考上面的方式不设置状态栏颜色
        if (Build.VERSION.SDK_INT < BuildUtil.VERSION_CODES.LOLLIPOP) {
            return;
        }
        if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setStatusBarColor(color);
            return;
        }
        if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
//            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            WindowManager.LayoutParams params = window.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            params.flags |= bits;
            window.setAttributes(params);
        }

        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(color);
    }

    public static void cancelSystemBarImmersive(Activity activity) {
//        if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        } else if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.KITKAT) {
//            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
    }
}

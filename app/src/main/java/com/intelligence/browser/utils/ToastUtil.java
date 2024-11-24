package com.intelligence.browser.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.intelligence.browser.R;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.ScreenUtils;

public class ToastUtil {

    private static Toast mToast = null;

    public static void showShortToast(Context context, int retId) {
        try {
//        if (mToast == null) {
            mToast = toastShow(context, retId, Toast.LENGTH_SHORT);
//        } else {
//            mToast = toastShow(context, retId, Toast.LENGTH_SHORT);
//        }
            mToast.show();
        } catch (Exception e) {
            Log.e("byfrost Toast","showShortToast==>>");
        }
    }

    public static void show(int tips) {
        try {
            toastShow(Global.getInstance(), tips, Toast.LENGTH_LONG);
        } catch (Exception e) {
            Log.e("byfrost Toast","show==>>");
        }
    }

    public static void show(String tips) {
        try {
            toastShow(Global.getInstance(), tips, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("byfrost Toast","show==1>>");
        }
    }

    public static void showCenterofScreen(String tips) {
        try {
            Toast toast = toastShow(Global.getInstance(), tips, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } catch (Exception e) {
            Log.e("byfrost Toast","showCenterofScreen==1>>");
        }
    }

    public static void showShortToastByString(Context context, String hint) {
        try {
//        if (mToast == null) {
            mToast = toastShow(context, hint, Toast.LENGTH_SHORT);
//        } else {
//            mToast.setText(hint);
//            mToast.setDuration(Toast.LENGTH_SHORT);
//        }
            mToast.show();
        } catch (Exception e) {
            Log.e("byfrost Toast","showShortToastByString==1>>");
        }
    }


    public static void showLongToast(Context context, int retId) {
        try {
//        if (mToast == null) {
            mToast = toastShow(context, retId, Toast.LENGTH_LONG);
//        } else {
//            mToast.setText(retId);
//            mToast.setDuration(Toast.LENGTH_LONG);
//        }
            mToast.show();
        } catch (Exception e) {
            Log.e("byfrost Toast","showLongToast==1>>");
        }
    }


    public static void showLongToastByString(Context context, String hint) {
        try {
//            if (mToast == null) {
            mToast = toastShow(context, hint, Toast.LENGTH_LONG);
//            } else {
//                mToast.setText(hint);
//                mToast.setDuration(Toast.LENGTH_LONG);
//            }
            mToast.show();
        } catch (Exception e) {
            Log.e("byfrost Toast","showLongToastByString==1>>");
        }
    }

    public static Toast toastShow(Context context, int rid, int duration) {
        return toastShow(context, context.getResources().getText(rid), duration);
    }

    public static Toast toastShow(Context context, CharSequence tvString, int duration) {
        try {
            View layout = LayoutInflater.from(context).inflate(R.layout.browser_toast_view, null);
            TextView text = layout.findViewById(android.R.id.message);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            text.setText(tvString);
            Toast toast = new Toast(context);
            toast.setDuration(duration);

            int yOffset = ScreenUtils.dpToPxInt(context, 50); // 这里可以根据需要调整 Y 轴偏移量（高度）
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, yOffset);

            toast.setView(layout);
            return toast;
        } catch (Exception e) {
            return null;
        }
    }
}

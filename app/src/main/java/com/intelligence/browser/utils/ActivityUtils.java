package com.intelligence.browser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.intelligence.browser.ui.activity.BrowserComboActivity;
import com.intelligence.browser.ui.activity.BrowserDownloadActivity;
import com.intelligence.browser.webview.Controller;
import com.intelligence.browser.ui.MainActivity;
import com.intelligence.browser.R;
import com.intelligence.qr.BrowserCaptureActivity;
import com.intelligence.qr.BrowserIntentIntegrator;

public class ActivityUtils {
    public final static int QRCODE_RESULT = 1212;

    public static void startActivity(Context context, Class<? extends Activity> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    public static void startDownloadActivity(Activity activity, int state) {
        Intent intent = new Intent(activity, BrowserDownloadActivity.class);
        intent.putExtra(Constants.DOWNLOAD_STATE, state + "");
        activity.startActivityForResult(intent, Controller.COMBO_VIEW);
        activity.overridePendingTransition(R.anim.browser_zoom_in, R.anim.browser_zoom_out);
    }

    public static void startComboViewActivity(Activity activity, int code, int menuId,int pageIndx) {
        Intent intent = new Intent(activity, BrowserComboActivity.class);
        intent.putExtra(Constants.MENU_ID, menuId);
        intent.putExtra(Constants.PAGE_INDEX, pageIndx+"");
        activity.startActivityForResult(intent, code);
        activity.overridePendingTransition(R.anim.browser_zoom_in, R.anim.browser_zoom_out);
    }

    public static void openUrl(Activity srcActivity, String url) {
        Intent intent = srcActivity.getPackageManager().getLaunchIntentForPackage(srcActivity.getPackageName());
        intent.setClass(srcActivity, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        srcActivity.startActivity(intent);
    }

    public static void startForResultActivity(Activity context, Class<? extends Activity> cls,int requestCode) {
        Intent intent = new Intent(context, cls);
        context.startActivityForResult(intent, requestCode);
    }

    public static void startQRCodeScanner(Activity context){
        BrowserIntentIntegrator integrator = new BrowserIntentIntegrator(context);
        integrator.setDesiredBarcodeFormats(Controller.QR_CODE);
        integrator.setCameraId(0);  // 使用后置摄像头
        integrator.setBeepEnabled(false); // 扫描成功后蜂鸣
        integrator.setBarcodeImageEnabled(false); // 允许保存二维码图像
        integrator.initiateScan();
    }

    public static void startThotoPage(Activity activity,int requestCode) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(intent, activity.getString(R.string.select_qr_code));
        activity.startActivityForResult(wrapperIntent, requestCode);
    }
}






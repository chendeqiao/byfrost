package com.intelligence.qr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.intelligence.browser.R;
import com.intelligence.browser.downloads.DownloadHandler;
import com.intelligence.browser.manager.WallpaperHandler;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.webview.Controller;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.IOUtils;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.qr.barcodescanner.CaptureManager;
import com.intelligence.qr.barcodescanner.DecoratedBarcodeView;
import com.intelligence.qr.barcodescanner.camera.CameraInstance;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Hashtable;

public class BrowserCaptureActivity extends Activity {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageView mPhotoView;
    private ImageView mFlashLamp;
    private ImageView mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barcodeScannerView = initializeContent();
        mPhotoView = findViewById(R.id.qr_qrcode_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasPermission = DownloadHandler.hasReadWritePermissions(BrowserCaptureActivity.this);
                if (hasPermission) {
                    openGallery();
                } else {
                    DeviceInfoUtils.verifyStoragePermissions((Activity) BrowserCaptureActivity.this, DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_QR);
                }
//                ActivityUtils.startThotoPage(BrowserCaptureActivity.this,PHOTO_CODE);
            }
        });
        mFlashLamp = findViewById(R.id.qr_flashlamp);
        mFlashLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeScannerView != null) {
                    if (barcodeScannerView.isOpenLight()) {
                        barcodeScannerView.setTorchOff();
                    } else {
                        barcodeScannerView.setTorchOn();
                    }
                }
            }
        });
        mCancel = findViewById(R.id.qr_cancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.browser_zxing_capture);
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == DeviceInfoUtils.REQUEST_EXTERNAL_STORAGE_QR) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                showPermissionDeniedDialog(R.string.browser_permission_qr);
            }
        }
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void showPermissionDeniedDialog(int message) {
        new AlertDialog.Builder(BrowserCaptureActivity.this)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用程序的权限设置页面
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BrowserCaptureActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        BrowserCaptureActivity.this.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static final int PHOTO_CODE = 1112;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PHOTO_CODE);
    }

//    public Bitmap getBitmapForResult(int requestCode, int resultCode, Intent data) {
//        String photo_path = "";
//        if (Activity.RESULT_OK == resultCode) {
//            if (requestCode == PHOTO_CODE) {
//                String[] project = {MediaStore.Images.Media.DATA};
//                Cursor cursor = getContentResolver().query(data.getData(), project, null, null, null);
//                if (cursor.moveToFirst()) {
//                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                    photo_path = cursor.getString(index);
//                    if (photo_path == null) {
//                        photo_path = ImageUtils.getPath(BrowserCaptureActivity.this, data.getData());
//                    }
//                }
//                IOUtils.closeCursor(cursor);
//                final String temp_photo_path = photo_path;
//                new Thread() {
//                    @Override
//                    public void run() {
//                        Result result = ScanningImage(temp_photo_path);
//                        if (result == null) {
//                            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ToastUtil.show(R.string.picture_nocodeinfo);
//                                }
//                            });
//                        } else {
//                            final String recoder = StringUtil.recoder(result.toString());
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Intent intent = new Intent();
//                                    intent.putExtra(String.valueOf(ActivityUtils.QRCODE_RESULT), recoder);
//                                    setResult(BrowserCaptureActivity.RESULT_OK, intent);
//                                    finish();
//                                }
//                            });
//                        }
//                    }
//                }.start();
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    String qrCodeText = decodeQRCode(bitmap);
                    if (qrCodeText != null) {
                        Intent intent = new Intent();
                        intent.putExtra(Controller.QR_CODE, qrCodeText);
                        setResult(this.RESULT_OK, intent);
                        finish();
                    } else {
                        ToastUtil.show(Global.getInstance().getResources().getString(R.string.picture_nocodeinfo));
                    }
                } catch (IOException e) {
                    ToastUtil.show(Global.getInstance().getResources().getString(R.string.picture_nocodeinfo));
                }
            }
        }else {

        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    //    public Result ScanningImage(String path) {
//        if (TextUtils.isEmpty(path)) {
//            return null;
//        }
//        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
//        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
//        options.inJustDecodeBounds = false;
//        int sampleSize = (int) (options.outHeight / (float) 200);
//        if (sampleSize <= 0) {
//            sampleSize = 1;
//        }
//        options.inSampleSize = sampleSize;
//        scanBitmap = BitmapFactory.decodeFile(path, options);
//        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
//        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//        QRCodeReader reader = new QRCodeReader();
//        try {
//            decodeQRCode(scanBitmap);
////            return reader.decode(bitmap, hints);
//        } catch (ReaderException e) {
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                @Override
//                public void run() {
//                    ToastUtil.show(R.string.picture_nocodeinfo);
//                }
//            });
//        }
//        return null;
//    }

    private String decodeQRCode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            Result result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            ToastUtil.show(Global.getInstance().getResources().getString(R.string.picture_nocodeinfo));
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
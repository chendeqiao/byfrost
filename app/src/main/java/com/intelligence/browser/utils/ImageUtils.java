package com.intelligence.browser.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.intelligence.browser.controller.BackgroundHandler;
import com.intelligence.browser.R;
import com.intelligence.browser.data.SearchEngineEntity;
import com.intelligence.commonlib.tools.IOUtils;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils {

    private static final String LOGTAG = ImageUtils.class.getSimpleName();

    public static final int ALPHA_TRANSPARENT = 0; //Alpha通道为0x00， 即0时为透明像素

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(Context context, final Uri uri) {
        final boolean isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitkat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] slit = docId.split(":");
                final String type = slit[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + slit[1];
                }
            } else if (isDownloadDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id = ?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            IOUtils.closeCursor(cursor);
        }
        return null;
    }

    public static Bitmap drawableToBitmap(Context context, int drawableId) {
        try {
            // 从资源中获取 Drawable 对象
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            if (drawable == null) {
                return null;
            }
            // 设置 Drawable 的尺寸
            int width = (int) ScreenUtils.dpToPx(context, 36);
            int height = (int) ScreenUtils.dpToPx(context, 36);
            drawable.setBounds(0, 0, width, height);
            // 创建一个 Bitmap 并将 Drawable 绘制到其中
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.draw(canvas);
            return bitmap;
        }catch (Exception e){
        }
        return null;
    }

    /**
     * Whether the Uri authority is DownloadsProvider
     *
     * @param uri
     * @return
     */
    public static boolean isDownloadDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * whethre the uri authority is ExternalStorageProvider
     *
     * @param uri
     * @return
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        if (bm == null || bm.isRecycled()) {
            return null;
        }
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, os);
        return os.toByteArray();
    }

    static ThreadLocal<BitmapFactory.Options> sOptions = new ThreadLocal<BitmapFactory.Options>() {
        @Override
        protected BitmapFactory.Options initialValue() {
            return new BitmapFactory.Options();
        }
    };

    public static
    @Nullable
    Bitmap getBitmap(byte[] data, Bitmap inBitmap) {
        if (data == null) {
            return null;
        }
        if (inBitmap == null) {
            return ImageUtils.decodeByteToBitmap(data);
        }
        BitmapFactory.Options opts = sOptions.get();
        opts.inBitmap = inBitmap;
        opts.inSampleSize = 1;
        opts.inScaled = false;
        try {
            return decodeByteToBitmap(data, opts);
        } catch (IllegalArgumentException ex) {
            // Failed to re-use bitmap, create a new one
            return decodeByteToBitmap(data);
        }
    }

    public static
    @Nullable
    Bitmap decodeByteToBitmap(byte[] imgByte) {
        return decodeByteToBitmap(imgByte, null);
    }

    public static
    @Nullable
    Bitmap decodeByteToBitmap(byte[] imgByte, BitmapFactory.Options options) {
        InputStream input = new ByteArrayInputStream(imgByte);
        Bitmap bitmap = null;
        try {
            SoftReference<Bitmap> softRef = new SoftReference<>(BitmapFactory.decodeStream(
                    input, null, options));
            bitmap = softRef.get();
        } catch (OutOfMemoryError e) {
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static Bitmap readImgFile(File imgFile) {
        Bitmap bitmap = null;
        if (imgFile != null && imgFile.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(imgFile);
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return bitmap;
    }

    public static Bitmap syncDownloadEngineIcon(final SearchEngineEntity entity, final ImageLoadListener listener) {
        BackgroundHandler.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = downloadEngineIcon(entity.getImageUrl());
                if (listener != null) {
                    entity.setImageIcon(ImageUtils.bitmapToBytes(bitmap));
                    listener.onLoadSuccess(entity);
                }
            }
        });
        return null;
    }

    public static Bitmap downloadEngineIcon(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();

            if (connection != null && connection.getResponseCode() == 200) {
                InputStream content = connection.getInputStream();
                Bitmap icon = null;
                try {
                    icon = BitmapFactory.decodeStream(content, null, null);
                } finally {
                    try {
                        if (content != null) {
                            content.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                return icon;
            }
        } catch (IOException ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static void saveBitmap(Bitmap bm, String picName, String filePath) {
        File f = new File(filePath, picName);

        FileOutputStream out = null;
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            out = new FileOutputStream(f);
            if (out == null) {
                return;
            }
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static @Nullable Bitmap createBitmap(int width, int height) {
        return createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    public static @Nullable Bitmap createBitmap(int width, int height, Bitmap.Config config) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static Bitmap makeRoundCornerImage(Bitmap bitmap, int pixels) {
        if (bitmap == null || pixels == 0) return bitmap;
        Bitmap output = createBitmap(bitmap.getWidth(), bitmap.getHeight());
        if (output == null) {
            return null;
        }
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;
        // 抗锯齿
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public interface ImageLoadListener {
        void onLoadSuccess(SearchEngineEntity entity);
    }

    public static void tinyIconColor(ImageView imageView) {
        imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), R.color.black), PorterDuff.Mode.SRC_IN);
    }

    public static void tinyIconColor(ImageView imageView, int color) {
        imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), color), PorterDuff.Mode.SRC_IN);
    }

    public static void loadLocalImage(DownloadInfo downloadInfo, Callback callback) {
        if (downloadInfo == null || callback == null) {
            return;
        }
        BackgroundHandler.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = FileUtils.getVideoThumbnail(downloadInfo.getLocalFilePath());
                callback.onResponse(bitmap);
            }
        });
    }

    public static void loadLocalImage(String imagePath, int width, int height, Callback callback) {
        if (TextUtils.isEmpty(imagePath) || callback == null) {
            return;
        }
        BackgroundHandler.execute(new Runnable() {
            @Override
            public void run() {
                // 设置为适当的采样率以获取缩略图
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true; // 只获取尺寸
                BitmapFactory.decodeFile(imagePath, options);

                // 计算采样率
                options.inSampleSize = calculateInSampleSize(options, width, height);
                options.inJustDecodeBounds = false; // 重新加载位图
                callback.onResponse(BitmapFactory.decodeFile(imagePath, options));
            }
        });
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 计算合适的采样率
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public interface Callback {
        void onResponse(Bitmap bitmap);
    }

}

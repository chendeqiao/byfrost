package com.intelligence.commonlib.imageloader.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.intelligence.commonlib.imageloader.ImageConfig;
import com.intelligence.commonlib.imageloader.ImageLoader;
import com.intelligence.commonlib.imageloader.cached.MemoryCache;
import com.intelligence.commonlib.imageloader.cached.WeakMemoryCache;
import com.intelligence.commonlib.imageloader.cached.disk.DiskCache;
import com.intelligence.commonlib.imageloader.cached.disk.UnlimitedDiskCache;
import com.intelligence.commonlib.imageloader.utils.MemoryCacheUtils;
import com.intelligence.commonlib.network.Network;
import com.intelligence.commonlib.network.NetworkServices;
import com.intelligence.commonlib.network.UpdateHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultImageLoader implements ImageLoader {
    private static final String TAG = "DefaultImageLoader";
    private static final String CACHE_PATH = "/sdcard/Turbo";

    private MemoryCache mCache = new WeakMemoryCache();
    private DiskCache mDiskCache = new UnlimitedDiskCache(new File(CACHE_PATH));
    private Handler mHandle = new Handler();
    private Context mContext;

    public DefaultImageLoader(Context context) {
        this.mContext = context;
    }

    public DefaultImageLoader() {
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    @Override
    public void load(ImageConfig config) {
        String cacheKey = MemoryCacheUtils.generateKey(config.url, null);
        Bitmap bitmap = mCache.get(cacheKey);
        ImageView imageView = (ImageView) config.view;
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            if (config.defRes > 0)
                imageView.setImageResource(config.defRes);
            File file = mDiskCache.get(config.url);
            ImageInfo imageInfo = new ImageInfo(file);
            if (file != null && file.exists()) {
                bitmap = decodeImage(imageInfo);
            }
            if (bitmap != null) {
                mCache.put(cacheKey, bitmap);
                imageView.setImageBitmap(bitmap);
            } else {
                imageInfo.memCacheKey = cacheKey;
                imageInfo.url = config.url;
                imageInfo.view = config.view;
                NetworkServices.getInstance().updateServices(new InternalHandler(config.context, imageInfo));
            }
        }
    }

    public boolean syncLoad(final ImageConfig config) {
        File img = mDiskCache.get(config.url);
        if (img.exists()) {
            return true;
        }
        final String cacheKey = MemoryCacheUtils.generateKey(config.url, null);
        try {
            Network.OutsourceHandler handler = new Network.OutsourceHandler() {
                @Override
                public boolean outsource(InputStream input) {
                    final Bitmap bitmap = decode(input);
                    mCache.put(cacheKey, bitmap);
                    if (config.view != null) {
                        mHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) config.view).setImageBitmap(bitmap);
                            }
                        });
                    }

                    saveToDisk(config.url, bitmap);
                    return true;
                }
            };
            return Network.downloadFile(mContext, config.url, handler);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Bitmap decodeImage(ImageInfo info) {
        return decodeImage(info.diskFile);
    }

    private Bitmap decodeImage(File imgPath) {
        Bitmap bitmap = null;
        if (imgPath != null && imgPath.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(imgPath);
                bitmap = decode(in);
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

    /**
     * 图像的缩放，大小强制设置可以在这里设置
     * 暂时对图的大小不做限制
     *
     * @param input
     * @return
     */
    private Bitmap decode(InputStream input) {
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
        return BitmapFactory.decodeStream(input, null, options);
    }

    class ImageInfo {
        public File diskFile;
        public String memCacheKey;
        public String url;
        public View view;

        public ImageInfo(File diskFile) {
            this.diskFile = diskFile;
        }
    }

    private class InternalHandler extends UpdateHandler {
        private ImageInfo mInfo;

        public InternalHandler(Context context, ImageInfo info) {
            super(context);
            this.mInfo = info;
        }

        @Override
        public void doUpdateFail() {
            super.doUpdateFail();
        }

        @Override
        public boolean doUpdateNow() {
            try {
                Network.OutsourceHandler handler = new Network.OutsourceHandler() {
                    @Override
                    public boolean outsource(InputStream input) {
                        final Bitmap bitmap = decode(input);
                        mCache.put(mInfo.memCacheKey, bitmap);
                        mHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) mInfo.view).setImageBitmap(bitmap);
                            }
                        });
                        saveToDisk(mInfo.url, bitmap);
                        return true;
                    }
                };
                Network.downloadFile(mContext, mInfo.url, handler);
                return true;
            } catch (SecurityException e) {
                e.printStackTrace();
            } finally {
            }
            return false;
        }
    }

    private void saveToDisk(String url, Bitmap input) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(CACHE_PATH, mDiskCache.getFileByUrl(url)));
            input.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    @Override
    public String getFilePathByUrl(String url) {
        return CACHE_PATH + File.separator + mDiskCache.getFileByUrl(url);
    }

}

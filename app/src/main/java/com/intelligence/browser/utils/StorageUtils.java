package com.intelligence.browser.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import androidx.core.os.EnvironmentCompat;

import com.intelligence.browser.R;
import com.intelligence.browser.data.StorageBean;
import com.intelligence.commonlib.tools.BuildUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class StorageUtils {

    private static final String TAG = StorageUtils.class.getSimpleName();

    public static ArrayList<StorageBean> getStorageData(Context pContext) {
        ArrayList<StorageBean> list = new ArrayList<>();
        try {
            File[] externalFilesDirs = pContext.getExternalFilesDirs(null);

            for (File file : externalFilesDirs) {
                if (file != null) {
                    StorageBean storageBean = new StorageBean();
                    String path = file.getAbsolutePath();
                    String state = Environment.getExternalStorageState(file);
                    boolean removable = Environment.isExternalStorageRemovable(file);

                    long totalSize = 0;
                    long availableSize = 0;
                    if (Environment.MEDIA_MOUNTED.equals(state)) {
                        totalSize = StorageUtils.getTotalSize(path);
                        availableSize = StorageUtils.getAvailableSize(path);
                    }

                    storageBean.setPath(path);
                    storageBean.setRemovable(removable);
                    storageBean.setMounted(state);
                    storageBean.setTotalSize(totalSize);
                    storageBean.setAvailableSize(availableSize);
                    list.add(storageBean);
                }
            }
        }catch (Exception e){
        }
        return list;
    }

    public static long getTotalSize(String path) {
        try {
            final StatFs statFs = new StatFs(path);
            long blockSize = 0;
            long blockCountLong = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                blockCountLong = statFs.getBlockCountLong();
            } else {
                blockSize = statFs.getBlockSize();
                blockCountLong = statFs.getBlockCount();
            }
            return blockSize * blockCountLong;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long getAvailableSize(String path) {
        try {
            final StatFs statFs = new StatFs(path);
            long blockSize = 0;
            long availableBlocks = 0;
            if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFs.getBlockSizeLong();
                availableBlocks = statFs.getAvailableBlocksLong();
            } else {
                blockSize = statFs.getBlockSize();
                availableBlocks = statFs.getAvailableBlocks();
            }
            return availableBlocks * blockSize;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static final long A_GB = 1073741824;
    public static final long A_MB = 1048576;
    public static final int A_KB = 1024;

    public static String fmtSpace(long space) {
        if (space <= 0) {
            return "0";
        }
        double gbValue = (double) space / A_GB;
        if (gbValue >= 1) {
            return String.format("%.2fGB", gbValue);
        } else {
            double mbValue = (double) space / A_MB;
            if (mbValue >= 1) {
                return String.format("%.2fMB", mbValue);
            } else {
                final double kbValue = space / A_KB;
                return String.format("%.2fKB", kbValue);
            }
        }
    }

    public static String getAllSDSpace(Context context) {
        ArrayList<StorageBean> storageBeanList = getStorageData(context);
        long total = 0;
        long available = 0;

        if (storageBeanList != null) {
            for (StorageBean bean :
                    storageBeanList) {
                total = total + bean.getTotalSize();
                available = available + bean.getAvailableSize();
            }
        }

        return context.getString(R.string.available) + " : " + fmtSpace(available) + " / " + context.getString(R.string
                .total) + " : " + fmtSpace(total);
    }

    public static boolean isSDExist(Context context, String patha) {
        final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            //得到StorageManager中的getVolumeList()方法的对象
            final Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");
            //---------------------------------------------------------------------

            //得到StorageVolume类的对象
            final Class<?> storageValumeClazz = Class.forName("android.os.storage.StorageVolume");
            //---------------------------------------------------------------------
            //获得StorageVolume中的一些方法
            final Method getPath = storageValumeClazz.getMethod("getPath");
            Method isRemovable = storageValumeClazz.getMethod("isRemovable");

            Method mGetState = null;
            //getState 方法是在4.4_r1之后的版本加的，之前版本（含4.4_r1）没有
            // （http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4
            // .4_r1/android/os/Environment.java/）
            if (Build.VERSION.SDK_INT > BuildUtil.VERSION_CODES.KITKAT) {
                try {
                    mGetState = storageValumeClazz.getMethod("getState");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            //---------------------------------------------------------------------

            //调用getVolumeList方法，参数为：“谁”中调用这个方法
            final Object invokeVolumeList = getVolumeList.invoke(storageManager);
            //---------------------------------------------------------------------
            final int length = Array.getLength(invokeVolumeList);

            for (int i = 0; i < length; i++) {
                final Object storageValume = Array.get(invokeVolumeList, i);//得到StorageVolume对象
                final String path = (String) getPath.invoke(storageValume);
                final boolean removable = (Boolean) isRemovable.invoke(storageValume);
                String state = null;
                if (mGetState != null) {
                    state = (String) mGetState.invoke(storageValume);
                } else if (Build.VERSION.SDK_INT >= BuildUtil.VERSION_CODES.KITKAT) {
                    state = Environment.getStorageState(new File(path));
                } else if (removable) {
                    state = EnvironmentCompat.getStorageState(new File(path));
                } else {
                    //不能移除的存储介质，一直是mounted
                    state = Environment.MEDIA_MOUNTED;
                }

                if (patha.startsWith(path)) {
                    return state.equals(Environment.MEDIA_MOUNTED);
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

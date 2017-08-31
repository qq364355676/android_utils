package com.utils.commonutils.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * SD卡相关工具类
 */

public class SDCardUtils {

    /**
     * 判断SDCard是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    }

    /**
     * 获取内置sdcard卡路径
     *
     * @return
     */
    public static String getInnerSDCardPath() {
        // TODO Auto-generated method stub
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取外置SD卡路径
     *
     * @return 应该就一条记录或空
     */
    public static String getExtSDCardPath(Context context) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取SD卡的剩余容量 单位byte
     * @param context null 表示内置SD卡 否则 表示外置SD卡
     *
     * @return  内置SD卡或外置SD卡剩余容量
     */
    public static long getSDCardAllSize(Context context) {
        StatFs stat ;
        if (isSDCardEnable()) {
            if (context ==null){
                stat = new StatFs(getInnerSDCardPath());
            }else {
                stat = new StatFs(getExtSDCardPath(context));
            }
            // 获取空闲的数据块的数量
            long availableBlocks = stat.getAvailableBlocksLong() - 4;
            // 获取单个数据块的大小（byte）
            long freeBlocks = stat.getAvailableBlocksLong();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }

    /**
     * 获取指定路径所在空间的剩余可用容量字节数，单位byte
     *
     * @param filePath
     * @param context   内置SD卡路径时可以为空
     * @return 容量字节 SDCard可用空间，内部存储可用空间 ，外置SD卡存储可用空间
     */
    public static long getFreeBytes(String filePath,Context context) {
        // 如果是sd卡的下的路径，则获取sd卡可用容量
        if (filePath.startsWith(getInnerSDCardPath())) {
            filePath = getInnerSDCardPath();
        }else if (filePath.startsWith(getExtSDCardPath(context))){
            filePath = getExtSDCardPath(context);
        }else {// 如果是内部存储的路径，则获取内存存储的可用容量
            filePath = Environment.getDataDirectory().getAbsolutePath();
        }
        StatFs stat = new StatFs(filePath);
        long availableBlocks = stat.getAvailableBlocksLong() - 4;
        return stat.getBlockSizeLong() * availableBlocks;
    }
}

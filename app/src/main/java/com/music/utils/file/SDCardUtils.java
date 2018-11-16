package com.music.utils.file;

import android.os.Environment;
import android.os.StatFs;
import java.io.File;
import java.text.DecimalFormat;

/**
 * [description about this class]
 * 内存卡工具类
 * @author jack
 */
public class SDCardUtils {

    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");
    private final static SDCardUtils instance = new SDCardUtils();

    /**
     * 单例对象实例
     */
    public static SDCardUtils getInstance(){
        return instance;
    }

    /**
     * 判断sdcard是否存在
     * @return
     */
    public boolean existSDCard() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return true;
        } else{
            return false;
        }
    }

    /**
     * 查看SD卡的剩余空间
     * @return
     */
    public String getSDFreeSize(){

        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
        }else{
            blockSize = sf.getBlockSize();
        }
        //空闲的数据块的数量
        long freeBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeBlocks = sf.getAvailableBlocksLong();
        }else{
            freeBlocks = sf.getAvailableBlocks();
        }
        //返回SD卡空闲大小
        return formatFileSize(freeBlocks * blockSize,false); //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        //return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * 查看SD卡总容量
     * @return
     */
    public String getSDAllSize(){

        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = sf.getBlockSizeLong();
        }else{
            blockSize = sf.getBlockSize();
        }
        //获取所有数据块数
        long allBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            allBlocks = sf.getBlockCountLong();
        }else{
            allBlocks = sf.getBlockCount();
        }
        //返回SD卡大小
        return formatFileSize(allBlocks * blockSize,false); //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        //return (allBlocks * blockSize)/1024/1024; //单位MB
    }

    /**
     * 单位换算
     *
     * @param size 单位为B
     * @param isInteger 是否返回取整的单位
     * @return 转换后的单位
     */
    public String formatFileSize(long size, boolean isInteger) {

        DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
        String fileSizeString = "0MB";
        if (size < 1024 && size > 0) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1024 * 1024) {
            fileSizeString = df.format((double) size / 1024) + "K";
        } else if (size < 1024 * 1024 * 1024) {
            fileSizeString = df.format((double) size / (1024 * 1024)) + "MB";
        } else {
            fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
        }
        return fileSizeString;
    }
}

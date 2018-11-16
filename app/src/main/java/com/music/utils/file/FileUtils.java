package com.music.utils.file;

import android.text.TextUtils;
import com.briup.utils.Constants;
import java.io.File;
import java.io.IOException;

/**
 * 文件工具类
 */
public class FileUtils {

    private final static FileUtils instance = new FileUtils();

    /**
     * 单例对象实例
     */
    public static FileUtils getInstance(){
        return instance;
    }

    /**
     * 创建根目录(直接在手机目录下)
     */
    public boolean createRootDirectory(){
        if(SDCardUtils.getInstance().existSDCard()){
            File file = new File(Constants.ROOT_FILE_PATH);
            if(!file.exists()){
                return file.mkdirs();
            }
            return true;
        }
        return false;
    }

    /**
     * 创建头像默认根目录
     * @return
     */
    public boolean createPhotoDirectory(){
        if(createRootDirectory()){
            File file = new File(Constants.DEFAULT_PHOTO_IMAGE_DIR);
            if(!file.exists()){
                return file.mkdirs();
            }
            return true;
        }
        return false;
    }

    /**
     * 创建身份证正面默认根目录
     * @return
     */
    public boolean createShenFZDirectory1(){
        if(createRootDirectory()){
            File file = new File(Constants.DEFAULT_PHOTO_IMAGE_DIR);
            if(!file.exists()){
                return file.mkdirs();
            }
            return true;
        }
        return false;
    }

    /**
     * 创建身份证反面默认根目录
     * @return
     */
    public boolean createShenFZDirectory2(){
        if(createRootDirectory()){
            File file = new File(Constants.DEFAULT_PHOTO_IMAGE_DIR);
            if(!file.exists()){
                return file.mkdirs();
            }
            return true;
        }
        return false;
    }

    /**
     * 创建缓存根目录(应用卸载后会被自动删除)
     */
    public boolean createCacheRootDirectory(){
        File file = new File(Constants.CACHE_ROOT_FILE_PATH);
        if(!file.exists()){
            return file.mkdirs();
        }
        return false;
    }

    /**
     * 得到根目录(直接在手机目录下)
     */
    public String getRootDirectory(){
        if(createRootDirectory()){
            return Constants.ROOT_FILE_PATH;
        }
        return "";
    }

    /**
     *  删除文件夹和文件夹里面的文件
      */
    public void deleteDir(String path) {
        File dir = new File(path);
        if (dir == null || !dir.exists() || !dir.isDirectory()){
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDir(path); // 递规的方式删除文件夹
            }
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 文件是否存在
     * @param path
     * @return
     */
    public boolean fileIsExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 创建文件
     * @param fileName
     * @param isCacheDir true 在缓存目录创建文件 false 在手机目录下创建文件
     * @return
     */
    public boolean createFile(String fileName, boolean isCacheDir){

        if(TextUtils.isEmpty(fileName)){
            return false;
        }else{
            if(isCacheDir){
                if(createCacheRootDirectory()){
                    File file = new File(Constants.ROOT_FILE_PATH + File.separator + fileName);
                    if(!file.exists()){
                        try {
                            return file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        file.delete();
                        try {
                            return file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                if(SDCardUtils.getInstance().existSDCard()){
                    if(createRootDirectory()){
                        File file = new File(Constants.ROOT_FILE_PATH + File.separator + fileName);
                        if(!file.exists()){
                            try {
                                return file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            file.delete();
                            try {
                                return file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    return false;
                }
            }
        }
        return false;
    }
}

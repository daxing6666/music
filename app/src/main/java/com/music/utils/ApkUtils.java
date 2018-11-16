package com.music.utils;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import com.music.AppDroid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * [description about this class]
 * apk操作工具类
 * @author jack
 */

public class ApkUtils {

    private final static ApkUtils instance = new ApkUtils();

    /**
     * 单例对象实例
     */
    public static ApkUtils getInstance(){
        return instance;
    }

    /**
     * 获取应用名称
     * @return
     */
    public String getApkName(){
        PackageInfo pkg = null;
        try {
            pkg = AppDroid.getInstance().getPackageManager().getPackageInfo(
                    AppDroid.getInstance().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appName = pkg.applicationInfo.loadLabel(AppDroid.getInstance().getPackageManager()).toString();
        return appName;
    }

    /**
     * 获得APP包名
     *
     * @return
     */
    public String getApkPackageName() {
        return AppDroid.getInstance().getApplicationContext().getPackageName();
    }

    /**
     * 获得磁盘缓存目录路径(应用卸载后会被自动删除)
     * @return
     */
    public String getDiskCacheDirPath()
    {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = AppDroid.getInstance().getApplicationContext().getExternalCacheDir().getPath();
        }
        else {
            cachePath =  AppDroid.getInstance().getApplicationContext().getFilesDir().getPath();
        }
        return cachePath;
    }


    /** 安装一个apk文件 */
    public void installApk(File uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        AppDroid.getInstance().startActivity(intent);
    }

    /** 卸载一个app */
    public static void uninstallApk(String packageName) {
        //通过程序的包名创建URI
        Uri packageURI = Uri.parse("package:" + packageName);
        //创建Intent意图
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        //执行卸载程序
        AppDroid.getInstance().startActivity(intent);
    }

    /** 检查手机上是否安装了指定的软件 */
    public boolean isAvailable(String packageName) {
        // 获取packagemanager
        final PackageManager packageManager = AppDroid.getInstance().getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        // 用于存储所有已安装程序的包名
        List<String> packageNames = new ArrayList<>();
        // 从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        // 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }

    /** 检查手机上是否安装了指定的软件 */
    public boolean isAvailable(File file) {
        return isAvailable(getPackageName(file.getAbsolutePath()));
    }

    /** 根据文件路径获取包名 */
    public static String getPackageName(String filePath) {
        PackageManager packageManager = AppDroid.getInstance().getPackageManager();
        PackageInfo info = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            return appInfo.packageName;  //得到安装包名称
        }
        return null;
    }

    /** 从apk中获取版本信息 */
    public String getChannelFromApk(String channelPrefix) {
        //从apk包中获取
        ApplicationInfo appinfo = AppDroid.getInstance().getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + channelPrefix;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String[] split = ret.split(channelPrefix);
        String channel = "";
        if (split.length >= 2) {
            channel = ret.substring(key.length());
        }
        return channel;
    }

    /**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = AppDroid.getInstance().getPackageManager();
            PackageInfo info = manager.getPackageInfo(AppDroid.getInstance().getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }
    }

    public String getVersionCode(){
        PackageManager packageManager=AppDroid.getInstance().getPackageManager();
        PackageInfo packageInfo;
        String versionCode="";
        try {
            packageInfo=packageManager.getPackageInfo(AppDroid.getInstance().getPackageName(),0);
            versionCode=packageInfo.versionCode+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}

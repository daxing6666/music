package com.music.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import com.music.AppDroid;
import java.util.List;

/**
 *
 * 网络判断
 */
public class NetworkUtils {

    private final static NetworkUtils instance = new NetworkUtils();

    /**
     * 单例对象实例
     */
    public static NetworkUtils getInstance(){
        return instance;
    }

    /**
     * 是否可有网络连接
     *
     * @return
     */
    public boolean isNetworkConnected(){

        ConnectivityManager mConnectivityManager = (ConnectivityManager) AppDroid.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null)
        {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * Gps是否打开
     * @return
     */
    public boolean isGpsEnabled() {
        LocationManager locationManager = ((LocationManager)  AppDroid.getInstance()
                .getSystemService(Context.LOCATION_SERVICE));
        List<String> accessibleProviders = locationManager.getProviders(true);
        return accessibleProviders != null && accessibleProviders.size() > 0;
    }

    /**
     * wifi是否打开
     */
    public boolean isWifiEnabled() {
        ConnectivityManager mgrConn = (ConnectivityManager) AppDroid.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager)  AppDroid.getInstance()
                .getSystemService(Context.TELEPHONY_SERVICE);
        return ((mgrConn.getActiveNetworkInfo() != null && mgrConn
                .getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel
                .getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    /**
     * 判断当前网络是否是wifi网络
     * if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE) {
     * @return boolean
     */
    public boolean isWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) AppDroid.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 判断当前网络是否3G网络
     * @return boolean
     */
    public boolean is3G() {
        ConnectivityManager connectivityManager = (ConnectivityManager) AppDroid.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }
}
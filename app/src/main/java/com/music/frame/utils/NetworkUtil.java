package com.music.frame.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	public static boolean isWiFi(Context context) {
		NetworkInfo info = getActiveNetworkInfo(context);
		return info != null && info.isAvailable() && info.getType() == ConnectivityManager.TYPE_WIFI;
	}

	private static NetworkInfo getActiveNetworkInfo(Context context) {
		return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	}
}
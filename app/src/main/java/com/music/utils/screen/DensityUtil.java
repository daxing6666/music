package com.music.utils.screen;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.music.AppDroid;

/**
 * [description about this class]
 *
 * @author jack
 */

public class DensityUtil
{
	// 根据屏幕密度转换
	private static float mPixels = 0.0F;
	private static float density = -1.0F;
	private final static DensityUtil instance = new DensityUtil();

	/**
	 * 单例对象实例
	 */
	public static DensityUtil getInstance(){
		return instance;
	}


	/**将像素转换为对应设备的density*/
	public int pixelsToDp(int pixels) {
		float scale = AppDroid.getInstance().getResources().getDisplayMetrics().density;
		return (int) ((pixels - 0.5f) / scale);
	}

	public float getDensity(){
		return AppDroid.getInstance().getResources().getDisplayMetrics().density;
	}

	/**将dp值转换为对应的像素值*/
	public int dpToPx(float dp){
		float scale = AppDroid.getInstance().getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	/**将sp值转换为对应的像素值，主要用于TextView的字体中*/
	public int spToPx(float sp){
		 return (int) (sp * AppDroid.getInstance().getResources().getDisplayMetrics().scaledDensity);
	}

	/**将像素值值转换为对应的sp值，主要用于TextView的字体中*/
	public int pxToSp(int pixels){
		return (int) (pixels / AppDroid.getInstance().getResources().getDisplayMetrics().scaledDensity);
	}

	public int getScreenWidthInPx(){
		return  AppDroid.getInstance().getResources().getDisplayMetrics().widthPixels;
	}

	public int getScreenHeightInPx(){
		return  AppDroid.getInstance().getResources().getDisplayMetrics().heightPixels;
	}

	public int getDisplayMetrics(float pixels) {
		if (mPixels == 0.0F) {
			mPixels = AppDroid.getInstance().getResources().getDisplayMetrics().density;
		}
		return (int) (0.5F + pixels * mPixels);
	}
	public int getImageWeidth(Context context , float pixels) {
		return context.getResources().getDisplayMetrics().widthPixels - 66 - getDisplayMetrics(pixels);
	}

	public int fromDPToPix(int dp) {
		return Math.round(getDensity() * dp);
	}

	public int round(int paramInt) {
		return Math.round(paramInt / getDensity());
	}

	public int getMetricsDensity(float height) {
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		((WindowManager) AppDroid.getInstance().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(localDisplayMetrics);
		return Math.round(height * localDisplayMetrics.densityDpi / 160.0F);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 *
	 * @param spValue
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public int sp2px(float spValue) {
		final float fontScale = AppDroid.getInstance().getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}

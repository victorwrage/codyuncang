/** 
 * @Filename VToast.java 
 * @Description TODO 
 * @Version 1.0
 * @Author xiaoyl
 * @Creation 2013-8-10 下午2:46:56   
 * @Copyright Copyright © 2009 - 2013 Victor.All Rights Reserved.
**/
package com.zdv.codyuncang.utils;

import android.content.Context;
import android.widget.Toast;

/** 
 * @ClassName VToast 
 * @Description TODO 吐司提醒，同一提示在3秒内只出现一次
 * @Version 1.0
 * @Creation 2013-8-10 下午2:46:56 
 * @Mender xiaoyl
 * @Modification 2013-8-10 下午2:46:56 
 **/
public class VToast {
	private static String message_last = "";
	private static long elapse_time = 0;
	private static Toast toast;
	/**
	 * 提示
	 * 
	 * @param context,String
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static void toast(Context context, String message) {
		if(message_last ==null){
			message_last= "";
		}
		if (message_last.equals(message)
				&& (System.currentTimeMillis() - elapse_time) < 2000) {
			return;
		}
		message_last = message;
		elapse_time = System.currentTimeMillis();
		elapse_time = System.currentTimeMillis();
		if(toast ==null) {
			toast = new Toast(context);
		}
		toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	public static void cancel(){
        if(toast!=null){
			toast.cancel();
		}
	}
	/**
	 * 提示
	 * 
	 * @param context ,res
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static void toast(Context context, int res) {
		String message = context.getResources().getString(res);
		if (message_last.equals(message)
				&& (System.currentTimeMillis() - elapse_time) < 2000) {
			return;
		}
		message_last = message;
		elapse_time = System.currentTimeMillis();
		elapse_time = System.currentTimeMillis();
		if(toast ==null) {
			toast = new Toast(context);
		}
		toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}

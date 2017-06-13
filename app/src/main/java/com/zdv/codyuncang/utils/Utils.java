package com.zdv.codyuncang.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.math.BigDecimal;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/13 17:21
 */

public class Utils {
    private static Utils instance= null;
    private Utils(){

    }

    public static synchronized Utils getInstance(){

        if(instance == null){
            synchronized (Utils.class) {
                if(instance == null) {
                    return new Utils();
                }
            }
        }
        return instance;
    }

    //两个Double数相减
    public Double sub(Double v1, Double v2){

       BigDecimal b1 = new BigDecimal(v1.toString());
       BigDecimal b2 = new BigDecimal(v2.toString());

       return b1.subtract(b2).doubleValue();

   }

    /**
     * 判断金额的格式，必须精确到小数点后两位
     */
    public String verify(String val) {

        int p = val.indexOf(".") ;
        int l = val.length();
        if (  !val.equals(".")&& (val != null)
                && (!val.isEmpty())
                && (Float.valueOf(val) != 0)) {
            StringBuilder sb = new StringBuilder();
            for(char s:val.toCharArray()){
                sb.append(s);
            }
            if(p!=-1) {
                switch (l - p) {

                    case 1:
                        sb.append('0').append('0');
                        break;
                    case 2:
                        sb.append('0');
                        break;
                    default:
                        break;
                }
            }else{
                sb.append('.').append('0').append('0');
            }
            val = sb.toString();


        } else {
            val = "0.00";
            //   VToast.toast(context, "请输入的大于0的金额 !");
        }
        return val;
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}

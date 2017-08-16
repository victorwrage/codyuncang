package com.zdv.codyuncang.utils;


import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.pos.api.Scan;
import com.socks.library.KLog;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Hello_world on 2017/6/14.
 */

public class D2000V1ScanInitUtils {
    private Activity activity;
    private Scan scan;
    private int iRet = -1;
    private int iRetRead = -1;
    private Handler handler;
    static D2000V1ScanInitUtils instance;
    private Executor executor;

    public Boolean getStart() {
        return isStart;
    }

    public void setStart(Boolean start) {
        isStart = start;
    }

    private Boolean flag = false;
    private Boolean isStart = true;

    private D2000V1ScanInitUtils(Activity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
        executor = Executors.newSingleThreadScheduledExecutor();
        scan = new Scan(activity, new Scan.ScanConnectStatusListener() {
            @Override
            public void onConnectResult(boolean bRet) {
                if (bRet == true) {
                    isStart = true;
                    iRet = scan.DLL_ScanOpen();
                }
            }
        });
    }

    public static D2000V1ScanInitUtils getInstance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    public static D2000V1ScanInitUtils getInstance(Activity activity, Handler handler) {
        if (instance == null) {
            synchronized (D2000V1ScanInitUtils.class) {
                if (instance == null) {
                    instance = new D2000V1ScanInitUtils(activity, handler);
                }
            }
        }
        return instance;
    }

    public void setScanState() {
        flag = false;
        if (null != scan) {
            int re = scan.DLL_ScanClose();
            KLog.v("setScanState" + re);
        }
    }

    public void open() {
        iRet = scan.DLL_ScanOpen();
    }

    public Boolean isOpen() {
        return flag;
    }

    public void d2000V1ScanOpen() {
        if (flag) {
            return;
        }
        flag = true;
        KLog.v("D2000V1ScanInitUtils--d2000V1ScanOpen");
        executor.execute(() -> {
            String data[] = new String[100];
            while (flag) {
                if (iRet == 0) {
                    iRetRead = scan.DLL_ScanRead(data);
                    if (iRetRead > 0) {

                        Message message = handler.obtainMessage();
                        message.what = 0x06;
                        message.obj = data[0].trim();
                        handler.sendMessage(message);
                        flag = false;
                    }
                }
            }
            isStart = false;
            scan.DLL_ScanClose();
        });
    }


    public void close() {
        isStart = false;
        flag = false;
        if (null != scan) scan.DLL_ScanClose();
        if (null != scan) scan.DLL_ScanRelease();
    }

    public void close2() {
        //  isStart = false;
        flag = false;
        //  if (null != scan) scan.DLL_ScanClose();

    }


}

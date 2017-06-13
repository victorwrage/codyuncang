package com.zdv.codyuncang.service;

import android.app.IntentService;
import android.content.Intent;
import android.device.PrinterManager;

public class PrintBillService extends IntentService {

    private PrinterManager printer;

    public PrintBillService() {
        super("bill");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        printer = new PrinterManager();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        String context = intent.getStringExtra("SPRT");
        if (context == null || context.equals("")) return;

        printer.setupPage(384, -1);
        int  ret = printer.drawTextEx(context, 5, 0, 384, -1, "arial", 24, 0, 0, 0);
      //  printer.prn_paperBack(50);
       // printer.drawLine(5,0,384,0,384);
       // printer.drawText("POS支付凭证",100,0,"arial",45,true,false,0);
        android.util.Log.i("debug", "ret:" + ret);
       // int  ret;
        /*printer.drawText("备注:",5,0,"arial",24,false,false,0);
        printer.drawText("您的24H商户贴身管家",5,0,"arial",24,false,false,0);
        printer.drawText("微信关注 百宝平台(baibao)",5,0,"arial",24,false,false,0);
        printer.drawText(" 中大威客服热线升级为：8008208820",5,0,"arial",24,false,false,0);
        printer.drawText("",5,0,"arial",24,false,false,0);
        printer.drawText("签名：:",200,0,"arial",24,false,false,0);*/


      //  android.util.Log.i("debug", "ret:" + ret);
        //sleep(5000);
        ret = printer.printPage(0);

        Intent i = new Intent("android.print.message");
        i.putExtra("ret", ret);
        this.sendBroadcast(i);
    }

    private void sleep(int second) {
        //延时1秒
        try {
            Thread.currentThread();
            Thread.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
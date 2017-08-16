package com.zdv.codyuncang;


import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.pos.api.Printer;
import com.socks.library.KLog;
import com.zdv.codyuncang.bean.CheckPayInfo;
import com.zdv.codyuncang.fragment.FragmentMain;
import com.zdv.codyuncang.fragment.FragmentPay;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.D2000V1ScanInitUtils;
import com.zdv.codyuncang.utils.VToast;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity implements FragmentMain.IMainListener, FragmentPay.IPayListener {
    private static final int RECORD_PROMPT_MSG = 0x06;
    private static final int SCAN_CLOSED = 1020;

    private static final int TRADE_STATE_TYPE_ALIPAY = 0;//  支付宝类型
    private static final int TRADE_STATE_TYPE_WXPAY = TRADE_STATE_TYPE_ALIPAY + 1;// 微信类型
    private static final int TRADE_STATE_TYPE_CARDPAY = TRADE_STATE_TYPE_WXPAY + 1;// 刷卡类型
    private static final int TRADE_STATE_TYPE_CASHPAY = TRADE_STATE_TYPE_CARDPAY + 1;// 现金类型

    private Printer printer;
    IntentFilter filter;

    FragmentMain fragment0;
    FragmentPay fragment1;
    private int cur_page = 0;
    private String merchant_name;
    String termianl, card, tenant;
    D2000V1ScanInitUtils d2000V1ScanInitUtils;
    Boolean isInit = false;
    String TRACE_NUM = "";
    String PATCH_NUM = "";
    private Executor executor;

    private void sendData(String obj) {
        KLog.v("sendData" + obj);

        switch (cur_page) {
            case 0:
                fragment0.fetchScanResult(obj.trim());
                break;
            case 1:
                fragment1.newPay(obj.trim());
                break;
            default:
                VToast.toast(context, "没有处理");
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragment1 != null && fragment1.isVisible()) {
            gotoMain(0);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Exit();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        executor = Executors.newSingleThreadScheduledExecutor();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        fragment0 = new FragmentMain();
        ft.add(R.id.fragment_container, fragment0, PAGE_0);
        ft.show(fragment0);
        ft.commit();
    }

    @Override
    protected void onResume() {
        KLog.v("onResume");
        super.onResume();
        if (filter == null) {
            filter = new IntentFilter();
            filter.addAction("android.print.message");
            filter.addAction("android.intent.action.D2000PayAmountBroadcastReceiver");
            registerReceiver(mPrtReceiver, filter);
        }
        if (card != null) return;
        if (!isInit) {
            isInit = true;
        } else {
            KLog.v("请稍后");
            showWaitDialog("请稍后");
            promptHandler.postDelayed(() -> hideWaitDialog(), 5000);
        }
        KLog.v("onResume" + d2000V1ScanInitUtils.getStart());
        executor.execute(() -> startScan());
        merchant_name = Constant.cookie.get("user_name");
    }

    @Override
    protected void onStop() {
        KLog.v("onStop");
        super.onStop();
        if (d2000V1ScanInitUtils.getStart()) {
            d2000V1ScanInitUtils.setStart(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        d2000V1ScanInitUtils = D2000V1ScanInitUtils.getInstance(MainActivity.this, promptHandler);
    }


    @Override
    protected void onDestroy() {
        if (null != printer) printer.DLL_PrnRelease();
        d2000V1ScanInitUtils.close();
        unregisterReceiver(mPrtReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mPrtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //   VToast.toast(context,"支付返回"+intent.getAction());
            if (intent.getAction().equals("android.intent.action.D2000PayAmountBroadcastReceiver")) {
                KLog.v(intent.getStringExtra("bn") + "---" + intent.getStringExtra("pc") + intent.getStringExtra("result")
                        + "card=" + intent.getStringExtra("card") + "termianl" + intent.getStringExtra("termianl") + "tenant" + intent.getStringExtra("tenant"));
                if (intent.getStringExtra("result").equals("success")) {
                    //    VToast.toast(context,"支付成功");

                    TRACE_NUM = intent.getStringExtra("bn") == null ? "" : intent.getStringExtra("bn");
                    PATCH_NUM = intent.getStringExtra("pc") == null ? "" : intent.getStringExtra("pc");
                    String BANK_NUM = card = intent.getStringExtra("card") == null ? "" : intent.getStringExtra("card");
                    termianl = intent.getStringExtra("termianl") == null ? "" : intent.getStringExtra("termianl");
                    tenant = intent.getStringExtra("tenant") == null ? "" : intent.getStringExtra("tenant");
                    promptHandler.postDelayed(() -> fragment1.SynchronizePay(BANK_NUM, PATCH_NUM, TRACE_NUM), 4000);
                } else {
                    VToast.toast(context, "支付失败");
                    startScan();
                }
                return;
            }
        }
    };

    private Handler promptHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_PROMPT_MSG:
                    sendData((String) msg.obj);
                    break;
                case SCAN_CLOSED:
                    if (fragment1 != null) fragment1.print();
                    break;
                default:
                    break;
            }
        }
    };

    private void startScan() {
        if (!d2000V1ScanInitUtils.getStart()) {
            d2000V1ScanInitUtils.open();
        }
        d2000V1ScanInitUtils.d2000V1ScanOpen();
    }


    private void Exit() {
        showDialog(EXIT_CONFIRM, "提示", "是否退出?", "确认", "取消");
    }

    @Override
    protected void confirm(int type, DialogInterface dialog) {
        super.confirm(type, dialog);
        switch (type) {
            case EXIT_CONFIRM:
                System.exit(0);
                break;
        }
    }


    @Override
    public void finishMain() {
        Exit();
    }

    @Override
    public void gotoPay(HashMap<String, String> intent) {
        KLog.v("gotoPay" + Constant.cookie.get("user_name"));
        cur_page = 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment1 == null) {
            fragment1 = new FragmentPay();
            ft.add(R.id.fragment_container, fragment1, PAGE_1);
        }
        fragment1.initState(intent);
        ft.show(fragment1);
        ft.hide(fragment0);
        ft.commit();
    }

    public void gotoYLpay(String order) {
        d2000V1ScanInitUtils.setScanState();
        try {
            String pn = "com.qhw.swishcardapp";
            String an = "com.qhw.swishcardapp.activity.LoginActivity";
            Intent intent = new Intent();
            ComponentName component = new ComponentName(pn, an);
            intent.setComponent(component);
            intent.setFlags(101);
            intent.putExtra("money", order); //此处传入出库订单编号
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.fillInStackTrace();
            showWaitDialog("请稍后");
            promptHandler.postDelayed(() -> {
                scan();
                hideWaitDialog();
            }, 2000);
            VToast.toast(context, "没有安装银联支付模块");
        }
    }

    @Override
    public void gotoSynergy(String order_no) {

    }

    @Override
    public void scan() {
        startScan();
    }

    @Override
    public void startOpenDevice() {
        d2000V1ScanInitUtils.open();
    }

    private void doPrint(int cur_pay_type, HashMap<String, String> order_info, CheckPayInfo checkPayInfo, String BANK_NUM) {
        printer = new Printer(this, bRet -> executor.execute(() ->
        {
            int iRet = -1;
            iRet = printer.DLL_PrnInit();
            KLog.v("setScanState" + iRet);
            if (iRet == 0) {
                printStr(cur_pay_type, order_info, checkPayInfo, BANK_NUM);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printStr(cur_pay_type, order_info, checkPayInfo, BANK_NUM);

            } else {
                VToast.toast(context, "打印错误");
            }

        }));
        showWaitDialog("请等待打印完成");
        promptHandler.postDelayed(() -> {
            hideWaitDialog();
            gotoMain(1);
        }, 15000);
    }

    public void closeScanThenPrint() {
        new Thread(() -> d2000V1ScanInitUtils.setScanState()).start();
        showWaitDialog("请稍等");
        new Thread(() -> {
            hideWaitDialog();
            promptHandler.sendEmptyMessageDelayed(SCAN_CLOSED, 2000);
        }).start();
    }


    private void printStr(int cur_pay_type, HashMap<String, String> order_info, CheckPayInfo checkPayInfo, String BANK_NUM) {
        String pay_tp = "";
        KLog.v("printStr" + cur_pay_type);
        Bitmap bitmap = readBitMap(R.drawable.print_icon);
        switch (cur_pay_type) {
            case TRADE_STATE_TYPE_CARDPAY:
                pay_tp = "银联支付";
                break;
            case TRADE_STATE_TYPE_CASHPAY:
                pay_tp = "现金支付";
                break;
            case TRADE_STATE_TYPE_WXPAY:
                pay_tp = "微信支付";
                break;
            case TRADE_STATE_TYPE_ALIPAY:
                pay_tp = "支付宝支付";
                break;
        }

        printer.DLL_PrnBmp(bitmap);
        /*printer.DLL_PrnSetFont((byte)16, (byte)16, (byte)0x33);
        printer.DLL_PrnStr("    云仓POS支付\n");*/
        printer.DLL_PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
        printer.DLL_PrnStr("-------------------------------\n");
        printer.DLL_PrnStr("商户名:" + order_info.get("customer_name") + "\n");
        printer.DLL_PrnStr("商户编号:" + order_info.get("customer_num") + "\n");
        printer.DLL_PrnStr("-------------------------------\n");
        printer.DLL_PrnStr("订单号:" + order_info.get("order_no") + "\n");
        printer.DLL_PrnStr("支付类型:" + pay_tp + "\n");
        printer.DLL_PrnStr("支付渠道:" + merchant_name + "\n");
        printer.DLL_PrnStr("机器序号:" + order_info.get("seri_no") + "\n");
        if (order_info.get("buyer_logon_id") != null && !order_info.get("buyer_logon_id").equals("")) {
            printer.DLL_PrnStr("支付账号:" + order_info.get("buyer_logon_id") + "\n");
        }
        if (order_info.get("buyer_logon_id") != null && !order_info.get("buyer_logon_id").equals("")) {
            printer.DLL_PrnStr("支付凭证:" + checkPayInfo.getAuth_code() + "\n");
        }
        if (checkPayInfo.getOrder_no() != null && !checkPayInfo.getOrder_no().equals("")) {
            printer.DLL_PrnStr("支付单号:" + checkPayInfo.getOrder_no() + "\n");
        }
        if (TRACE_NUM != null && !TRACE_NUM.equals("")) {
            printer.DLL_PrnStr("流水号:" + TRACE_NUM + "\n");
        }
        if (PATCH_NUM != null && !PATCH_NUM.equals("")) {
            printer.DLL_PrnStr("批次号:" + PATCH_NUM + "\n");
        }
        if (card != null) {
            printer.DLL_PrnStr("银行卡号:" + util.getIDCardEncrypt(card) + "\n");
        }
        if (termianl != null) {
            printer.DLL_PrnStr("终端号:" + termianl + "\n");
        }
        if (tenant != null) {
            printer.DLL_PrnStr("门店号:" + tenant + "\n");
        }
        printer.DLL_PrnStr("-------------------------------\n");
        printer.DLL_PrnStr("支付日期:" + currentDate("yyyyMMdd HH:mm:ss") + "\n");
        printer.DLL_PrnStr("总计(Total): RMB" + order_info.get("cash_cur") + "\n");
        printer.DLL_PrnStr("  \n");
        printer.DLL_PrnSetFont((byte) 16, (byte) 16, (byte) 0x00);
        printer.DLL_PrnStr("备注:" + "  \n");
        printer.DLL_PrnStr("  \n");
        printer.DLL_PrnStr("          签名：" + "  \n");
        Bitmap bitmap2 = readBitMap(R.drawable.blank);
        printer.DLL_PrnBmp(bitmap2);
        printer.DLL_PrnStr("-------------------------------\n");
        printer.DLL_PrnStart();
    }

    public Bitmap readBitMap(int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    private void toMain() {
        cur_page = 0;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment0 == null) {
            fragment0 = new FragmentMain();
            ft.add(R.id.fragment_container, fragment0, PAGE_0);
        }
        ft.show(fragment0);
        ft.hide(fragment1);
        ft.commit();
    }

    @Override
    public void gotoMain(int state) {
        toMain();
        switch (state) {
            case 0:
                break;
            case 1:
                card = null;
                termianl = null;
                tenant = null;
                TRACE_NUM = null;
                PATCH_NUM = null;
                fragment0.initState();
                break;
        }
    }

    @Override
    public void print(int cur_pay_type, HashMap<String, String> order_info, CheckPayInfo checkPayInfo, String BANK_NUM) {
        promptHandler.postDelayed(() -> doPrint(cur_pay_type, order_info, checkPayInfo, BANK_NUM), 1000);
    }
}

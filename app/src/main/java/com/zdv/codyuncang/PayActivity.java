package com.zdv.codyuncang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.socks.library.KLog;
import com.zdv.codyuncang.bean.CheckPayInfo;
import com.zdv.codyuncang.bean.PayInfo;
import com.zdv.codyuncang.bean.SynergyMallResponse;
import com.zdv.codyuncang.bean.SynergyPayBack;
import com.zdv.codyuncang.bean.SynergyPayBackResult;
import com.zdv.codyuncang.bean.xml_check_info_root;
import com.zdv.codyuncang.bean.xml_pay_info_root;
import com.zdv.codyuncang.service.PrintBillService;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.VToast;
import com.zdv.codyuncang.view.IPayView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PayActivity extends BaseActivity implements IPayView {
    private static final int TRADE_STATE_TYPE_ALIPAY = 0;//  支付宝类型
    private static final int TRADE_STATE_TYPE_WXPAY = TRADE_STATE_TYPE_ALIPAY + 1;// 微信类型
    private static final int TRADE_STATE_TYPE_CARDPAY = TRADE_STATE_TYPE_WXPAY + 1;// 刷卡类型
    private static final int TRADE_STATE_TYPE_CASHPAY = TRADE_STATE_TYPE_CARDPAY + 1;// 现金类型

    private static final int CUSTOMER_PAY = 0;
    private static final int CUSTOMER_CHECK_PAY = CUSTOMER_PAY + 1;
    private static final int SYNCHORIZE_SYNERGY_PAY = CUSTOMER_CHECK_PAY + 1;
    private static final int PRINT_ERROR = SYNCHORIZE_SYNERGY_PAY + 1;

    private static final int SEND_PAY_SUCCESS = SYNCHORIZE_SYNERGY_PAY + 1;//提交支付
    private static final int SEND_PAY_FAIL = SEND_PAY_SUCCESS + 1;
    private static final int STATE_PAY_SUCCESS = SEND_PAY_FAIL + 1;//获取支付状态
    private static final int STATE_PAY_FAIL = STATE_PAY_SUCCESS + 1;
    private static final int SYNCHRONIZE_PAY_SUCCESS = STATE_PAY_FAIL + 1;//同步到星利源
    private static final int SYNCHRONIZE_PAY_FAIL = SYNCHRONIZE_PAY_SUCCESS + 1;
    private static final int MALL_STATUS_SUCCESS = SYNCHRONIZE_PAY_FAIL + 1;//同步到星利源商城
    private static final int MALL_STATUS_FAIL = MALL_STATUS_SUCCESS + 1;

    private int cur_pay_type = -1;
    @Bind(R.id.unionpay_layout)
    LinearLayout unionpay_layout;
    @Bind(R.id.alipay_layout)
    LinearLayout alipay_layout;
    @Bind(R.id.wxpay_layout)
    LinearLayout wxpay_layout;
    @Bind(R.id.cashpay_layout)
    LinearLayout cashpay_layout;
    @Bind(R.id.pay_back)
    TextView pay_back;
    @Bind(R.id.pay_count_tv)
    TextView pay_count_tv;
    HashMap<String, String> order_info;
    PayInfo payInfo;
    CheckPayInfo checkPayInfo;
    private int printCount = 0;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_PAY_SUCCESS:

                    break;
                case SEND_PAY_FAIL:
                    CheckPay();
                    break;
                case STATE_PAY_SUCCESS:
                    KLog.v("支付成功");
                    VToast.toast(context, "支付成功");
                    SynchronizePay();
                    break;
                case STATE_PAY_FAIL:
                    KLog.v("支付失败");
                    VToast.toast(context, (String) msg.obj);
                    if (msg.obj.equals("需要用户输入支付密码")) {
                        showDialog(SYNCHORIZE_SYNERGY_PAY, "提示", "可能需要用户输入密码,请查看客户是否支付成功？点击'是'将回传支付成功信息，点击'否'退回支付界面", " 是", "否");
                    } else {
                        showDialog(SYNCHORIZE_SYNERGY_PAY, "提示", "出错了,请查看客户是否支付成功？点击'是'将回传支付成功信息，点击'否'退回支付界面", " 是", "否");
                    }

                    break;
                case SYNCHRONIZE_PAY_SUCCESS:
                    VToast.toast(context, "支付信息同步成功");
                    //  present.initRetrofit(Constant.URL_SYNERGY_MALL,false);
                    //  present.SendToMall(order_info.get("order_no"));
                    print();

                    break;
                case SYNCHRONIZE_PAY_FAIL:
                    showDialog(SYNCHORIZE_SYNERGY_PAY, "提示", "同步支付信息失败", "重试", null);
                    break;
                case MALL_STATUS_SUCCESS:
                    break;
                case MALL_STATUS_FAIL:
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(PayActivity.this, R.layout.activity_pay, null);
        setContentView(view);
        ButterKnife.bind(PayActivity.this, view);
        initDate(getIntent());
        initView();
    }

    private void initDate(Intent intent) {
        checkPayInfo = new CheckPayInfo();
        payInfo = new PayInfo();
        order_info = new HashMap<>();
        order_info.put("price", intent.getStringExtra("price"));
        order_info.put("cash", intent.getStringExtra("cash"));
        order_info.put("cash_for", intent.getStringExtra("cash_for"));
        order_info.put("order_no", intent.getStringExtra("order_no"));
        order_info.put("cash_re", intent.getStringExtra("cash_re"));
        order_info.put("customer_name", intent.getStringExtra("customer_name"));
        order_info.put("customer_add", intent.getStringExtra("customer_add"));
        order_info.put("customer_tel", intent.getStringExtra("customer_tel"));
        order_info.put("customer_num", intent.getStringExtra("customer_num"));

       /* order_info.put("price", "0.01");
        order_info.put("cash", "0.01");
        order_info.put("cash_for", "0.01");
        order_info.put("order_no", "000000000");
        order_info.put("cash_re", "0.01");
        order_info.put("customer_name", "test");
        order_info.put("customer_add", "test");
        order_info.put("customer_tel", "13000000");
        order_info.put("customer_num", "000001");
        pay_count_tv.setText("0.01");*/

        pay_count_tv.setText(intent.getStringExtra("cash_for"));

        present.setView(PayActivity.this);
    }

    private void initView() {
        RxView.clicks(unionpay_layout).subscribe(s -> CardPay());
        RxView.clicks(cashpay_layout).subscribe(s -> CashPay());
        RxView.clicks(alipay_layout).subscribe(s -> ScanQRcode(TRADE_STATE_TYPE_ALIPAY));
        RxView.clicks(wxpay_layout).subscribe(s -> ScanQRcode(TRADE_STATE_TYPE_WXPAY));
        RxView.clicks(pay_back).subscribe(s -> finish());
    }

    private void ScanQRcode(int type) {
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "貌似没有网络");
            return;
        }
        cur_pay_type = type;
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * 银联支付
     */
    private void CardPay() {
        print();
        cur_pay_type = TRADE_STATE_TYPE_CASHPAY;
        VToast.toast(context, "暂未开通");
    }

    /**
     * 现金支付
     */
    private void CashPay() {
        showWaitDialog("正在回传信息");
        cur_pay_type = TRADE_STATE_TYPE_CASHPAY;
        showDialog(SYNCHORIZE_SYNERGY_PAY, "提示", "商户是否已经现金付款？点击'是'将回传支付成功信息，点击'否'退回支付界面", "是", "否");
    }

    /**
     * 支付
     */
    private void Pay() {
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "貌似没有网络");
            return;
        }

        present.initRetrofit(Constant.URL_BAIBAO, true);
        showWaitDialog("正在支付");
        switch (cur_pay_type) {
            case TRADE_STATE_TYPE_ALIPAY:
                present.Pay("zfb", payInfo);
                break;
            case TRADE_STATE_TYPE_WXPAY:
                present.Pay("weixin", payInfo);
                break;
        }
    }

    /**
     * 检查支付状态
     */
    private void CheckPay() {
        showWaitDialog("正在查询支付结果");
        present.initRetrofit(Constant.URL_BAIBAO, true);
        switch (cur_pay_type) {
            case TRADE_STATE_TYPE_ALIPAY:
                present.CheckPay("checkzfb", checkPayInfo);
                break;
            case TRADE_STATE_TYPE_WXPAY:
                present.CheckPay("checkwx", checkPayInfo);
                break;
        }
    }

    /**
     * 同步支付状态
     */
    private void SynchronizePay() {
        KLog.v("开始同步");
        showWaitDialog("正在回传信息");


        SynergyPayBack synergyPayBackItem = new SynergyPayBack();
        ArrayList<SynergyPayBack.ItemInfo> temp = new ArrayList<>();
        SynergyPayBack.ItemInfo synergyPayBack = synergyPayBackItem.new ItemInfo();
        synergyPayBack.setKunnr(order_info.get("customer_num"));
        synergyPayBack.setVbeln(order_info.get("order_no"));
        synergyPayBack.setBank_num("");
        synergyPayBack.setPos_num(deviceManager.getDeviceId());
        synergyPayBack.setPay_person(Constant.cookie.get("user_name"));
        synergyPayBack.setPay_day(currentDate("yyyyMMdd"));
        synergyPayBack.setPay_time(currentDate("HHmmss"));
        synergyPayBack.setName1(order_info.get("customer_name"));
        synergyPayBack.setDmbtr(order_info.get("cash"));

        synergyPayBack.setPay_money(order_info.get("cash_for"));
        synergyPayBack.setRe_money(order_info.get("cash_re"));
        switch (cur_pay_type) {
            case TRADE_STATE_TYPE_CARDPAY:
                synergyPayBack.setPay_mode("YL");
                break;
            case TRADE_STATE_TYPE_CASHPAY:
                synergyPayBack.setPay_mode("XJ");
                break;
            case TRADE_STATE_TYPE_WXPAY:
                synergyPayBack.setPay_mode("WX");
                break;
            case TRADE_STATE_TYPE_ALIPAY:
                synergyPayBack.setPay_mode("ZFB");
                break;
        }
        KLog.v(synergyPayBack.toString());
        temp.add(synergyPayBack);
        synergyPayBackItem.setZpos_pay(temp);
        present.initRetrofit(Constant.URL_SYNERGY, false);
        present.SendPay(synergyPayBackItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.print.message");
        registerReceiver(mPrtReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mPrtReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mPrtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int ret = intent.getIntExtra("ret", 0);
            if (ret == -1) {
                VToast.toast(context, "没有打印纸了");
                showDialog(PRINT_ERROR, "提示", "打印没完成，是否继续", " 是", "否");
            } else {
                if(printCount<2){
                    unionpay_layout.postDelayed(()->print(),5000);
                    return;
                }
                PayActivity.this.setResult(1024);
                finish();
            }
        }
    };

    /**
     * 打印
     */
    private void print() {
        printCount++;
        String CfgStr = "";
        String pay_tp = "";
        switch (cur_pay_type) {
            case TRADE_STATE_TYPE_CARDPAY:
                pay_tp = "刷卡支付";
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
        CfgStr = "\r\n                    POS支付"
                + "\r\n商户名:" + order_info.get("customer_name")
                + "\r\n商户编号:" + order_info.get("customer_num")
                + "\r\n终端号:" + deviceManager.getDeviceId()
                + "\r\n支付金额:" + order_info.get("cash_for")
                + "\r\n订单号:" + order_info.get("order_no")
                + "\r\n支付类型:" + pay_tp
                + "\r\n支付渠道:" + Constant.cookie.get("user_name");
        if (order_info.get("buyer_logon_id") != null && !order_info.get("buyer_logon_id").equals("")) {
            CfgStr += "\r\n支付账号:";
            CfgStr += order_info.get("buyer_logon_id");
        }
        if (checkPayInfo.getAuth_code() != null && !checkPayInfo.getAuth_code().equals("")) {
            CfgStr += "\r\n支付凭证:";
            CfgStr += checkPayInfo.getAuth_code();
        }
        if (checkPayInfo.getOrder_no() != null && !checkPayInfo.getOrder_no().equals("")) {
            CfgStr += "\r\n支付单号:";
            CfgStr += checkPayInfo.getOrder_no();
        }

        CfgStr += "\r\n支付日期:" + currentDate("yyyyMMdd HH:mm:ss")
                + "\r\n备注:"
                + "\r\n您的24H商户贴身管家:"
                + "\r\n微信关注 百宝平台(baibao) :"
                + "\r\n                           "
                + "\r\n                           "
                + "\r\n                    签名：:"
                + "\r\n                           "
                + "\r\n                           "
                + "\r\n                           ";


        Intent intentService = new Intent(PayActivity.this, PrintBillService.class);
        intentService.putExtra("SPRT", CfgStr);
        startService(intentService);
    }


    @Override
    public void ResolveMallInfo(SynergyMallResponse info) {
        hideWaitDialog();
        if (info != null) {
            handler.sendEmptyMessage(MALL_STATUS_SUCCESS);
        } else {
            handler.sendEmptyMessage(MALL_STATUS_FAIL);
        }

    }

    @Override
    public void ResolvePayInfo(xml_pay_info_root payResultInfo) {
        hideWaitDialog();
        Message msg = new Message();
        checkPayInfo.setOrder_no("");
        if (payResultInfo.xml_data != null) {
            KLog.v(payResultInfo.xml_data.msg + payResultInfo.xml_data.code + payResultInfo.xml_data.result_code + payResultInfo.xml_data.title
                    + payResultInfo.xml_data.order_no);
            if (payResultInfo.xml_data.code != null && payResultInfo.xml_data.code.equals("10000")//支付宝
                    || (payResultInfo.xml_data.result_code != null && payResultInfo.xml_data.result_code.equals("SUCCESS"))) {//微信
                checkPayInfo.setOrder_no(payResultInfo.xml_data.order_no);
                handler.sendEmptyMessage(STATE_PAY_SUCCESS);
            } else {
                msg.obj = payResultInfo.xml_data.msg == null ? "支付失败" : payResultInfo.xml_data.msg;
                msg.what = STATE_PAY_FAIL;
                handler.sendMessage(msg);
            }
        } else {
            msg.obj = "支付失败";
            msg.what = SEND_PAY_FAIL;
            handler.sendMessage(msg);
        }

    }

    @Override
    public void ResolveCheckPayInfo(xml_check_info_root checkPayResultInfo) {
        hideWaitDialog();
        Message msg = new Message();

        if (checkPayResultInfo.xml_data != null) {
            KLog.v(checkPayResultInfo.xml_data.msg + checkPayResultInfo.xml_data.status + checkPayResultInfo.xml_data.buyer_logon_id
                    + checkPayResultInfo.xml_data.pay_time + checkPayResultInfo.xml_data.out_trade_no);
            if (checkPayResultInfo.xml_data.status != null && checkPayResultInfo.xml_data.status.equals("1")) {
                order_info.put("buyer_logon_id", checkPayResultInfo.xml_data.buyer_logon_id);
                msg.what = STATE_PAY_SUCCESS;
                msg.obj = "支付成功";
                handler.sendMessage(msg);
            } else {
                msg.what = STATE_PAY_FAIL;
                msg.obj = checkPayResultInfo.xml_data.msg == null ? "支付失败" : checkPayResultInfo.xml_data.msg;
                handler.sendMessage(msg);
            }
        } else {
            msg.what = STATE_PAY_FAIL;
            msg.obj = "支付失败";
            handler.sendMessage(msg);
        }

    }

    @Override
    public void ResolveSynergyPayInfo(SynergyPayBackResult info) {
        KLog.v(info.toString());
        hideWaitDialog();
        Message msg = new Message();
        if (info.getOm_message() == null) {
            msg.what = SYNCHRONIZE_PAY_FAIL;
            msg.obj = "同步失败";
            handler.sendMessage(msg);
        } else {
            if (info.getEm_type().equals("S")) {
                handler.sendEmptyMessage(SYNCHRONIZE_PAY_SUCCESS);
            } else {
                msg.what = SYNCHRONIZE_PAY_FAIL;
                msg.obj = info.getOm_message();
                handler.sendMessage(msg);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            KLog.v(bundle.getString("result"));

            checkPayInfo.setAuth_code(bundle.getString("result"));
            checkPayInfo.setUsername(Constant.cookie.get("user_name"));
            checkPayInfo.setPassword(Constant.cookie.get("user_pw"));

            payInfo.setUsername(Constant.cookie.get("user_name"));
            payInfo.setPassword(Constant.cookie.get("user_pw"));
            payInfo.setCode(bundle.getString("result"));
            payInfo.setNumscreen(order_info.get("cash_for"));
            Pay();

        } else if (resultCode == RESULT_CANCELED) {
            // VToast.toast(context, "扫码取消");
        } else {
            // VToast.toast(context, "扫码错误");
        }
    }

    @Override
    protected void cancel(int type, DialogInterface dialog) {
        super.cancel(type, dialog);
        hideWaitDialog();
        switch (type) {
            case PRINT_ERROR:
                PayActivity.this.setResult(1024);
                finish();
                break;
        }
    }

    @Override
    protected void confirm(int type, DialogInterface dialog) {
        super.confirm(type, dialog);
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "没有网络连接");
            return;
        }
        switch (type) {
            case CUSTOMER_PAY:
                Pay();
                break;
            case CUSTOMER_CHECK_PAY:
                CheckPay();
                break;
            case SYNCHORIZE_SYNERGY_PAY:
                SynchronizePay();
                break;
            case PRINT_ERROR:
                print();
                break;
        }
    }
}

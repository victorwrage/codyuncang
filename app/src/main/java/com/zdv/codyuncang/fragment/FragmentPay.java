package com.zdv.codyuncang.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.socks.library.KLog;
import com.zdv.codyuncang.R;
import com.zdv.codyuncang.bean.CheckPayInfo;
import com.zdv.codyuncang.bean.PayInfo;
import com.zdv.codyuncang.bean.SynergyMallResponse;
import com.zdv.codyuncang.bean.SynergyPayBack;
import com.zdv.codyuncang.bean.SynergyPayBackResult;
import com.zdv.codyuncang.bean.xml_check_info_root;
import com.zdv.codyuncang.bean.xml_pay_info_root;
import com.zdv.codyuncang.present.QueryPresent;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.Utils;
import com.zdv.codyuncang.utils.VToast;
import com.zdv.codyuncang.view.IPayView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentPay extends BaseFragment  implements IPayView {
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
    private String cash_cur;
    @Bind(R.id.unionpay_layout)
    LinearLayout unionpay_layout;
    @Bind(R.id.alipay_layout)
    LinearLayout alipay_layout;
    @Bind(R.id.wxpay_layout)
    LinearLayout wxpay_layout;
    @Bind(R.id.cashpay_layout)
    LinearLayout cashpay_layout;
    @Bind(R.id.pay_back)
    RelativeLayout pay_back;
    @Bind(R.id.pay_count_tv)
    TextView pay_count_tv;
    HashMap<String, String> order_info;
    PayInfo payInfo;
    Utils utils;
    CheckPayInfo checkPayInfo;

    IPayListener listner;
    Boolean isFetching = false;
    String BANK_NUM = "";
    String PATCH_NUM = "";
    String TRACE_NUM = "";
    View view;
    QueryPresent present;
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
                    //VToast.toast(context, "支付成功");
                    SynchronizePay(BANK_NUM,PATCH_NUM,TRACE_NUM);
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
                    listner.closeScanThenPrint();

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

    public  void print(){
        listner.print(cur_pay_type,order_info,checkPayInfo,BANK_NUM);
    }
    public void newPay(String obj) {
        if (isFetching) {
            showWaitDialog("请稍候");
            wxpay_layout.postDelayed(() -> {
                hideWaitDialog();
                listner.scan();
            }, 2000);
            return;
        }
        if (cur_pay_type != TRADE_STATE_TYPE_ALIPAY && cur_pay_type != TRADE_STATE_TYPE_WXPAY) {
            showWaitDialog("请稍候");
            wxpay_layout.postDelayed(() -> {
                hideWaitDialog();
                listner.scan();
            }, 2000);
            VToast.toast(context, "请选择支付方式");
            return;
        }

        boolean isAvaliable = true;
        switch (cur_pay_type){
            case TRADE_STATE_TYPE_WXPAY:
                if(obj.trim().length() != 18 || !obj.trim().startsWith("1")) {
                    VToast.toast(context, "请扫描正确的微信付款码");
                    isAvaliable = false;
                }
                break;
            case TRADE_STATE_TYPE_ALIPAY:
                if(obj.trim().length() != 18 || !obj.trim().startsWith("2")) {
                    VToast.toast(context, "请扫描正确的支付宝付款码");
                    isAvaliable = false;
                }
                break;
            default:
                VToast.toast(context, "请选择微信|支付宝 其中一种支付方式");
                isAvaliable = false;
                break;
        }

        if(!isAvaliable){
            showWaitDialog("请稍候");
            new Handler().postDelayed(() -> {
                listner.scan();
                hideWaitDialog();
            }, 2000);
            return;
        }

        isFetching = true;

        KLog.v("setAuth_code" + obj);
        String code = obj.trim();
        checkPayInfo.setAuth_code(code);
        checkPayInfo.setUsername(Constant.cookie.get("user_name"));
        checkPayInfo.setPassword(Constant.cookie.get("user_pw"));
        payInfo.setUsername(Constant.cookie.get("user_name"));
        payInfo.setPassword(Constant.cookie.get("user_pw"));
        payInfo.setCode(code);
        payInfo.setNumscreen(order_info.get("cash_cur"));
        KLog.v("setAuth_code" + payInfo.getCode() + code);
        Pay();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_pay, container, false);
        ButterKnife.bind(FragmentPay.this, view);
        initDate();
        initView();
        return view;
    }

    private void initDate() {
        utils = Utils.getInstance();
        present = QueryPresent.getInstance(context);
        present.setView(FragmentPay.this);
    }

    private void initView() {
        RxView.clicks(unionpay_layout).subscribe(s -> CardPay());
        RxView.clicks(cashpay_layout).subscribe(s -> CashPay());
        // RxView.clicks(alipay_layout).subscribe(s -> ScanQRcode(TRADE_STATE_TYPE_ALIPAY));

        RxView.clicks( alipay_layout ).throttleFirst( 2 , TimeUnit.SECONDS ).subscribe(s ->startScan(0)) ;
        //  RxView.clicks(wxpay_layout).subscribe(s -> ScanQRcode(TRADE_STATE_TYPE_WXPAY));
        RxView.clicks( wxpay_layout ).throttleFirst( 2 , TimeUnit.SECONDS ).subscribe(s ->startScan(1)) ;
        RxView.clicks(pay_back).subscribe(s -> listner.gotoMain(0));
    }

    private void startScan(int i) {

        switch (i) {
            case 0:
                cur_pay_type = TRADE_STATE_TYPE_ALIPAY;
                VToast.toast(context,"请扫描支付宝支付二维码");
               // listner.scan();
                break;
            case 1:
                cur_pay_type = TRADE_STATE_TYPE_WXPAY;
                VToast.toast(context,"请扫描微信支付二维码");
               // listner.scan();
                break;
        }
    }

    /**
     * 银联支付
     */
    private void CardPay() {
        cur_pay_type = TRADE_STATE_TYPE_CARDPAY;
        listner.gotoYLpay(order_info.get("cash_cur"));
    }


    /**
     * 现金支付
     */
    private void CashPay() {
        cur_pay_type = TRADE_STATE_TYPE_CASHPAY;
        showDialog(SYNCHORIZE_SYNERGY_PAY, "提示", "商户是否已经现金付款？点击'是'将回传支付成功信息，点击'否'退回支付界面", "是", "否");
    }

    /**
     * 支付
     */
    private void Pay() {
        if (!utils.isNetworkConnected(context)) {
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

    @Override
    public void onResume() {
        super.onResume();
       // cur_pay_type = -1;
    }

    /**
     * 同步支付状态
     */
    public void SynchronizePay(String BANK_NUM,String PATCH_NUM,String TRACE_NUM) {
        KLog.v("SynchronizePay开始同步");
        showWaitDialog("正在回传信息");

        SynergyPayBack synergyPayBackItem = new SynergyPayBack();
        ArrayList<SynergyPayBack.ItemInfo> temp = new ArrayList<>();
        SynergyPayBack.ItemInfo synergyPayBack = synergyPayBackItem.new ItemInfo();
        synergyPayBack.setKunnr(order_info.get("customer_num"));
        synergyPayBack.setVbeln(order_info.get("order_no"));

        synergyPayBack.setPos_num(order_info.get("seri_no"));
        synergyPayBack.setPay_person(Constant.cookie.get("user_name"));
        synergyPayBack.setPay_day(currentDate("yyyyMMdd"));
        synergyPayBack.setPay_time(currentDate("HHmmss"));
        synergyPayBack.setName1(order_info.get("customer_name"));
        synergyPayBack.setDmbtr(order_info.get("cash_for"));

        synergyPayBack.setPay_money(order_info.get("cash_cur"));
        synergyPayBack.setRe_money(order_info.get("cash_re"));

        switch (cur_pay_type) {
            case TRADE_STATE_TYPE_CARDPAY:
                synergyPayBack.setBANK_NUM(BANK_NUM);
                synergyPayBack.setPATCH_NUM(PATCH_NUM);
                synergyPayBack.setPay_mode("YL");
                break;
            case TRADE_STATE_TYPE_CASHPAY:
                synergyPayBack.setBANK_NUM(checkPayInfo.getAuth_code());
                synergyPayBack.setPATCH_NUM(checkPayInfo.getOrder_no());
                synergyPayBack.setPay_mode("XJ");
                break;
            case TRADE_STATE_TYPE_WXPAY:
                synergyPayBack.setBANK_NUM(checkPayInfo.getAuth_code());
                synergyPayBack.setPATCH_NUM(checkPayInfo.getOrder_no());
                synergyPayBack.setPay_mode("WX");
                break;
            case TRADE_STATE_TYPE_ALIPAY:
                synergyPayBack.setBANK_NUM(checkPayInfo.getAuth_code());
                synergyPayBack.setPATCH_NUM(checkPayInfo.getOrder_no());
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pay_count_tv.setText(cash_cur);
    }

    @Override
    public void ResolveSynergyPayInfo(SynergyPayBackResult info) {
        KLog.v(info.toString());
        isFetching = false;
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
    protected void cancel(int type, DialogInterface dialog) {
        super.cancel(type, dialog);
        isFetching = false;
        hideWaitDialog();
        switch (type) {
            case PRINT_ERROR:
                listner.gotoMain(1);
                break;
            case SYNCHORIZE_SYNERGY_PAY:
                if(cur_pay_type != TRADE_STATE_TYPE_CASHPAY) {
                    showWaitDialog("请稍候");
                    cur_pay_type = -1;
                    wxpay_layout.postDelayed(() -> {
                        hideWaitDialog();
                        listner.scan();
                    }, 2000);
                }
                break;
        }
    }

    public void initState(HashMap<String,String> intent){
        utils =  Utils.getInstance();
        cur_pay_type = -1;
        checkPayInfo = new CheckPayInfo();
        payInfo = new PayInfo();
        order_info = new HashMap<>();
        order_info.put("price", intent.get("price"));
        order_info.put("cash", intent.get("cash"));
        order_info.put("cash_for", intent.get("cash_for"));
        order_info.put("cash_cur", intent.get("cash_cur"));
        order_info.put("order_no", intent.get("order_no"));
        order_info.put("cash_re", intent.get("cash_re"));
        order_info.put("customer_name", intent.get("customer_name"));
        order_info.put("customer_add", intent.get("customer_add"));
        order_info.put("customer_tel", intent.get("customer_tel"));
        order_info.put("customer_num", intent.get("customer_num"));
        order_info.put("seri_no", utils.getSerialNumber());
        KLog.v("cur"+intent.get("cash_cur"));
        cash_cur = intent.get("cash_cur");
        if(pay_count_tv!=null){
            pay_count_tv.setText(cash_cur);
        }
    }


    @Override
    protected void confirm(int type, DialogInterface dialog) {
        super.confirm(type, dialog);
        isFetching = false;
        if (!utils.isNetworkConnected(context)) {
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
                SynchronizePay(BANK_NUM,PATCH_NUM,TRACE_NUM);
                break;
            case PRINT_ERROR:
                listner.print(cur_pay_type,order_info, checkPayInfo,BANK_NUM);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listner = (IPayListener) context;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public interface IPayListener {
        void gotoMain(int state);

        void print(int cur_pay_type,HashMap<String, String> order_info, CheckPayInfo checkPayInfo,String BANK_NUM);
        void gotoYLpay(String order);
        void closeScanThenPrint();

        void scan();
    }
}

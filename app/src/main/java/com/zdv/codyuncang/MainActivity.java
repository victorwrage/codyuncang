package com.zdv.codyuncang;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.socks.library.KLog;
import com.zdv.codyuncang.bean.SynergyCustomerOrderInfo;
import com.zdv.codyuncang.bean.SynergyCustomerOrderItemInfo;
import com.zdv.codyuncang.bean.SynergyRequest;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.VToast;
import com.zdv.codyuncang.view.IOrderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements IOrderView {
    private static final int ORDER_FETCH_SUCCESS = 10;//  订单获取成功
    private static final int ORDER_FETCH_FAIL = ORDER_FETCH_SUCCESS + 1;// 订单获取失败
    private static final int ORDER_FETCH_NOT = ORDER_FETCH_FAIL + 1;// 订单没记录

    private static final int EXIT_CONFIRM = 1025;
    private static final int TRY_AGAIN = EXIT_CONFIRM + 1;

    private static final int EDIT_ACT_CASH = 20;// 修改实付金额
    private static final int EDIT_ORDER = 21;// 修改订单号
    private int CUR_EDIT;// 当前操作
    @Bind(R.id.btn_txt0)
    TextView btn_txt0;
    @Bind(R.id.btn_txt1)
    TextView btn_txt1;
    @Bind(R.id.btn_txt2)
    TextView btn_txt2;
    @Bind(R.id.btn_txt3)
    TextView btn_txt3;
    @Bind(R.id.btn_txt4)
    TextView btn_txt4;
    @Bind(R.id.btn_txt5)
    TextView btn_txt5;
    @Bind(R.id.btn_txt6)
    TextView btn_txt6;
    @Bind(R.id.btn_txt7)
    TextView btn_txt7;
    @Bind(R.id.btn_txt8)
    TextView btn_txt8;
    @Bind(R.id.btn_txt9)
    TextView btn_txt9;
    @Bind(R.id.btn_dot)
    TextView btn_dot;
    @Bind(R.id.btn_char_y)
    TextView btn_char_y;
    @Bind(R.id.btn_del)
    TextView btn_del;
    @Bind(R.id.btn_confirm)
    TextView btn_confirm;
    @Bind(R.id.btn_cancel)
    TextView btn_cancel;
    @Bind(R.id.tv_digit)
    TextView tv_digit;
    String TempOrderNum;
    ListView listView;


    List<Map<String, String>> order_list;
    Map<String, String> customer_info;
    SimpleAdapter adapter;

    MyBroadcastReceiver receiver;

    private int cur_postion = -1;
    View popupWindowView;
    private PopupWindow popupWindow;
    private LinearLayout edit_act_cash_lay, bottom_lay;
    private TextView confirm_order_tv, edit_order_tv, text_order_no_tv, text_dec_tv, text_name, text_act_tv, edit_pay_tv, scan_order_tv;
    Double receive_cash = 0.00;
    String order_no;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ORDER_FETCH_SUCCESS:
                    adapter.notifyDataSetChanged();
                    pay();
                    break;
                case ORDER_FETCH_FAIL:
                    showDialog(TRY_AGAIN, "网络错误", "查询订单失败", "重试", "取消");
                    break;
                case ORDER_FETCH_NOT:
                    VToast.toast(context, (String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        initDate();
        initView();
    }

    private void initView() {
        text_dec_tv = (TextView) findViewById(R.id.text_dec_tv);
        text_order_no_tv = (TextView) findViewById(R.id.text_order_no_tv);
        edit_act_cash_lay = (LinearLayout) findViewById(R.id.edit_act_cash_lay);
        bottom_lay = (LinearLayout) findViewById(R.id.bottom_lay);
        text_act_tv = (TextView) findViewById(R.id.text_act_tv);
        text_name = (TextView) findViewById(R.id.text_name_tv);
        confirm_order_tv = (TextView) findViewById(R.id.confirm_order_tv);
        edit_order_tv = (TextView) findViewById(R.id.edit_order_tv);
        scan_order_tv = (TextView) findViewById(R.id.scan_order_tv);
        edit_pay_tv = (TextView) findViewById(R.id.edit_pay_tv);
        scan_order_tv = (TextView) findViewById(R.id.scan_order_tv);
        //  input_order = (TextView) findViewById(R.id.input_order);
        listView = (ListView) findViewById(R.id.listView);
        popupWindowView = getLayoutInflater().inflate(R.layout.pop_password, null);
        ButterKnife.bind(MainActivity.this, popupWindowView);
        bottom_lay.setVisibility(View.GONE);
        RxView.clicks(edit_pay_tv).subscribe(s -> gotoPay());
        //RxView.clicks(scan_lay).subscribe(s -> startScan());
        RxView.clicks(scan_order_tv).subscribe(s -> startScan());


        RxView.clicks(confirm_order_tv).subscribe(s -> gotoSynergy());
        RxView.clicks(edit_order_tv).subscribe(s -> showPopupWindow(EDIT_ORDER));
        RxView.clicks(text_order_no_tv).subscribe(s -> showPopupWindow(EDIT_ORDER));
        RxView.clicks(edit_act_cash_lay).subscribe(s -> showPopupWindow(EDIT_ACT_CASH));

        View header = View.inflate(MainActivity.this, R.layout.listheader, null);
        listView.addHeaderView(header);
        adapter = new SimpleAdapter(MainActivity.this, order_list, R.layout.listheader, new String[]{"order_no", "price", "cash"}, new int[]{R.id.order_no, R.id.price, R.id.cash});
        listView.setAdapter(adapter);
        // listView.setOnItemClickListener((parent, view, position, id) -> itemClick(position));
    }

    private void initDate() {
        present.initRetrofit(Constant.URL_SYNERGY, false);
        present.setView(MainActivity.this);
        order_list = new ArrayList<>();

     /*   receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PayAmountBroadcastReceiver");
        registerReceiver(receiver, filter);*/
    }

    private void Exit() {
        showDialog(EXIT_CONFIRM, "提示", "是否退出?", "确认", "取消");
    }

    private void itemClick(int position) {
        if (position == 0) {
            return;
        }
        cur_postion = position - 1;
        // showPopupWindow();
    }

    /**
     *
     */
    private void startScan() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, 0);
    }


    @Override
    protected void onDestroy() {
       // unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            KLog.v(bundle.getString("result"));

            if (!util.isNetworkConnected(context)) {
                VToast.toast(context, "貌似没有网络");
                return;
            }
            showWaitDialog("正在查询订单号...");
            order_list.clear();
            adapter.notifyDataSetChanged();
            text_dec_tv.setText("0");


            TempOrderNum = bundle.getString("result");
            SynergyRequest synergyRequest = new SynergyRequest();
            synergyRequest.setSCFID(TempOrderNum);

            present.initRetrofit(Constant.URL_SYNERGY,false);
            present.QueryOder(synergyRequest);

        } else if (resultCode == RESULT_CANCELED) {
        } else if (resultCode == 1024) {//已经支付成功

            receive_cash = 0.00;
            order_no = null;
            order_list.clear();
            text_dec_tv.setText("0");
            text_act_tv.setText("0");
            bottom_lay.setVisibility(View.GONE);
            text_name.setText("");
            adapter.notifyDataSetChanged();
        } else {
            Intent intent = new Intent(this, CaptureActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    public void ResolveCustomerOrder(SynergyCustomerOrderInfo info) {
        Message msg = new Message();
        if (info.getName1() == null) {
            msg.what = ORDER_FETCH_FAIL;
            msg.obj = "网络错误";
            handler.sendMessage(msg);
            hideWaitDialog();
            return;
        }
        KLog.v(info.toString());
        if (info.getEm_type() != null && !info.getEm_type().equals("S")) {
            msg.what = ORDER_FETCH_NOT;
            msg.obj = info.getOm_message();
            handler.sendMessage(msg);
            hideWaitDialog();
            return;
        }

        bottom_lay.setVisibility(View.VISIBLE);
        text_order_no_tv.setText(TempOrderNum);
        order_no = text_order_no_tv.getText().toString();

        KLog.v(info.getName1());
        customer_info = new HashMap<>();
        customer_info.put("customer_name", info.getName1());
        customer_info.put("customer_add", info.getStr_suppl());
        customer_info.put("customer_contact", info.getNamev());
        customer_info.put("customer_num", info.getKunnr());
        customer_info.put("customer_tel", info.getParvo_tel());


        text_name.setText("客户名称:" + info.getName1() + "\n"
                + "客户地址:" + info.getStr_suppl() + "\n"
                + "客户编号:" + info.getKunnr() + "\n"
                + "联系人:" + info.getNamev() + "\n"
                + "客户电话:" + info.getParvo_tel() + "\n");

        order_list.clear();
        text_dec_tv.setText("0");
        for (SynergyCustomerOrderItemInfo item : info.getZtsd031()) {
            Map<String, String> map = new HashMap<>();
            map.put("order_no", item.getVbeln());
            map.put("price", item.getKbetr());
            map.put("cash", item.getSkbetr());
             receive_cash = Double.parseDouble( item.getSkbetr());
             text_act_tv.setText( item.getSkbetr());//直接修改金额
            order_list.add(map);
        }

        text_order_no_tv.setText(order_no);
        handler.sendEmptyMessage(ORDER_FETCH_SUCCESS);
        hideWaitDialog();
    }

    @Override
    protected void confirm(int type, DialogInterface dialog) {
        super.confirm(type, dialog);
        switch (type) {
            case EXIT_CONFIRM:
                finish();
                break;
            case TRY_AGAIN:
                showWaitDialog("正在查询订单号...");
                SynergyRequest synergyRequest = new SynergyRequest();
                synergyRequest.setSCFID(TempOrderNum);

                present.initRetrofit(Constant.URL_SYNERGY,false);
                present.QueryOder(synergyRequest);

                break;

        }
    }

    private void pay() {
        if (order_list.size() < 1) {
            VToast.toast(context, "没有要支付的订单");
            return;
        }
       // gotoSynergy();

    }

    private void gotoSynergy() {
        if (order_no == null) {
            VToast.toast(context, "请先查询订单");
            return;
        }
        String pn = "com.synergymall.driver";
        String an = "com.synergymall.driver.order.OrderHistoryDetailActivity";
        Intent intent = new Intent();
        ComponentName component = new ComponentName(pn, an);

        intent.setComponent(component);
        KLog.v(order_list.get(0).get("order_no"));
        intent.putExtra("orderNo", order_list.get(0).get("order_no")); //此处传入出库订单编号
        startActivity(intent);
    }

    private void gotoPay() {
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "没有网络连接，不能支付");
            return;
        }
        if (receive_cash == 0.00) {
            VToast.toast(context, "请先确认收货");
            return;
        }

        if(text_act_tv.getText().toString().equals("0")){
            VToast.toast(context, "请先获取订单");
            return;
        }

        Intent intent = new Intent(MainActivity.this, PayActivity.class);

        intent.putExtra("price", order_list.get(0).get("price"));
        intent.putExtra("cash", order_list.get(0).get("cash"));
        intent.putExtra("cash_for", text_act_tv.getText().toString());
        intent.putExtra("cash_re", text_dec_tv.getText().toString());
        intent.putExtra("order_no", order_list.get(0).get("order_no"));

        intent.putExtra("customer_name", customer_info.get("customer_name"));
        intent.putExtra("customer_add", customer_info.get("customer_add"));
        intent.putExtra("customer_tel", customer_info.get("customer_tel"));
        intent.putExtra("customer_num", customer_info.get("customer_num"));
        startActivityForResult(intent, 0);
    }

    private void showPopupWindow(int type) {

        CUR_EDIT = type;
        if (popupWindow == null) {
            popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setAnimationStyle(R.style.AnimationBottomFade);
            ColorDrawable dw = new ColorDrawable(0xffffffff);
            popupWindow.setBackgroundDrawable(dw);
            passwordLis();
        }
        // backgroundAlpha(0.5f);

        if (type == EDIT_ORDER) {
            tv_digit.setText("");
            btn_dot.setVisibility(View.GONE);
            btn_char_y.setVisibility(View.VISIBLE);
        } else {
            tv_digit.setText("0");
            if (receive_cash == 0.00) {
                VToast.toast(context, "请先确认收货");
                return;
            }
            btn_dot.setVisibility(View.VISIBLE);
            btn_char_y.setVisibility(View.GONE);
        }
        popupWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_main, null),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

   /* public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }*/

    private void passwordLis() {
        RxView.clicks(btn_txt1).subscribe(s -> textBtn('1'));
        RxView.clicks(btn_txt2).subscribe(s -> textBtn('2'));
        RxView.clicks(btn_txt3).subscribe(s -> textBtn('3'));
        RxView.clicks(btn_txt4).subscribe(s -> textBtn('4'));
        RxView.clicks(btn_txt5).subscribe(s -> textBtn('5'));
        RxView.clicks(btn_txt6).subscribe(s -> textBtn('6'));
        RxView.clicks(btn_txt7).subscribe(s -> textBtn('7'));
        RxView.clicks(btn_txt8).subscribe(s -> textBtn('8'));
        RxView.clicks(btn_txt9).subscribe(s -> textBtn('9'));
        RxView.clicks(btn_txt0).subscribe(s -> textBtn('0'));
        RxView.clicks(btn_char_y).subscribe(s -> textBtn('Y'));
        RxView.clicks(btn_dot).subscribe(s -> textBtn('.'));
        RxView.clicks(btn_del).subscribe(s -> del());
        RxView.clicks(btn_confirm).subscribe(s -> research());
        RxView.clicks(btn_cancel).subscribe(s -> clear());
    }

    /**
     * 显示并格式化输入
     *
     * @param paramChar
     */
    private void textBtn(char paramChar) {
        StringBuilder sb = new StringBuilder();
        String val = tv_digit.getText().toString();

        if (val.indexOf(".") == val.length() - 3 && val.length() > 3) {//小数点后面保留两位
            return;
        }
        if (paramChar == '.' && val.indexOf(".") != -1) {//只出现一次小数点
            return;
        }
        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            if (paramChar == '0' && val.charAt(0) == '0' && val.indexOf(".") == -1) {//no 0000
                return;
            }
        }
        if (val.length() > 30) {//最大长度
            return;
        }
        sb.append(val.toCharArray()).append(paramChar);
        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            if (sb.length() > 1 && sb.charAt(0) == '0' && sb.charAt(1) != '.') {
                sb.deleteCharAt(0);
            }
        }
        tv_digit.setText(sb.toString());
    }

    /**
     * 退格
     */
    private void del() {
        char[] chars = tv_digit.getText().toString().toCharArray();
        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            if (chars.length == 1) {
                tv_digit.setText("0");
                return;
            }
        }else{
            if (chars.length <= 1) {
                tv_digit.setText("");
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(chars);
        sb.deleteCharAt(sb.length() - 1);
        if (sb.charAt(sb.length() - 1) == '.') {
            sb.deleteCharAt(sb.length() - 1);
        }
        tv_digit.setText(sb.toString());
    }


    /**
     * 确定
     */
    private void confirm() {
        popupWindow.dismiss();
        String d = tv_digit.getText().toString();
        if (order_list.size() == 0) {
            return;
        }
        if (Double.parseDouble(d) > Double.parseDouble(order_list.get(0).get("cash"))) {
            VToast.toast(context, "不能超过可付金额");
            return;
        }
        if (d.indexOf(".") == -1) {
            d += ".00";
        }
        if (d.equals("0.0") || d.equals("0.00")) {
            d = "0";
        }
        text_dec_tv.setText(d);

    }


    private void research() {
        popupWindow.dismiss();

        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            double dec = Double.parseDouble(tv_digit.getText().toString());

            double result_pay = util.sub( receive_cash,dec);
            if(result_pay<0){
                VToast.toast(context,"不能大于需付金额");
                return;
            }
            text_dec_tv.setText(tv_digit.getText().toString());
            text_act_tv.setText(result_pay+"");

        } else {

            if (!util.isNetworkConnected(context)) {
                VToast.toast(context, "貌似没有网络");
                return;
            }
            showWaitDialog("正在查询订单号...");

            TempOrderNum = tv_digit.getText().toString();

            SynergyRequest synergyRequest = new SynergyRequest();
            synergyRequest.setSCFID(TempOrderNum);

            present.initRetrofit(Constant.URL_SYNERGY,false);
            present.QueryOder(synergyRequest);
        }
    }

    /**
     * 清空
     */
    private void clear() {
        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            tv_digit.setText("0");
        }else{
            tv_digit.setText("");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Exit();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.intent.action.PayAmountBroadcastReceiver")) {
                String status = intent.getStringExtra("checkStatus");
                receive_cash = intent.getDoubleExtra("checkOrderAmount", 0);
                if (status.equals("1001")) {
                    // VToast.toast(context, "确认收货成功");
                    KLog.v(receive_cash + "");
                    bottom_lay.setVisibility(View.VISIBLE);
                    text_act_tv.setText(receive_cash + "");
                    // gotoPay();//d
                } else {//新利源回调失败
                    VToast.toast(context, "确认收货失败");
                }
            }else{

            }
        }
    }
}

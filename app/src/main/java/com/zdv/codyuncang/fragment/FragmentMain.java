package com.zdv.codyuncang.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.socks.library.KLog;
import com.zdv.codyuncang.R;
import com.zdv.codyuncang.bean.SynergyCustomerOrderInfo;
import com.zdv.codyuncang.bean.SynergyCustomerOrderItemInfo;
import com.zdv.codyuncang.bean.SynergyRequest;
import com.zdv.codyuncang.present.QueryPresent;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.Utils;
import com.zdv.codyuncang.utils.VToast;
import com.zdv.codyuncang.view.IOrderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FragmentMain extends BaseFragment  implements IOrderView {
    private static final int ORDER_FETCH_SUCCESS = 10;//  订单获取成功
    private static final int ORDER_FETCH_FAIL = ORDER_FETCH_SUCCESS + 1;// 订单获取失败
    private static final int ORDER_FETCH_NOT = ORDER_FETCH_FAIL + 1;// 订单没记录

    private static final int EXIT_CONFIRM = 1025;
    private static final int TRY_AGAIN = EXIT_CONFIRM + 1;

    private static final int EDIT_ACT_CASH = 20;// 修改退货金额

    private static final int EDIT_ORDER = 21;// 修改订单号
    private static final int EDIT_FINAL_CASH = 22;// 修改实付金额
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
    IMainListener listner;
    List<Map<String, String>> order_list;
    Map<String, String> customer_info;
    SimpleAdapter adapter;
    private boolean isCallback = false;
    Boolean isFetching = false;
    Utils util;

    View view;
    View popupWindowView;
    private PopupWindow popupWindow;
    private LinearLayout edit_act_cash_lay, edit_should_cash_lay, bottom_lay,layout_result,tip_lay;
    private TextView confirm_order_tv, edit_order_tv, text_should_tv, text_order_no_tv, text_dec_tv, text_name, text_act_tv, edit_pay_tv, scan_order_tv;
    Double receive_cash = 0.00;
    String order_no;
    QueryPresent present;
    private String   scan_data = "";
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ORDER_FETCH_SUCCESS:
                    adapter.notifyDataSetChanged();
                    break;
                case ORDER_FETCH_FAIL:
                    showDialog(TRY_AGAIN, "查询订单失败","与服务器的连接不成功",  "重试", "取消");
                    break;
                case ORDER_FETCH_NOT:
                    VToast.toast(context, (String) msg.obj);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDate();
        initView();

    }

    public void fetchScanResult(String ret) {
        if (isFetching) {
            return;
        }
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "貌似没有网络");
            return;
        }
        scan_data = ret;
        KLog.v("ret" + ret.trim());
        showWaitDialog("正在查询订单号...");
        order_list.clear();
        adapter.notifyDataSetChanged();
        text_dec_tv.setText("0.00");
        text_should_tv.setText("0.00");
        isCallback = false;
        TempOrderNum = ret.trim();
        SynergyRequest synergyRequest = new SynergyRequest();
        synergyRequest.setSCFID(TempOrderNum);

        present.initRetrofit(Constant.URL_SYNERGY , false);
        present.QueryOder(synergyRequest);
        isFetching = true;
    }


    private void initView() {
        text_dec_tv = (TextView) view.findViewById(R.id.text_dec_tv);
        text_order_no_tv = (TextView) view.findViewById(R.id.text_order_no_tv);
        edit_act_cash_lay = (LinearLayout) view.findViewById(R.id.edit_act_cash_lay);
        edit_should_cash_lay = (LinearLayout) view.findViewById(R.id.edit_should_cash_lay);
        layout_result = (LinearLayout) view.findViewById(R.id.layout_result);
        tip_lay = (LinearLayout) view.findViewById(R.id.tip_lay);
        text_should_tv = (TextView) view.findViewById(R.id.text_should_tv);
        bottom_lay = (LinearLayout) view.findViewById(R.id.bottom_lay);
        text_act_tv = (TextView) view.findViewById(R.id.text_act_tv);
        text_name = (TextView) view.findViewById(R.id.text_name_tv);
        confirm_order_tv = (TextView) view.findViewById(R.id.confirm_order_tv);
        edit_order_tv = (TextView) view.findViewById(R.id.edit_order_tv);
        scan_order_tv = (TextView) view.findViewById(R.id.scan_order_tv);
        edit_pay_tv = (TextView) view.findViewById(R.id.edit_pay_tv);
        scan_order_tv = (TextView) view.findViewById(R.id.scan_order_tv);
        //  input_order = (TextView) findViewById(R.id.input_order);
        listView = (ListView) view.findViewById(R.id.listView);
        popupWindowView = View.inflate(context,R.layout.pop_password, null);
        ButterKnife.bind(FragmentMain.this, popupWindowView);
        bottom_lay.setVisibility(View.GONE);
        layout_result.setVisibility(View.GONE);
        edit_pay_tv.setVisibility(View.GONE);
        tip_lay.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        RxView.clicks(edit_pay_tv).subscribe(s -> gotoPay());
        RxView.clicks(scan_order_tv).subscribe(s ->  startScan());
        // RxView.clicks(scan_order_tv).subscribe(s -> startScan());

        //RxView.clicks(confirm_order_tv).subscribe(s -> gotoSynergy());
        RxView.clicks(edit_order_tv).subscribe(s -> showPopupWindow(EDIT_ORDER));
        RxView.clicks(text_order_no_tv).subscribe(s -> showPopupWindow(EDIT_ORDER));
        RxView.clicks(edit_act_cash_lay).subscribe(s -> showPopupWindow(EDIT_ACT_CASH));
        RxView.clicks(edit_should_cash_lay).subscribe(s -> showPopupWindow(EDIT_FINAL_CASH));

        View header = View.inflate(context, R.layout.listheader, null);
        listView.addHeaderView(header);
        adapter = new SimpleAdapter(context, order_list, R.layout.listheader, new String[]{"order_no", "price", "cash"}, new int[]{R.id.order_no, R.id.price, R.id.cash});
        listView.setAdapter(adapter);
        // listView.setOnItemClickListener((parent, view, position, id) -> itemClick(position));

    }

    private void startScan() {
        VToast.toast(context,"请按右侧蓝色键扫描");
        listner.scan();
    }

    private void initDate() {
        present = QueryPresent.getInstance(context);
        present.setView(FragmentMain.this);
        util = Utils.getInstance();
        order_list = new ArrayList<>();
    }


    public void initState(){
        receive_cash = 0.00;
        order_no = null;
        order_list.clear();
        isCallback = false;
        isFetching = false;
        text_dec_tv.setText("0.00");
        text_act_tv.setText("0.00");
        text_order_no_tv.setText("");
        bottom_lay.setVisibility(View.GONE);
        layout_result.setVisibility(View.GONE);
        edit_pay_tv.setVisibility(View.GONE);
        tip_lay.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
        text_name.setText("");
        adapter.notifyDataSetChanged();
        showWaitDialog("请稍候");
        new Handler().postDelayed(()->{
            hideWaitDialog();
            listner.startOpenDevice();
            listner.scan();},3000);
    }

    @Override
    public void ResolveCustomerOrder(SynergyCustomerOrderInfo info) {
        hideWaitDialog();
        isFetching = false;
        scan_data = "";

        Message msg = new Message();
        if (info.getName1() == null) {
            msg.what = ORDER_FETCH_FAIL;
            msg.obj = "网络错误";
            handler.sendMessage(msg);
            hideWaitDialog();
            return;
        }
        showWaitDialog("请稍候");
        bottom_lay.postDelayed( ()->{ hideWaitDialog();
            listner.scan();},1000);
        KLog.v(info.toString());
        if (info.getEm_type() != null && !info.getEm_type().equals("S")) {
            msg.what = ORDER_FETCH_NOT;
            msg.obj = info.getOm_message();
            handler.sendMessage(msg);
            hideWaitDialog();
            return;
        }

        bottom_lay.setVisibility(View.VISIBLE);
        layout_result.setVisibility(View.VISIBLE);
        edit_pay_tv.setVisibility(View.VISIBLE);
        tip_lay.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        text_order_no_tv.setText(TempOrderNum);
        order_no = text_order_no_tv.getText().toString();
        isCallback = true;
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
        text_dec_tv.setText("0.00");
        for (SynergyCustomerOrderItemInfo item : info.getZtsd031()) {
            Map<String, String> map = new HashMap<>();
            map.put("order_no", item.getVbeln());
            map.put("price", item.getKbetr());
            map.put("cash", item.getSkbetr());
            receive_cash = Double.parseDouble(item.getSkbetr());
            text_act_tv.setText(item.getSkbetr());//直接修改金额
            text_should_tv.setText(item.getSkbetr());//直接修改金额
            order_list.add(map);
        }
        text_order_no_tv.setText(order_no);
        handler.sendEmptyMessage(ORDER_FETCH_SUCCESS);

    }

    @Override
    protected void confirm(int type, DialogInterface dialog) {
        super.confirm(type, dialog);
        switch (type) {
            case EXIT_CONFIRM:
                listner.finishMain();
                break;
            case TRY_AGAIN:
                showWaitDialog("正在查询订单号...");
                SynergyRequest synergyRequest = new SynergyRequest();
                synergyRequest.setSCFID(TempOrderNum);
                present.initRetrofit(Constant.URL_SYNERGY, false);
                present.QueryOder(synergyRequest);
                break;
        }
    }

    @Override
    protected void cancel(int type, DialogInterface dia) {
        super.cancel(type, dia);
        switch(type){
            case TRY_AGAIN:
                showWaitDialog("请稍候");
                bottom_lay.postDelayed( ()->{ hideWaitDialog();
                    listner.scan();},1000);
                break;
        }
    }

    private void gotoPay() {
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "没有网络连接，不能支付");
            return;
        }
        if (!isCallback) {
            VToast.toast(context, "请先扫描订单");
            return;
        }

        if (Double.parseDouble(text_act_tv.getText().toString().trim())==0) {
            VToast.toast(context, "支付金额不能等于0");
            return;
        }

        HashMap<String,String> intent  = new HashMap<>();
        intent.put("price", order_list.get(0).get("price"));
        intent.put("cash", order_list.get(0).get("cash"));
        intent.put("cash_for", text_should_tv.getText().toString());
        intent.put("cash_cur", text_act_tv.getText().toString());
        intent.put("cash_re", text_dec_tv.getText().toString());
        intent.put("order_no", order_list.get(0).get("order_no"));

        intent.put("customer_name", customer_info.get("customer_name"));
        intent.put("customer_add", customer_info.get("customer_add"));
        intent.put("customer_tel", customer_info.get("customer_tel"));
        intent.put("customer_num", customer_info.get("customer_num"));

        listner.gotoPay(intent);
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
            if (!isCallback) {
                VToast.toast(context, "请先确认收货");
                return;
            }
            btn_dot.setVisibility(View.VISIBLE);
            btn_char_y.setVisibility(View.GONE);
        }
        popupWindow.showAtLocation(View.inflate(context,R.layout.activity_main, null),
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
        if (CUR_EDIT == EDIT_FINAL_CASH) {//区分订单输入或金额
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
        if (CUR_EDIT == EDIT_FINAL_CASH) {//区分订单输入或金额
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
        } else if (CUR_EDIT == EDIT_FINAL_CASH) {
            if (chars.length == 1) {
                tv_digit.setText("0");
                return;
            }
        } else {
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


    private void research() {
        popupWindow.dismiss();

        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            double dec = Double.parseDouble(tv_digit.getText().toString());
            double result_pay = util.sub(receive_cash, dec);
            if (result_pay < 0) {
                VToast.toast(context, "不能大于应付金额");
                return;
            }
            text_dec_tv.setText(tv_digit.getText().toString());
            text_should_tv.setText(result_pay + "");

            order_list.get(0).put("cash", result_pay + "");
            adapter.notifyDataSetChanged();

            if (result_pay < Double.parseDouble(text_act_tv.getText().toString())) {
                text_act_tv.setText(result_pay + "");
            }

        } else if (CUR_EDIT == EDIT_FINAL_CASH) {
            double dec = Double.parseDouble(tv_digit.getText().toString());
            double dec2 = Double.parseDouble(text_should_tv.getText().toString());
            if (dec > dec2) {
                VToast.toast(context, "不能大于应付金额");
                return;
            }
            if (Double.parseDouble(text_act_tv.getText().toString()) > dec2) {
                text_act_tv.setText(text_should_tv.getText().toString());
            } else {
                text_act_tv.setText(dec + "");
            }
        } else {
            if (!util.isNetworkConnected(context)) {
                VToast.toast(context, "貌似没有网络");
                return;
            }
            showWaitDialog("正在查询订单号...");
            TempOrderNum = tv_digit.getText().toString();
            SynergyRequest synergyRequest = new SynergyRequest();
            synergyRequest.setSCFID(TempOrderNum);

            present.initRetrofit(Constant.URL_SYNERGY, false);
            present.QueryOder(synergyRequest);
        }
    }


    /**
     * 清空
     */
    private void clear() {
        if (CUR_EDIT == EDIT_ACT_CASH) {//区分订单输入或金额
            tv_digit.setText("0");
        } else if (CUR_EDIT == EDIT_FINAL_CASH) {
            tv_digit.setText("0");
        } else {
            tv_digit.setText("");
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listner = (IMainListener) context;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public interface IMainListener {
        void finishMain();
        void gotoPay(HashMap<String,String> intent );
        void gotoSynergy(String order_no);
        void startOpenDevice();
        void scan();
    }
}

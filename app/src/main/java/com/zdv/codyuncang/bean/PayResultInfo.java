package com.zdv.codyuncang.bean;

/**
 * Info: 支付结果bean
 * Created by xiaoyl
 * 创建时间:2017/4/17 12:01
 */

public class PayResultInfo {
    String msg;
    String code;
    String order_no;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    @Override
    public String toString() {
        return msg+"-"+order_no;
    }
}

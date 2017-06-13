package com.zdv.codyuncang.bean;

/**
 * Info:支付请求类的bean
 * Created by xiaoyl
 * 创建时间:2017/4/17 10:00
 */

public class CheckPayInfo {
   String username;
   String password;
   String order_no;
    String auth_code;

    public String getAuth_code() {
        return auth_code;
    }

    public void setAuth_code(String auth_code) {
        this.auth_code = auth_code;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}

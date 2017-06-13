package com.zdv.codyuncang.bean;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/11 16:49
 */

public class LoginInfoRequest {
   private String user_login;
   private String user_pass;
   public LoginInfoRequest(String user_login_, String user_pass_){
       user_login = user_login_;
       user_pass = user_pass_;
   }
    public String getUser_login() {
        return user_login;
    }

    public void setUser_login(String user_login) {
        this.user_login = user_login;
    }

    public String getUser_pass() {
        return user_pass;
    }

    public void setUser_pass(String user_pass) {
        this.user_pass = user_pass;
    }
}

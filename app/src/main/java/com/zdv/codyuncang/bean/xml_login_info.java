package com.zdv.codyuncang.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Info:登录解析Bean
 * Created by xiaoyl
 * 创建时间:2017/4/11 16:49
 */
@Root(name = "data", strict = false) //name:要解析的xml数据的头部
public class xml_login_info {
    @Element(name = "msg",required = true)
    public String msg;
    @Element(name = "user_login",required = false)
    public String user_login;
    @Element(name = "user_email",required = false)
    public String user_email;
    @Element(name = "phone",required = false)
    public String phone;
    @Element(name = "user_account",required = false)
    public String user_account;
    @Element(name = "alipay_account",required = false)
    public String alipay_account;
    @Element(name = "score",required = false)
    public String score;
    @Element(name = "user_rank",required = false)
    public String user_rank;
    @Element(name = "isv",required = false)
    public String isv;

}

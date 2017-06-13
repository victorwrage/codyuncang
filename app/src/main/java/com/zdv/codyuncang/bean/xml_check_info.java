package com.zdv.codyuncang.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Info:支付解析Bean
 * Created by xiaoyl
 * 创建时间:2017/4/11 16:49
 */
@Root(name = "item", strict = false) //name:要解析的xml数据的头部
public class xml_check_info {
    @Element(name = "status",required = false)
    public String status;

    @Element(name = "pay_status",required = false)
    public String pay_status;

    @Element(name = "title",required = false)
    public String title;
    @Element(name = "result_code",required = false)
    public String result_code;
    @Element(name = "order_no",required = false)
    public String order_no;
    @Element(name = "out_trade_no",required = false)
    public String out_trade_no;
    @Element(name = "buyer_logon_id",required = false)
    public String buyer_logon_id;
    @Element(name = "mach_order_id",required = false)
    public String mach_order_id;
    @Element(name = "pay_time",required = false)
    public String pay_time;
    @Element(name = "msg",required = false)
    public String msg;



}

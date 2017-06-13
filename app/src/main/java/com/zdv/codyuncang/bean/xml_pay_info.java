package com.zdv.codyuncang.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Info:支付解析Bean
 * Created by xiaoyl
 * 创建时间:2017/4/11 16:49
 */
@Root(name = "item", strict = false) //name:要解析的xml数据的头部
public class xml_pay_info {
    @Element(name = "msg",required = false)
    public String msg;
    @Element(name = "code",required = false)
    public String code;
    @Element(name = "order_no",required = false)
    public String order_no;

    @Element(name = "title",required = false)
    public String title;

    @Element(name = "result_code",required = false)
    public String result_code;


}

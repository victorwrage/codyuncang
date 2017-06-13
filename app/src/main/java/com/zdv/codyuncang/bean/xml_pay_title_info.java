package com.zdv.codyuncang.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Info:支付解析Bean
 * Created by xiaoyl
 * 创建时间:2017/4/11 16:49
 */
@Root(name = "title", strict = false) //name:要解析的xml数据的头部
public class xml_pay_title_info {
    @Element(name = "size",required = false)
    @Path("title")
    public String size;


}

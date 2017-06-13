package com.zdv.codyuncang.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Info:支付解析Bean
 * Created by xiaoyl
 * 创建时间:2017/4/14 15:59
 */
@Root(name = "article", strict = false)
public class xml_check_info_root {
    @Element(name = "item")
    public xml_check_info xml_data;
}

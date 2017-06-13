package com.zdv.codyuncang.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Info:登录解析Bean
 * Created by xiaoyl
 * 创建时间:2017/4/14 15:59
 */
@Root(name = "root", strict = false)
public class xml_login_info_root {
    @Element(name = "data")
    public xml_login_info xml_data;
}

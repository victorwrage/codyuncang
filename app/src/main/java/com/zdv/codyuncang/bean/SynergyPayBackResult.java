package com.zdv.codyuncang.bean;

/**
 * Info: 返回给新利源支付信息的回调
 * Created by xiaoyl
 * 创建时间:2017/4/17 16:41
 */

public class SynergyPayBackResult {
    String em_type;
    String om_message;

    public String getEm_type() {
        return em_type;
    }

    public void setEm_type(String em_type) {
        this.em_type = em_type;
    }

    public String getOm_message() {
        return om_message;
    }

    public void setOm_message(String om_message) {
        this.om_message = om_message;
    }

    @Override
    public String toString() {
        return em_type+"-"+om_message;
    }
}

package com.zdv.codyuncang.bean;

/**
 * Info: 从新利源获取订单信息
 * Created by xiaoyl
 * 创建时间:2017/4/7 10:00
 */

public class SynergyRequest {

    public String getSCFID() {
        return SCFID;
    }

    public void setSCFID(String SCFID) {
        this.SCFID = SCFID;
    }

    public String getVBELN() {
        return VBELN;
    }

    public void setVBELN(String VBELN) {
        this.VBELN = VBELN;
    }

    private String SCFID="";
    private String VBELN="";

    public SynergyRequest Builder(){
        return this;
    }

}

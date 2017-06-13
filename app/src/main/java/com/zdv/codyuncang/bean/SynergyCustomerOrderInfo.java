package com.zdv.codyuncang.bean;

import java.util.ArrayList;

/**
 * Info: 从星利源得到的客户信息
 * Created by xiaoyl
 * 创建时间:2017/4/17 14:36
 */

public class SynergyCustomerOrderInfo {
    String kunnr;
    String name1;
    String namev;
    String parvo_tel;
    String str_suppl;
    String em_type;
    String om_message;

    ArrayList<SynergyCustomerOrderItemInfo> ztsd031;

    public ArrayList<SynergyCustomerOrderItemInfo> getZtsd031() {
        return ztsd031;
    }

    public void setZtsd031(ArrayList<SynergyCustomerOrderItemInfo> ztsd031) {
        this.ztsd031 = ztsd031;
    }

    public String getKunnr() {
        return kunnr;
    }

    public String getNamev() {
        return namev;
    }

    public void setNamev(String namev) {
        this.namev = namev;
    }

    public void setKunnr(String kunnr) {
        this.kunnr = kunnr;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getParvo_tel() {
        return parvo_tel;
    }

    public void setParvo_tel(String parvo_tel) {
        this.parvo_tel = parvo_tel;
    }

    public String getStr_suppl() {
        return str_suppl;
    }

    public void setStr_suppl(String str_suppl) {
        this.str_suppl = str_suppl;
    }

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
        return kunnr+"--"+name1+"--"+namev+"--"+parvo_tel+"--"+str_suppl+"--"+em_type+"--"+om_message+"-"+ztsd031.size();
    }
}

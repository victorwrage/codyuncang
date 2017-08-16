package com.zdv.codyuncang.bean;

import java.util.ArrayList;

/**
 * Info: 返回给新利源支付信息
 * Created by xiaoyl
 * 创建时间:2017/4/17 16:41
 */

public class SynergyPayBack {
    public ArrayList<ItemInfo> getZpos_pay() {
        return zpos_pay;
    }

    public void setZpos_pay( ArrayList<ItemInfo> zpos_pay) {
        this.zpos_pay = zpos_pay;
    }

    ArrayList<ItemInfo> zpos_pay;

    public class ItemInfo {
        String kunnr;
        String vbeln;
        String pos_num;
        String pay_day;
        String pay_time;
        String name1;
        String pay_person;
        String pay_money;
        String re_money;
        String dmbtr;

        String BANK_NUM;
        String PATCH_NUM;
        public String getBANK_NUM() {
            return BANK_NUM;
        }

        public void setBANK_NUM(String BANK_NUM) {
            this.BANK_NUM = BANK_NUM;
        }

        public String getPATCH_NUM() {
            return PATCH_NUM;
        }

        public void setPATCH_NUM(String PATCH_NUM) {
            this.PATCH_NUM = PATCH_NUM;
        }


        String pay_mode;

        public String getPay_mode() {
            return pay_mode;
        }

        public void setPay_mode(String pay_mode) {
            this.pay_mode = pay_mode;
        }

        public ItemInfo(){

        }
        public String getPay_time() {
            return pay_time;
        }

        public void setPay_time(String pay_time) {
            this.pay_time = pay_time;
        }

        public String getPay_person() {
            return pay_person;
        }

        public void setPay_person(String pay_person) {
            this.pay_person = pay_person;
        }

        public String getDmbtr() {
            return dmbtr;
        }

        public String getRe_money() {
            return re_money;
        }

        public void setRe_money(String re_money) {
            this.re_money = re_money;
        }

        public void setDmbtr(String dmbtr) {
            this.dmbtr = dmbtr;
        }



        public String getKunnr() {
            return kunnr;
        }

        public void setKunnr(String kunnr) {
            this.kunnr = kunnr;
        }

        public String getVbeln() {
            return vbeln;
        }

        public void setVbeln(String vbeln) {
            this.vbeln = vbeln;
        }

        public String getPos_num() {
            return pos_num;
        }

        public void setPos_num(String pos_num) {
            this.pos_num = pos_num;
        }

        public String getPay_day() {
            return pay_day;
        }

        public void setPay_day(String pay_day) {
            this.pay_day = pay_day;
        }

        public String getName1() {
            return name1;
        }

        public void setName1(String name1) {
            this.name1 = name1;
        }

        public String getPay_money() {
            return pay_money;
        }

        public void setPay_money(String pay_money) {
            this.pay_money = pay_money;
        }

        @Override
        public String toString() {
            return "kunnr:"+kunnr+"vbeln:"+vbeln+"pos_num:"+pos_num+"pay_day:"+pay_day+
                    "pay_time:"+pay_time+"name1:"+name1+"pay_person:"+pay_person+"pay_money:"+pay_money
                    +"dmbtr:"+dmbtr;
        }
    }
}

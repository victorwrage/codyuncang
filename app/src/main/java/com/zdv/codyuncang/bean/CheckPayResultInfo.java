package com.zdv.codyuncang.bean;

/**
 * Info:检查支付状态返回bean
 * Created by xiaoyl
 * 创建时间:2017/4/17 10:00
 */

public class CheckPayResultInfo {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String status;


    @Override
    public String toString() {
        return status;
    }
}

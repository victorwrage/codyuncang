package com.zdv.codyuncang.present;


import com.zdv.codyuncang.bean.CheckPayInfo;
import com.zdv.codyuncang.bean.LoginInfoRequest;
import com.zdv.codyuncang.bean.PayInfo;
import com.zdv.codyuncang.bean.SynergyPayBack;
import com.zdv.codyuncang.bean.SynergyRequest;


/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/7 9:46
 */

public interface IRequestPresent {
    void QueryOder(SynergyRequest synergyRequest);
    void SendPay(SynergyPayBack synergyPayBack);
    void SendToMall(String ddh);
    void Login(LoginInfoRequest request);

    void Pay(String path, PayInfo info);
    void CheckPay(String path, CheckPayInfo info);

}

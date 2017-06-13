package com.zdv.codyuncang.view;

import com.zdv.codyuncang.bean.SynergyCustomerOrderInfo;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/7 9:49
 */

public interface IOrderView extends IView{

    void ResolveCustomerOrder(SynergyCustomerOrderInfo info);
}

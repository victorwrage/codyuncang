package com.zdv.codyuncang.model;

import com.zdv.codyuncang.bean.SynergyCustomerOrderInfo;
import com.zdv.codyuncang.bean.SynergyMallResponse;
import com.zdv.codyuncang.bean.SynergyPayBack;
import com.zdv.codyuncang.bean.SynergyPayBackResult;
import com.zdv.codyuncang.bean.SynergyRequest;
import com.zdv.codyuncang.bean.xml_check_info_root;
import com.zdv.codyuncang.bean.xml_login_info_root;
import com.zdv.codyuncang.bean.xml_pay_info_root;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by xyl on 2017/4/6.
 */

public interface IRequestMode {

    @POST
    Observable<SynergyCustomerOrderInfo> QueryOrder(@Url String url,@Body SynergyRequest synergyRequest);


    @POST
    Observable<SynergyPayBackResult> SendPay(@Url String url,@Body SynergyPayBack synergyPayBack);

    @GET("synergyMallServcie/order/getDetailByScmForZDW.jhtml?")
    Observable<SynergyMallResponse> SendPayToMall(@Query("ddh") String ddh);

    @FormUrlEncoded
    @POST("App/Appinterface/login2/")
    Observable<xml_login_info_root> Login(@Field("user_login") String user_login, @Field("user_pass") String user_pass);

    @FormUrlEncoded
    @POST("Admin/Pospay/{type}/")
    Observable<xml_pay_info_root> Pay(@Path("type") String type, @Field("username") String username, @Field("password") String password, @Field("numscreen") String numscreen, @Field("code") String code);

    @FormUrlEncoded
    @POST("Admin/Pospay/{type}/")
    Observable<xml_check_info_root> CheckPayA(@Path("type") String type, @Field("username") String username, @Field("password") String password, @Field("order_no") String order_no);

    @FormUrlEncoded
    @POST("Admin/Pospay/{type}/")
    Observable<xml_check_info_root> CheckPayB(@Path("type") String type, @Field("username") String username, @Field("password") String password, @Field("auth_code") String auth_code);

}

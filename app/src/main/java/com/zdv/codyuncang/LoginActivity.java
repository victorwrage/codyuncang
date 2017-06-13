package com.zdv.codyuncang;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;
import com.zdv.codyuncang.bean.LoginInfoRequest;
import com.zdv.codyuncang.bean.xml_login_info_root;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.VToast;
import com.zdv.codyuncang.view.ILoginView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements ILoginView {
    private static final String COOKIE_KEY = "cookie";
    @Bind(R.id.username_edit)
    EditText username_edit;
    @Bind(R.id.password_edit)
    EditText password_edit;
    @Bind(R.id.cb_rem_pw)
    CheckBox cb_rem_pw;
    @Bind(R.id.button_login)
    Button button_login;

    SharedPreferences sp;
    ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        initDate();
        initView();

        present.initRetrofit(Constant.URL_BAIBAO,true);
        present.setView(LoginActivity.this);
        RxView.clicks(button_login)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(s -> Login());

    }


    /**
     * 初始化数据
     */
    private void initDate() {
        sp = getSharedPreferences(COOKIE_KEY, 0);

        username_edit.setText(sp.getString("user_name", ""));
        password_edit.setText(sp.getString("user_pw", ""));
        Constant.cookie.put("user_name", sp.getString("user_name", ""));
        Constant.cookie.put("user_pw", sp.getString("user_pw", ""));
        if (!sp.getString("user_name", "").equals("")) {
            cb_rem_pw.setChecked(true);
        }

    }

    /**
     * 初始化一些显示
     */
    private void initView() {

    }

    /**
     * 登录账户
     */
    private void Login() {
        if(!util.isNetworkConnected(context)){
            VToast.toast(context,"没有网络连接，不能支付");
            return;
        }
        if (username_edit.getText().toString().trim().equals("") ) {
            username_edit.setError("请输入用户名");
        } else if( password_edit.getText().toString().trim().equals("")){
            password_edit.setError("请输入密码");
        } else{
            SharedPreferences.Editor editor = sp.edit();
            if (cb_rem_pw.isChecked()) {
                editor.putString("user_name", username_edit.getText().toString().trim());
                editor.putString("user_pw", password_edit.getText().toString().trim());
                editor.commit();
            } else {
                editor.clear();
                editor.commit();
            }
            showWaitDialog();
            present.Login(new LoginInfoRequest(username_edit.getText().toString(), password_edit.getText().toString()));
        }
    }

    @Override
    public void ResolveLoginInfo(xml_login_info_root info) {

        hideWaitDialog();
        //KLog.v("wragee"+info.xml_data.msg);
        if(info.xml_data==null){
           VToast.toast(context, "网络超时");
           return;
        }
        VToast.toast(context, "" + info.xml_data.msg);
        if (info.xml_data.msg.equals("登录成功")) {
            Constant.cookie.put("user_name",  username_edit.getEditableText().toString().trim());
            Constant.cookie.put("user_pw", password_edit.getEditableText().toString().trim());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

    }

    @Override
    public void showWaitDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("正在登录");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void hideWaitDialog() {
        progressDialog.dismiss();
    }

}

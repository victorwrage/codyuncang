package com.zdv.codyuncang;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.socks.library.KLog;
import com.zdv.codyuncang.bean.LoginInfoRequest;
import com.zdv.codyuncang.bean.xml_login_info_root;
import com.zdv.codyuncang.utils.Constant;
import com.zdv.codyuncang.utils.VToast;
import com.zdv.codyuncang.view.ILoginView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateStatus;

public class LoginActivity extends BaseActivity implements ILoginView {
    private static final String COOKIE_KEY = "cookie";
    private static final String SERVER_INFO = "server_info";
    @Bind(R.id.username_edit)
    EditText username_edit;
    @Bind(R.id.password_edit)
    EditText password_edit;
    @Bind(R.id.cb_rem_pw)
    CheckBox cb_rem_pw;
    @Bind(R.id.button_login)
    Button button_login;
    @Bind(R.id.login_title)
    TextView login_title;
    @Bind(R.id.login_version)
    TextView login_version;

    @Bind(R.id.login_update)
    LinearLayout login_update;
    @Bind(R.id.login_setting)
    LinearLayout login_setting;

    View popupWindowView;
    View popupWindowViewPW;
    private PopupWindow popupWindow;
    private PopupWindow popupWindowPW;
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

        present.initRetrofit(Constant.URL_BAIBAO, true);
        present.setView(LoginActivity.this);
        RxView.clicks(button_login)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(s -> Login());

        RxView.clicks(login_update)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(s -> update());
        RxView.clicks(login_setting)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(s -> showPWPopupWindow(login_setting));


    }

    private void update() {
        try {
            Uri uri = Uri.parse("http://wdt.qianhaiwei.com/cyy/public/apk/2017-07-11/yuncangcod.apk");
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(it);
        } catch (ActivityNotFoundException e) {
            VToast.toast(context, "没有可用的浏览器，无法更新!");
        }
    }


    private void showPopupWindow(View view) {

        if (popupWindow == null) {
            popupWindow = new PopupWindow(popupWindowView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setAnimationStyle(R.style.AnimationLeftFade);
            ColorDrawable dw = new ColorDrawable(0xffffffff);
            popupWindow.setBackgroundDrawable(dw);
            RadioGroup serve_rp = (RadioGroup) popupWindowView.findViewById(R.id.serve_rp);
            RadioButton formal_rb = (RadioButton) popupWindowView.findViewById(R.id.formal_rb);
            RadioButton test_rb = (RadioButton) popupWindowView.findViewById(R.id.test_rb);

            if(sp.getBoolean(SERVER_INFO,true)){
                formal_rb.setChecked(true);
                test_rb.setChecked(false);
            }else{
                formal_rb.setChecked(false);
                test_rb.setChecked(true);
            }
            serve_rp.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId){
                    case R.id.formal_rb:
                        login_title.setText("云仓COD");
                        Constant.URL_SYNERGY = Constant.URL_SYNERGY_FORMAL;
                        Constant.URL_SYNERGY_PARAM = Constant.URL_SYNERGY_PARAM_FORMAL;
                        sp.edit().putBoolean(SERVER_INFO,true).commit();
                        break;
                    case R.id.test_rb:
                        login_title.setText("云仓COD(测试服务器)");
                        Constant.URL_SYNERGY = Constant.URL_SYNERGY_TEST;
                        Constant.URL_SYNERGY_PARAM = Constant.URL_SYNERGY_PARAM_TEST;
                        sp.edit().putBoolean(SERVER_INFO,false).commit();
                        break;
                }
            });

        }
        // backgroundAlpha(0.5f);
        popupWindow.showAtLocation(view,
                Gravity.CENTER | Gravity.CENTER, 0, 0);
    }
    private void showPWPopupWindow(View view) {

        if (popupWindowPW == null) {
            popupWindowPW = new PopupWindow(popupWindowViewPW, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindowPW.setAnimationStyle(R.style.AnimationLeftFade);
            ColorDrawable dw = new ColorDrawable(0xffffffff);
            popupWindowPW.setBackgroundDrawable(dw);

            Button pw_sure = (Button) popupWindowViewPW.findViewById(R.id.pw_sure_btn);
            EditText pw_pw = (EditText) popupWindowViewPW.findViewById(R.id.pw_pw_et);
            RxView.clicks(pw_sure)
                    .throttleFirst(1000, TimeUnit.MILLISECONDS)
                    .subscribe(s -> {
                        if(pw_pw.getText().toString().trim().equals("synergy")){
                            popupWindowPW.dismiss();
                            showPopupWindow(login_setting);
                        }else{
                            VToast.toast(context,"密码错误");
                        }
                    });
            popupWindowPW.setOnDismissListener(() -> {
                pw_pw.setText("");
            });
        }
        // backgroundAlpha(0.5f);
        popupWindowPW.showAtLocation(view,
                Gravity.CENTER | Gravity.CENTER, 0, 0);
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
        Bmob.initialize(this, Constant.PUBLIC_BMOB_KEY);
        // BmobUpdateAgent.initAppVersion(context);
        BmobUpdateAgent.setUpdateListener((updateStatus, updateInfo) -> {
            if (updateStatus == UpdateStatus.Yes) {//版本有更新

            } else if (updateStatus == UpdateStatus.No) {
                KLog.v("版本无更新");
            } else if (updateStatus == UpdateStatus.EmptyField) {//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                KLog.v("请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。");
            } else if (updateStatus == UpdateStatus.IGNORED) {
                KLog.v("该版本已被忽略更新");
            } else if (updateStatus == UpdateStatus.ErrorSizeFormat) {
                KLog.v("请检查target_size填写的格式，请使用file.length()方法获取apk大小。");
            } else if (updateStatus == UpdateStatus.TimeOut) {
                KLog.v("查询出错或查询超时");
            }
        });
        BmobUpdateAgent.update(this);

        popupWindowView = View.inflate(context,R.layout.pop_setting, null);
        popupWindowViewPW = View.inflate(context,R.layout.pop_pw, null);
        if(!sp.getBoolean(SERVER_INFO,true)){
            login_title.setText("云仓COD(测试服务器)");
        }else{
            login_title.setText("云仓COD");
        }

        login_version.setText("更新 "+util.getAppVersionName(context));
    }

    /**
     * 登录账户
     */
    private void Login() {
        if (!util.isNetworkConnected(context)) {
            VToast.toast(context, "没有网络连接，不能支付");
            return;
        }
        if (username_edit.getText().toString().trim().equals("")) {
            username_edit.setError("请输入用户名");
        } else if (password_edit.getText().toString().trim().equals("")) {
            password_edit.setError("请输入密码");
        } else {
            SharedPreferences.Editor editor = sp.edit();
            if (cb_rem_pw.isChecked()) {
                editor.putString("user_name", username_edit.getText().toString().trim());
                editor.putString("user_pw", password_edit.getText().toString().trim());
                editor.commit();
            } else {
                editor.putString("user_name", "");
                editor.putString("user_pw", "");
                editor.commit();
            }
            showWaitDialog("登陆中");
            present.Login(new LoginInfoRequest(username_edit.getText().toString(), password_edit.getText().toString()));
        }
    }

    @Override
    public void ResolveLoginInfo(xml_login_info_root info) {
        hideWaitDialog();
        if (info.xml_data == null) {
            VToast.toast(context, "网络超时");
            return;
        }
        VToast.toast(context, "" + info.xml_data.msg);
        if (info.xml_data.msg.equals("登录成功")) {
            Constant.cookie.put("user_name", username_edit.getEditableText().toString().trim());
            Constant.cookie.put("user_pw", password_edit.getEditableText().toString().trim());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}

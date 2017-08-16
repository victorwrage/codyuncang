package com.zdv.codyuncang;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.device.DeviceManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.zdv.codyuncang.cus_view.ProgressBarItem;
import com.zdv.codyuncang.present.QueryPresent;
import com.zdv.codyuncang.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseActivity extends FragmentActivity {
    protected static final String PAGE_0 = "page_0";
    protected static final String PAGE_1 = "page_1";
    protected Context context;
    ProgressDialog progressDialog;
    DeviceManager deviceManager;
    QueryPresent present;
    Utils util;

    protected final int EXIT_CONFIRM = 20;

    boolean stop = false;//网络请求标志位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        deviceManager = new DeviceManager();
        util = Utils.getInstance();
        present = QueryPresent.getInstance(BaseActivity.this);
    }

    public String currentDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    protected void showWaitDialog(String tip){
        ProgressBarItem.show(BaseActivity.this,tip,false,null);
    }
    protected void hideWaitDialog() {
        ProgressBarItem.hideProgress();
    }

    protected void showWaitDialogCacel(String tip) {
        progressDialog = new ProgressDialog(BaseActivity.this);
        progressDialog.setMessage(tip);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        progressDialog.setOnDismissListener((dia) -> onProgressDissmiss());
        progressDialog.show();

    }

    /**
     *
     */
    protected void onProgressDissmiss() {
        stop = true;
    }


    protected void showDialog(int type, String title, String tip, String posbtn, String negbtn) {
        AlertDialog dialog = null;
        if (negbtn == null) {
            dialog = new AlertDialog.Builder(this).setTitle(title)
                    .setMessage(tip)
                    .setPositiveButton(posbtn, (dia, which) -> confirm(type, dia))
                    .create();
        } else {
            dialog = new AlertDialog.Builder(this).setTitle(title)
                    .setMessage(tip)
                    .setPositiveButton(posbtn, (dia, which) -> confirm(type, dia))
                    .setNegativeButton(negbtn, (dia, which) -> cancel(type, dia)).create();
        }
        dialog.setCancelable(false);
        dialog.show();
    }

    protected void confirm(int type, DialogInterface dialog) {
        dialog.dismiss();
    }

    protected void cancel(int type, DialogInterface dialog) {
        dialog.dismiss();
    }
}

package com.zdv.codyuncang.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.zdv.codyuncang.R;
import com.zdv.codyuncang.cus_view.ProgressBarItem;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class BaseFragment extends Fragment {
    protected Context context;
    protected final String COOKIE_KEY = "cookie";
    protected long isFar = 5000;//5公里
    protected String[] scopes = new String[]{"交易成功", "交易失败", "已撤销", "已冲正", "待支付"};


    public String currentDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }


    protected void showWaitDialog(String tip){
        ProgressBarItem.show(getActivity(),tip,false,null);
    }
    protected void hideWaitDialog() {
        ProgressBarItem.hideProgress();
    }

    protected void startLoading(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        imageView.startAnimation(rotate);
    }

    protected void stopLoading(ImageView imageView) {
        imageView.setVisibility(View.GONE);
        imageView.clearAnimation();
    }


    AlertDialog dialog;
    protected void showDialog(int type, String title, String tip, String posbtn, String negbtn) {
        dialog = null;
        if (negbtn == null) {
            dialog = new AlertDialog.Builder(getActivity()).setTitle(title)
                    .setMessage(tip)
                    .setPositiveButton(posbtn, (dia, which) -> confirm(type, dia))
                    .create();
        } else {
            dialog = new AlertDialog.Builder(getActivity()).setTitle(title)
                    .setMessage(tip)
                    .setPositiveButton(posbtn, (dia, which) -> confirm(type, dia))
                    .setNegativeButton(negbtn, (dia, which) -> cancel(type, dia)).create();
        }
        dialog.setCancelable(false);
        dialog.show();

    }

    protected void cancel(int type, DialogInterface dia) {
        dialog.dismiss();
    }

    protected void confirm(int type, DialogInterface dia) {
        dialog.dismiss();
    }



}

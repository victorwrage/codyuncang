package com.zdv.codyuncang.cus_view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;
import com.zdv.codyuncang.R;

/**
 * TODO: document your custom view class.
 */
public class ProgressBarItem extends Dialog {
    static AVLoadingIndicatorView avi;
    private static ProgressBarItem progressBarItem;
    public ProgressBarItem(Context context)
    {
        super(context);
        init( context);
    }

    public ProgressBarItem(Context context, int theme) {
        super(context, theme);
    }

    /**
     * 当窗口焦点改变时调用
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        avi = (AVLoadingIndicatorView) findViewById(R.id.myloading_image_id);
        // 获取ImageView上的动画背景
    //    AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
        // 开始动画
      //  spinner.start();
        startAnim();
    }

    static void startAnim(){
        avi.show();
        // or avi.smoothToShow();
    }

    static void stopAnim(){
        if(avi!=null) {
            avi.hide();
        }
        // or avi.smoothToHide();
    }

    public static void  hideProgress(){
        if(progressBarItem!=null) {
            stopAnim();
            progressBarItem.dismiss();
            progressBarItem = null;
        }
    }

    /**
     * 弹出自定义ProgressDialog
     *
     * @param context
     *            上下文
     * @param message
     *            提示
     * @param cancelable
     *            是否按返回键取消
     * @param cancelListener
     *            按下返回键监听
     * @return
     */
    public static ProgressBarItem show(Context context, CharSequence message, boolean cancelable, OnCancelListener cancelListener) {
        if(progressBarItem==null) {
            progressBarItem = new ProgressBarItem(context, R.style.Custom_Progress);
        }
        progressBarItem.setTitle("");
        progressBarItem.setContentView(R.layout.progress_bar);
        if (message == null || message.length() == 0) {
            progressBarItem.findViewById(R.id.mylaodint_text_id).setVisibility(View.GONE);
        } else {
            TextView txt = (TextView) progressBarItem.findViewById(R.id.mylaodint_text_id);
            txt.setText(message);
        }
        // 按返回键是否取消
        progressBarItem.setCancelable(cancelable);
        // 监听返回键处理
        progressBarItem.setOnCancelListener(cancelListener);
        // 设置居中
        progressBarItem.getWindow().getAttributes().gravity = Gravity.CENTER;
        WindowManager.LayoutParams lp = progressBarItem.getWindow().getAttributes();
        // 设置背景层透明度
        lp.dimAmount = 0.2f;
        progressBarItem.getWindow().setAttributes(lp);
        // dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        progressBarItem.show();
        return progressBarItem;
    }

    private void init(Context context)
    {
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View progressBarItemView=inflater.inflate(R.layout.progress_bar, null);
        setContentView(progressBarItemView);
        start();
    }

    public void start(){
        ImageView progressImageView=(ImageView)this.findViewById(R.id.myloading_image_id);
        AnimationDrawable animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        animationDrawable.start();
    }

    public void stop(){
        ImageView progressImageView=(ImageView)this.findViewById(R.id.myloading_image_id);
        AnimationDrawable animationDrawable = (AnimationDrawable) progressImageView.getDrawable();
        animationDrawable.stop();
    }


}

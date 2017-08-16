/**
 * @Filename UncaughtExceptionCatcher.java
 * @Description TODO
 * @Version 1.0
 * @Author xiaoyl
 * @Creation 2013-8-10 上午10:30:59
 * @Copyright Copyright © 2009 - 2013 Victor.All Rights Reserved.
 **/
package com.zdv.codyuncang;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.socks.library.KLog;
import com.zdv.codyuncang.utils.D2000V1ScanInitUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @ClassName UncaughtExceptionCatcher
 * @Description TODO
 * @Version 1.0
 * @Creation 2013-8-10 上午10:30:59
 * @Mender xiaoyl
 **/
public class UncaughtExceptionCatcher implements UncaughtExceptionHandler {
    private Context iContext;
    private UncaughtExceptionHandler mDefaultHandler;
    /**
     * 使用Properties来保存设备的信息和错误堆栈信息
     */
    private Properties mDeviceCrashInfo = new Properties();
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    private static final String STACK_TRACE = "STACK_TRACE";
    /**
     * 错误报告文件的扩展名
     */
    private static final String CRASH_REPORTER_EXTENSION = ".cr";

    static private UncaughtExceptionCatcher instance;

    // private ExceptionHappenCaller iCaller;

    private UncaughtExceptionCatcher(Context aContext) {
        iContext = aContext;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void release() {
        // iCaller = null;
        instance = null;
        if (mDeviceCrashInfo != null) {
            mDeviceCrashInfo.clear();
        }
        mDeviceCrashInfo = null;
        mDefaultHandler = null;
    }

    final static public UncaughtExceptionCatcher getInstance(
            Context aApplicationContext) {
        if (instance == null) {
            instance = new UncaughtExceptionCatcher(aApplicationContext);
        }
        return instance;
    }

    /**
     * 设置异常发生时的响应器
     */
    /*
     * public void setExceptionHappenCaller(ExceptionHappenCaller aCaller) {
	 * iCaller = aCaller; }
	 */
    public void uncaughtException(final Thread thread, final Throwable ex) {

        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            // mDefaultHandler.uncaughtException(thread, ex);
        } else {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    // Toast.makeText(iContext, "程序出错啦:" + msg,
                    // Toast.LENGTH_LONG).show();
					/*
					 * if (iCaller != null) { iCaller.exceptionHappen(iContext,
					 * ex , UncaughtExceptionCatcher.this); } else {
					 */
                    //VLog.e(ex.getMessage());

                    Toast.makeText(iContext, "请重新启动APP!",
                            Toast.LENGTH_LONG).show();
                    if( D2000V1ScanInitUtils.getInstance()!=null) {
                        D2000V1ScanInitUtils.getInstance().close2();
                    }
                    new Handler().postDelayed(() -> {
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(10);
                    }, 2000);

                    Looper.loop();
                    if (Looper.myLooper() != null)
                        Looper.myLooper().quit();
                }

            }.start();
            // Sleep一会后结束程序

          /*  android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);*/
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return true;
        }

        // 收集设备信息
        collectCrashDeviceInfo(iContext);
        // //保存错误报告文件
        String crashFileName = saveCrashInfoToFile(ex);
        // //发送错误报告到服务器
        // sendCrashReportsToServer(iContext);
        return true;
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return
     */
    private String saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }

        String result = info.toString();
        printWriter.close();
        mDeviceCrashInfo.put(STACK_TRACE, result);
        KLog.v("error was = " + result);
        try {
            long timestamp = System.currentTimeMillis();
            // String fileName = "crash-" + timestamp +
            // CRASH_REPORTER_EXTENSION;
            String fileName = "crash-" + CRASH_REPORTER_EXTENSION;
            iContext.deleteFile(fileName);
            FileOutputStream trace = iContext.openFileOutput(fileName,
                    Context.MODE_PRIVATE);
            mDeviceCrashInfo.store(trace, null);
            trace.flush();
            trace.close();
            return fileName;
        } catch (Exception e) {
            KLog.e("an error occured while writing report file..." + e);
        }
        return null;
    }

    /**
     * 获取错误报告文件名
     *
     * @param ctx
     * @return
     */
    public String[] getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        return filesDir.list(filter);
    }

    /**
     * 收集程序崩溃的设备信息
     *
     * @param ctx
     */
    private void collectCrashDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null
                        ? "not set"
                        : pi.versionName);
                mDeviceCrashInfo.put(VERSION_CODE,
                        String.valueOf(pi.versionCode));
            }
        } catch (NameNotFoundException e) {
            KLog.e("Error while collect package info" + e);
        }
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mDeviceCrashInfo.put(field.getName(),
                        String.valueOf(field.get(null)));
            } catch (Exception e) {
                KLog.e("Error while collect crash info" + e);
            }
        }
    }
}

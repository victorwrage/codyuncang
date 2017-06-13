
package com.zdv.codyuncang.zbar.decode;

import android.os.Handler;
import android.os.Looper;

import com.zdv.codyuncang.CaptureActivity;

import java.util.concurrent.CountDownLatch;

final class DecodeThread extends Thread {

  private final CaptureActivity activity;
  private Handler handler;
  private final CountDownLatch handlerInitLatch;

  public DecodeThread(CaptureActivity activity)
  {
    this.activity = activity;
    handlerInitLatch = new CountDownLatch(1);
  }


    Handler getHandler() {
    try 
    {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new DecodeHandler(activity);
    handlerInitLatch.countDown();
    Looper.loop();
  }

}

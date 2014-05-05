package com.clover.kitchenscreen;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PrintService extends Service {
  private static final Handler handler = new Handler(Looper.getMainLooper());

  public class PrintBinder extends Binder {
    PrintService getService() {
      return PrintService.this;
    }
  }

  public static interface Listener {
    void onBitmapReceived(Bitmap bitmap);
  }

  public static final String ACTION_START = "com.clover.kitchenscreen.START";
  public static final String ACTION_STOP = "com.clover.kitchenscreen.STOP";


  private static final String TAG = PrintService.class.getName();

  private final Executor exec = Executors.newSingleThreadExecutor();
  private final IBinder binder = new PrintBinder();
  private PrintServer server;
  private Listener listener;

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (ACTION_START.equals(intent.getAction())) {
      startServer();
    } else if (ACTION_STOP.equals(intent.getAction())) {
      stopServer();
    }

    return START_NOT_STICKY;
  }

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public void removeListener() {
    listener = null;
  }

  private void startServer() {
    if (server == null) {
      server = new PrintServer(PrintServer.PORT) {
        @Override
        protected void onReceiptBitmapReceived(final Bitmap bitmap) {
          if (listener != null) {
            handler.post(new Runnable() {
              @Override
              public void run() {
                listener.onBitmapReceived(bitmap);
              }
            });
          }
        }
      };
      server.start();

      Notification n = new Notification.Builder(this)
          .setSmallIcon(android.R.drawable.stat_notify_sync)
          .setContentTitle("Clover Kitchen Screen Print Service")
          .setContentText("Waiting for print jobs ...")
          .build();

      startForeground(100, n);
      Log.i(TAG, "server started at address: " + server.getAddress());
    } else {
      Log.i(TAG, "server already started at address: " + server.getAddress());
    }
  }

  private void stopServer() {
    if (server != null) {
      try {
        server.stop();
        Log.i(TAG, "server stopped at address: " + server.getAddress());
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        server = null;
      }
    } else {
      Log.i(TAG, "server not running");
    }
    stopForeground(true);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  @Override
  public void onDestroy() {
    stopServer();
    super.onDestroy();
  }
}

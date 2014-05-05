package com.clover.kitchenscreen;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.IBinder;
import android.util.Log;

public class RegisterService extends Service {
  private static final String TAG = RegisterService.class.getName();

  public static final String ACTION_REGISTER = "com.clover.kitchenscreen.REGISTER";
  public static final String ACTION_UNREGISTER = "com.clover.kitchenscreen.UNREGISTER";

  private String serviceName = null;

  private NsdManager.RegistrationListener listener = null;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      return START_NOT_STICKY;
    }

    if (ACTION_REGISTER.equals(intent.getAction())) {
      NsdServiceInfo serviceInfo  = new NsdServiceInfo();

      serviceInfo.setServiceName("CloverKitchenScreen");
      serviceInfo.setServiceType("_http._tcp.");
      serviceInfo.setPort(PrintServer.PORT);

      listener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
          Log.i(TAG, "registered");
          serviceName = NsdServiceInfo.getServiceName();
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
          Log.i(TAG, "registration failed: " + errorCode);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
          Log.i(TAG, "unregistered");
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
          Log.i(TAG, "unregister failed: " + errorCode);
        }
      };

      NsdManager nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

      nsdManager.registerService(
          serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener);

      Notification n = new Notification.Builder(this)
          .setSmallIcon(android.R.drawable.stat_notify_sync)
          .setContentTitle("Clover Kitchen Screen Discover Service")
          .setContentText("Registered ...")
          .build();

      startForeground(101, n);

    } else if (ACTION_UNREGISTER.equals(intent.getAction())) {
      NsdManager nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
      nsdManager.unregisterService(listener);
      listener = null;

      stopForeground(true);
    }

    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}

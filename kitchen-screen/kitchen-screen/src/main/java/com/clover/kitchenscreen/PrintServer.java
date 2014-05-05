package com.clover.kitchenscreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

public abstract class PrintServer extends WebSocketServer {
  public static final int PORT = 8333;

  private static final String TAG = PrintServer.class.getName();

  public PrintServer(int port) {
    super(new InetSocketAddress(getIpAddress(), port));
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    Log.i(TAG, "open, connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    Log.i(TAG, String.format("close, code: %d, reason: %s, remote: %b", code, reason, remote));
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    Log.e(TAG, "SHOULDN'T BE CALLED message, connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + ", message: " + message);
  }

  @Override
  public void onMessage(WebSocket conn, ByteBuffer message) {
    Log.i(TAG, "message, connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + ", message: " + message);

    if (message.remaining() == 0) {
      Log.i(TAG, "message, received ping");
      return;
    }

    byte[] bytes = new byte[message.remaining()];
    message.get(bytes, 0, bytes.length);

    Log.i(TAG, String.format("message, read %d bytes", bytes.length));

    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    onReceiptBitmapReceived(bitmap);
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    Log.e(TAG, "error", ex);
  }

  protected abstract void onReceiptBitmapReceived(Bitmap bitmap);

  private static String getIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
            String ipAddress = inetAddress.getHostAddress().toString();
            return ipAddress;
          }
        }
      }
    } catch (SocketException ex) {
      Log.e(TAG, "unable to get IP address", ex);
    }
    return null;
  }
}

package com.clover.kitchenscreen;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ScreenActivity extends Activity implements ServiceConnection {
  private static final String TAG = ScreenActivity.class.getName();

  private PrintService printService = null;

  private ListView receiptList;
  private ReceiptAdapter receiptAdapter;
  private TextView unreadText;
  private ObjectAnimator unreadAnimator;
  private View headerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_screen);

    receiptList = (ListView) findViewById(R.id.list_receipts);
    receiptAdapter = new ReceiptAdapter(this);
    receiptList.setAdapter(receiptAdapter);
    receiptList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        receiptAdapter.onItemClicked(position);
        updateHeader();
      }
    });
    unreadText = (TextView) findViewById(R.id.text_unread);

//    final Property<TextView, Integer> property = new Property<TextView, Integer>(int.class, "background") {
//      @Override
//      public Integer get(TextView object) {
//        return object.getCurrentTextColor();
//      }
//
//      @Override
//      public void set(TextView object, Integer value) {
//        object.setTextColor(value);
//      }
//    };
//
//    unreadAnimator = ObjectAnimator.ofInt(unreadText, property, Color.RED);
//    unreadAnimator.setDuration(250L);
//    unreadAnimator.setEvaluator(new ArgbEvaluator());
//    unreadAnimator.setInterpolator(new LinearInterpolator());
//
//    unreadAnimator.setRepeatCount(ObjectAnimator.INFINITE);
//    unreadAnimator.setRepeatMode(ObjectAnimator.REVERSE);

    headerLayout = findViewById(R.id.layout_header);
    unreadAnimator = ObjectAnimator.ofObject(headerLayout, "backgroundColor", new ArgbEvaluator(), 0xFFFF0000, 0xFF34763B);
    unreadAnimator.setDuration(250L);
    unreadAnimator.setInterpolator(new LinearInterpolator());

    unreadAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    unreadAnimator.setRepeatMode(ObjectAnimator.REVERSE);

    updateHeader();
  }

  @Override
  protected void onResume() {
    super.onResume();

    Intent printIntent = new Intent(this, PrintService.class);
    printIntent.setAction(PrintService.ACTION_START);
    startService(printIntent);

    bindService(printIntent, this, BIND_AUTO_CREATE);
  }

  @Override
  protected void onPause() {
    Intent registerIntent = new Intent(this, RegisterService.class);
    registerIntent.setAction(RegisterService.ACTION_UNREGISTER);
    startService(registerIntent);

    Intent printIntent = new Intent(this, PrintService.class);
    printIntent.setAction(PrintService.ACTION_STOP);
    startService(printIntent);

    unbindService(this);

    super.onPause();
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder binder) {
    PrintService.PrintBinder printBinder = (PrintService.PrintBinder) binder;
    printService = printBinder.getService();

    printService.setListener(new PrintService.Listener() {
      @Override
      public void onBitmapReceived(Bitmap bitmap) {
        receiptAdapter.addReceipt(bitmap);
        updateHeader();
        alart();
      }
    });


    Intent registerIntent = new Intent(this, RegisterService.class);
    registerIntent.setAction(RegisterService.ACTION_REGISTER);
    startService(registerIntent);
  }

  private void alart() {
    final MediaPlayer player = MediaPlayer.create(this, R.raw.dotmatrix);
    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        player.release();
      }
    });
    player.start();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    if (printService != null) {
      printService.removeListener();
      printService = null;
    }
  }

  private void updateHeader() {
    int unreadCount = receiptAdapter.getUnreadCount();
    unreadText.setText(String.format("Unread receipts: %d", unreadCount));
    if (unreadCount > 0) {
      unreadText.setTypeface(null, Typeface.BOLD);
      unreadAnimator.start();
    } else {
      unreadAnimator.cancel();
      unreadText.setTypeface(null, Typeface.NORMAL);
      headerLayout.setBackgroundColor(Color.parseColor("#34763B"));
    }
  }
}

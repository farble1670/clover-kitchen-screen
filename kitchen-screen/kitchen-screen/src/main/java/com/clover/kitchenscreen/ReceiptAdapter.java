package com.clover.kitchenscreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiptAdapter extends BaseAdapter {
  private static class ReceiptItem {
    final Bitmap bitmap;
    final long time;

    boolean read;

    ReceiptItem(Bitmap bitmap) {
      this.bitmap = bitmap;
      this.read = false;
      this.time = System.currentTimeMillis();
    }
  }
  private final List<ReceiptItem> receipts = new ArrayList<ReceiptItem>();
  private final Context context;

  public ReceiptAdapter(Context context) {
    this.context = context;
  }

  @Override
  public int getCount() {
    return receipts.size();
  }

  @Override
  public Object getItem(int position) {
    return receipts.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      view = LayoutInflater.from(context).inflate(R.layout.item_receipt, parent, false);
    }

    ImageView receiptImage = (ImageView) view.findViewById(R.id.image_receipt);
    ReceiptItem item = (ReceiptItem) getItem(position);
    receiptImage.setImageBitmap(item.bitmap);

    TextView dateTimeText = (TextView) view.findViewById(R.id.text_time);
    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date(item.time));
    dateTimeText.setText(currentDateTimeString);

    CheckBox readCheck = (CheckBox) view.findViewById(R.id.check_read);
    readCheck.setChecked(item.read);

    return view;
  }

  public void addReceipt(Bitmap receiptBitmap) {
    if (receipts.size() == 10) {
      receipts.remove(receipts.size() - 1);
    }
    receipts.add(0, new ReceiptItem(receiptBitmap));
    notifyDataSetChanged();
  }

  protected void onItemClicked(int position) {
    ReceiptItem item = (ReceiptItem) getItem(position);
    item.read = !item.read;
    notifyDataSetChanged();
  }

  public int getUnreadCount() {
    int count = 0;
    for (ReceiptItem item: receipts) {
      if (!item.read) {
        count++;
      }
    }

    return count;
  }
}

package com.github.frimtec.android.pikettassist.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;

public class AutoStart extends BroadcastReceiver {
  @SuppressLint("UnsafeProtectedBroadcastReceiver")
  @Override
  public void onReceive(Context context, Intent intent) {
    NotificationHelper.registerChannel(context);
    context.startService(new Intent(context, PikettService.class));
  }
}

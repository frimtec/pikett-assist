package com.github.frimtec.android.pikettassist.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.Sms;

public final class SmsHelper {

  private final static String TAG = "SmsHelper";

  private SmsHelper() {
  }

  public static Sms getSmsFromIntent(Intent intent) {
    Bundle bundle = intent.getExtras();
    return new Sms(bundle.getString(Intent.EXTRA_PHONE_NUMBER), bundle.getString(Intent.EXTRA_TEXT));
  }

  public static void confirmSms(Context context, String confirmText, String number) {
    Intent sendSmsIntent = new Intent("com.github.frimtec.android.SEND_SMS");
    sendSmsIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, number);
    sendSmsIntent.putExtra(Intent.EXTRA_TEXT, confirmText);
    Log.d(TAG, "Broadcast: SMS send");
    context.sendOrderedBroadcast(sendSmsIntent, null);
  }
}

package com.github.frimtec.android.pikettassist.service.confirmation;

import android.util.Log;

import com.github.frimtec.android.securesmsproxyapi.Sms;

public class NoConfirmStrategy implements ConfirmStrategy {

  private static final String TAG = NoConfirmStrategy.class.getSimpleName();

  @Override
  public boolean confirm(Sms receivedAlarmSms) {
    Log.i(TAG, "Alert acknowledgement disabled.");
    return true;
  }
}

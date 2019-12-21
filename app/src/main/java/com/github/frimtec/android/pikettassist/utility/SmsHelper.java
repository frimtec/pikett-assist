package com.github.frimtec.android.pikettassist.utility;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;

public final class SmsHelper {

  private static final String TAG = "SmsHelper";

  private SmsHelper() {
  }

  public static List<Sms> getSmsFromIntent(Context context, Intent intent) {
    SecureSmsProxyFacade s2msp = SecureSmsProxyFacade.instance(context);
    return s2msp.extractReceivedSms(intent, SharedState.getSmsAdapterSecret(context));
  }

  public static void confirmSms(Context context, String confirmText, String number, Integer subscriptionId) {
    SecureSmsProxyFacade s2msp = SecureSmsProxyFacade.instance(context);
    Log.d(TAG, "Send SMS to SIM with subscription: " + subscriptionId);
    com.github.frimtec.android.securesmsproxyapi.Sms sms = new Sms(number, confirmText, subscriptionId);
    s2msp.sendSms(sms, SharedState.getSmsAdapterSecret(context));
  }
}

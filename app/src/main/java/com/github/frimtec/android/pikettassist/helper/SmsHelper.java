package com.github.frimtec.android.pikettassist.helper;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;

public final class SmsHelper {

  private SmsHelper() {
  }

  public static List<Sms> getSmsFromIntent(Context context, Intent intent) {
    SecureSmsProxyFacade s2smp = SecureSmsProxyFacade.instance(context);
    return s2smp.extractReceivedSms(intent, SharedState.getSmsAdapterSecret(context));
  }

  public static void confirmSms(Context context, String confirmText, String number) {
    SecureSmsProxyFacade s2smp = SecureSmsProxyFacade.instance(context);
    com.github.frimtec.android.securesmsproxyapi.Sms sms = new Sms(number, confirmText);
    s2smp.sendSms(sms, SharedState.getSmsAdapterSecret(context));
  }
}

package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;

public final class SmsService {

  private static final String TAG = "SmsService";

  private final Context context;

  public SmsService(Context context) {
    this.context = context;
  }

  public List<Sms> getSmsFromReceivedIntent(Intent intent) {
    SecureSmsProxyFacade s2msp = SecureSmsProxyFacade.instance(this.context);
    return s2msp.extractReceivedSms(intent, ApplicationState.instance().getSmsAdapterSecret());
  }

  public void sendSms(String confirmText, String number, Integer subscriptionId) {
    SecureSmsProxyFacade s2msp = SecureSmsProxyFacade.instance(this.context);
    Log.d(TAG, "Send SMS to SIM with subscription: " + subscriptionId);
    Sms sms = new Sms(number, confirmText, subscriptionId);
    s2msp.sendSms(sms, ApplicationState.instance().getSmsAdapterSecret());
  }
}

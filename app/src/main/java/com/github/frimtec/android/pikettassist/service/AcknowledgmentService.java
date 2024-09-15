package com.github.frimtec.android.pikettassist.service;

import android.content.Context;

import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.securesmsproxyapi.Sms;

public class AcknowledgmentService {

  private final Context context;
  private final SmsService smsService;

  public AcknowledgmentService(Context context, SmsService smsService) {
    this.context = context;
    this.smsService = smsService;
  }

  public void acknowledge(Sms receivedSms) {
    ApplicationPreferences applicationPreferences = ApplicationPreferences.instance();
    if (applicationPreferences.getSendConfirmSms(context)) {
      this.smsService.sendSms(
          applicationPreferences.getSmsConfirmText(context),
          receivedSms.getNumber(),
          receivedSms.getSubscriptionId()
      );
    }
  }
}

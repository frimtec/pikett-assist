package com.github.frimtec.android.pikettassist.service.confirmation;

import com.github.frimtec.android.securesmsproxyapi.Sms;

@FunctionalInterface
public interface ConfirmStrategy {
  boolean confirm(Sms receivedAlarmSms);
}

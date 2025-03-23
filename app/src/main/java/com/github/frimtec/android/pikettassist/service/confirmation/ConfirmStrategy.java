package com.github.frimtec.android.pikettassist.service.confirmation;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;

@FunctionalInterface
public interface ConfirmStrategy {

  default boolean confirm(List<Sms> receivedAlarmSms) {
    boolean success = false;
    for (Sms sms : receivedAlarmSms) {
      if (confirm(sms)) {
        success = true;
      }
    }
    return success;
  }

  boolean confirm(Sms receivedAlarmSms);
}

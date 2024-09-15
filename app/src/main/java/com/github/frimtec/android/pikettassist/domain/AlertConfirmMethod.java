package com.github.frimtec.android.pikettassist.domain;

public enum AlertConfirmMethod {
  NO_ACKNOWLEDGE(false),
  SMS_STATIC_TEXT(true),
  SMS_DYNAMIC_TEXT(true);

  private final boolean sms;

  AlertConfirmMethod(boolean sms) {
    this.sms = sms;
  }

  public boolean isSms() {
    return this.sms;
  }
}

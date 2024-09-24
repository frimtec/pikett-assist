package com.github.frimtec.android.pikettassist.domain;

public enum AlertConfirmMethod {
  NO_ACKNOWLEDGE(false, false),
  SMS_STATIC_TEXT(true, false),
  SMS_DYNAMIC_TEXT(true, false),
  INTERNET_FACT24_ENS(false, true);

  private final boolean sms;
  private final boolean internet;

  AlertConfirmMethod(boolean sms, boolean internet) {
    this.sms = sms;
    this.internet = internet;
  }

  public boolean isSms() {
    return sms;
  }

  public boolean isInternet() {
    return internet;
  }
}

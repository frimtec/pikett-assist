package com.github.frimtec.android.pikettassist.domain;

public enum Action {
  REFRESH("com.github.frimtec.android.pikettassist.refresh"),
  SMS_RECEIVED("com.github.frimtec.android.securesmsproxy.SMS_RECEIVED");

  private final String id;

  Action(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}

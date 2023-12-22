package com.github.frimtec.android.pikettassist.domain;

import android.text.TextUtils;

import java.time.Instant;

public record TestAlarm(
    TestAlarmContext context,
    Instant receivedTime,
    OnOffState alertState,
    String message,
    String alias
) {

  public String name() {
    return TextUtils.isEmpty(this.alias) ? context.context() : this.alias;
  }
}

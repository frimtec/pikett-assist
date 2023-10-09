package com.github.frimtec.android.pikettassist.domain;

import androidx.annotation.NonNull;

import java.util.Objects;

public record TestAlarmContext(String context) {

  public TestAlarmContext {
    Objects.nonNull(context);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestAlarmContext testAlarmContext = (TestAlarmContext) o;
    return Objects.equals(context, testAlarmContext.context);
  }

  @NonNull
  @Override
  public String toString() {
    return context;
  }
}

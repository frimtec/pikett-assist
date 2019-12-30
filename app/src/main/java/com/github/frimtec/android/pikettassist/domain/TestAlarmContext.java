package com.github.frimtec.android.pikettassist.domain;

import androidx.annotation.NonNull;

import java.util.Objects;

public class TestAlarmContext {

  private final String context;

  public TestAlarmContext(String context) {
    Objects.nonNull(context);
    this.context = context;
  }

  public String getContext() {
    return context;
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

  @Override
  public int hashCode() {
    return Objects.hash(context);
  }

  @NonNull
  @Override
  public String toString() {
    return context;
  }
}

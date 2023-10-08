package com.github.frimtec.android.pikettassist.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public record Alert(
    long id,
    Instant startTime,
    Instant confirmTime,
    boolean confirmed,
    Instant endTime,
    List<AlertCall> calls
) {

  public Alert(
      long id,
      Instant startTime,
      Instant confirmTime,
      boolean confirmed,
      Instant endTime,
      List<AlertCall> calls
  ) {
    this.id = id;
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(calls);
    this.startTime = startTime;
    this.confirmTime = confirmTime;
    this.confirmed = confirmed;
    this.endTime = endTime;
    this.calls = new LinkedList<>(calls);
    this.calls.sort(Comparator.comparing(AlertCall::time));
  }

  @Override
  public List<AlertCall> calls() {
    return Collections.unmodifiableList(calls);
  }

  public boolean isClosed() {
    return endTime != null;
  }

  public record AlertCall(Instant time, String message) {

    public AlertCall {
      Objects.requireNonNull(time);
      Objects.requireNonNull(message);
    }
  }
}

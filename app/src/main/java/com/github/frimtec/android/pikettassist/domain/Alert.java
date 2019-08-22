package com.github.frimtec.android.pikettassist.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Alert {

  private final long id;
  private final Instant startTime;
  private final Instant confirmTime;
  private final boolean confirmed;
  private final Instant endTime;
  private final List<AlertCall> calls;
  public Alert(long id, Instant startTime, Instant confirmTime, boolean confirmed, Instant endTime, List<AlertCall> calls) {
    this.id = id;
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(calls);
    this.startTime = startTime;
    this.confirmTime = confirmTime;
    this.confirmed = confirmed;
    this.endTime = endTime;
    this.calls = new LinkedList<>(calls);
    this.calls.sort(Comparator.comparing(AlertCall::getTime));
  }

  public long getId() {
    return id;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getConfirmTime() {
    return confirmTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public List<AlertCall> getCalls() {
    return Collections.unmodifiableList(calls);
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public boolean isClosed() {
    return endTime != null;
  }

  @Override
  public String toString() {
    return "Alert{" +
        "id=" + id +
        ", startTime=" + startTime +
        ", confirmTime=" + confirmTime +
        ", confirmed=" + confirmed +
        ", endTime=" + endTime +
        ", calls=" + calls +
        '}';
  }

  public static class AlertCall {
    private final Instant time;
    private final String message;

    public AlertCall(Instant time, String message) {
      Objects.requireNonNull(time);
      Objects.requireNonNull(message);
      this.time = time;
      this.message = message;
    }

    public Instant getTime() {
      return time;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return "AlertCall{" +
          "time=" + time +
          ", message='" + message + '\'' +
          '}';
    }
  }
}

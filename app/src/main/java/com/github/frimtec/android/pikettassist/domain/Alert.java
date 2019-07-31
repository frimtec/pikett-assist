package com.github.frimtec.android.pikettassist.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class Alert {

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

  private final long id;
  private final Instant startTime;
  private final Instant confirmTime;
  private final Instant endTime;
  private final List<AlertCall> calls;

  public Alert(long id, Instant startTime, Instant confirmTime, Instant endTime, List<AlertCall> calls) {
    this.id = id;
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(calls);
    this.startTime = startTime;
    this.confirmTime = confirmTime;
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
    return confirmTime != null;
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
        ", endTime=" + endTime +
        ", calls=" + calls +
        '}';
  }
}

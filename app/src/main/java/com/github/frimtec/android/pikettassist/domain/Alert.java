package com.github.frimtec.android.pikettassist.domain;

import java.time.LocalDateTime;
import java.util.*;

public class Alert {

  public static class AlertCall {
    private final LocalDateTime time;
    private final String message;

    public AlertCall(LocalDateTime time, String message) {
      Objects.requireNonNull(time);
      Objects.requireNonNull(message);
      this.time = time;
      this.message = message;
    }

    public LocalDateTime getTime() {
      return time;
    }

    public String getMessage() {
      return message;
    }
  }

  private final LocalDateTime startTime;
  private final LocalDateTime confirmTime;
  private final LocalDateTime endTime;
  private final List<AlertCall> calls;

  public Alert(LocalDateTime startTime, LocalDateTime confirmTime, LocalDateTime endTime, List<AlertCall> calls) {
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(calls);
    this.startTime = startTime;
    this.confirmTime = confirmTime;
    this.endTime = endTime;
    this.calls = new LinkedList<>(calls);
    this.calls.sort(Comparator.comparing(AlertCall::getTime));
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getConfirmTime() {
    return confirmTime;
  }

  public LocalDateTime getEndTime() {
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
}

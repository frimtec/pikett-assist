package com.github.frimtec.android.pikettassist.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class PikettShift {

  private static final Duration TIME_TOLLERANCE = Duration.ofMinutes(5);

  private final String title;
  private final Instant startTime;
  private final Instant endTime;

  public PikettShift(String title, Instant startTime, Instant endTime) {
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public String getTitle() {
    return title;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public static Instant now() {
    return LocalDateTime.now().toInstant(ZoneOffset.UTC);
  }

  public boolean isNow() {
    return isNow(now());
  }

  public boolean isNow(Instant now) {
    return !isInFuture(now) && !isOver(now);
  }

  public boolean isOver(Instant now) {
    return now.isAfter(getEndTime().plus(TIME_TOLLERANCE));
  }

  public boolean isInFuture(Instant now) {
    return now.isBefore(getStartTime().minus(TIME_TOLLERANCE)) ;
  }
}

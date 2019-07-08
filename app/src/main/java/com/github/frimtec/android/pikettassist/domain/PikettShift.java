package com.github.frimtec.android.pikettassist.domain;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PikettShift {

  private static final Duration TIME_TOLLERANCE = Duration.ofMinutes(5);

  private final long id;
  private final String title;
  private final Instant startTime;
  private final Instant endTime;

  public PikettShift(long id, String title, Instant startTime, Instant endTime) {
    this.id = id;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Instant getStartTime(boolean withTollerance) {
    return withTollerance ? startTime.minus(TIME_TOLLERANCE) : startTime;
  }

  public Instant getEndTime(boolean withTollerance) {
    return withTollerance ? endTime.plus(TIME_TOLLERANCE) : endTime;
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
    return now.isAfter(getEndTime(true));
  }

  public boolean isInFuture(Instant now) {
    return now.isBefore(getStartTime(true));
  }
}

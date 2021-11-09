package com.github.frimtec.android.pikettassist.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Shift {

  private final long id;
  private final String title;
  private final Instant startTime;
  private final Instant endTime;
  private final boolean confirmed;
  private final List<String> partners;

  public Shift(long id, String title, Instant startTime, Instant endTime, boolean confirmed, Collection<String> partners) {
    this.id = id;
    this.title = title;
    this.startTime = startTime;
    this.endTime = endTime;
    this.confirmed = confirmed;
    this.partners = new ArrayList<>(partners);
  }

  public static Instant now() {
    return Instant.now();
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Instant getStartTime(Duration prePostRuntime) {
    return getStartTime().minus(prePostRuntime);
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime(Duration prePostRuntime) {
    return getEndTime().plus(prePostRuntime);
  }

  public Instant getEndTime() {
    return endTime;
  }

  public boolean isNow(Duration prePostRunTime) {
    return isNow(now(), prePostRunTime);
  }

  public boolean isNow(Instant now, Duration prePostRunTime) {
    return !isInFuture(now, prePostRunTime) && !isOver(now, prePostRunTime);
  }

  public boolean isOver(Instant now, Duration prePostRunTime) {
    return now.isAfter(getEndTime(prePostRunTime));
  }

  public boolean isInFuture(Instant now, Duration prePostRunTime) {
    return now.isBefore(getStartTime(prePostRunTime));
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public List<String> getPartners() {
    return Collections.unmodifiableList(partners);
  }
}

package com.github.frimtec.android.pikettassist.domain;


import static org.assertj.core.api.Assertions.assertThat;

import static java.time.Duration.ofMinutes;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Supplier;

class ShiftTest {

  private static final Duration TIME_TOLERANCE = ofMinutes(5);

  @Test
  void getId() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), true, Collections.emptyList());

    assertThat(shift.getId()).isEqualTo(0L);
  }

  @Test
  void getTitle() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), true, Collections.emptyList());

    assertThat(shift.getTitle()).isEqualTo("Test");
  }

  @Test
  void isConfirmedForTrue() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), true, Collections.emptyList());

    assertThat(shift.isConfirmed()).isTrue();
  }

  @Test
  void isConfirmedForFalse() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), false, Collections.emptyList());

    assertThat(shift.isConfirmed()).isFalse();
  }

  @Test
  void isOver() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), true, Collections.emptyList());
    checkTime(() -> shift.isOver(now.minus(TIME_TOLERANCE).minusMillis(1), TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now.minus(TIME_TOLERANCE), TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now, TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)), TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1), TIME_TOLERANCE), true);
  }

  @Test
  void isInFuture() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), true, Collections.emptyList());
    checkTime(() -> shift.isInFuture(now.minus(TIME_TOLERANCE).minusMillis(1), TIME_TOLERANCE), true);
    checkTime(() -> shift.isInFuture(now.minus(TIME_TOLERANCE), TIME_TOLERANCE), false);
    checkTime(() -> shift.isInFuture(now, TIME_TOLERANCE), false);
    checkTime(() -> shift.isInFuture(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)), TIME_TOLERANCE), false);
    checkTime(() -> shift.isInFuture(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1), TIME_TOLERANCE), false);
  }

  @Test
  void isNow() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)), true, Collections.emptyList());
    checkTime(() -> shift.isNow(now.minus(TIME_TOLERANCE).minusMillis(1), TIME_TOLERANCE), false);
    checkTime(() -> shift.isNow(now.minus(TIME_TOLERANCE), TIME_TOLERANCE), true);
    checkTime(() -> shift.isNow(now, TIME_TOLERANCE), true);
    checkTime(() -> shift.isNow(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)), TIME_TOLERANCE), true);
    checkTime(() -> shift.isNow(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1), TIME_TOLERANCE), false);
  }

  private void checkTime(Supplier<Boolean> test, boolean expectedResult) {
    // act
    boolean result = test.get();

    // assert
    assertThat(result).isEqualTo(expectedResult);
  }
}

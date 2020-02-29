package com.github.frimtec.android.pikettassist.domain;


import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.threeten.bp.Duration.ofMinutes;

 class ShiftTest {

  private static final Duration TIME_TOLERANCE = Duration.ofMinutes(5);

  @Test
   void isOver() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)));
    checkTime(() -> shift.isOver(now.minus(TIME_TOLERANCE).minusMillis(1),TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now.minus(TIME_TOLERANCE), TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now, TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)), TIME_TOLERANCE), false);
    checkTime(() -> shift.isOver(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1), TIME_TOLERANCE), true);
  }

  @Test
   void isInFuture() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)));
    checkTime(() -> shift.isInFuture(now.minus(TIME_TOLERANCE).minusMillis(1), TIME_TOLERANCE), true);
    checkTime(() -> shift.isInFuture(now.minus(TIME_TOLERANCE), TIME_TOLERANCE), false);
    checkTime(() -> shift.isInFuture(now, TIME_TOLERANCE), false);
    checkTime(() -> shift.isInFuture(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)),TIME_TOLERANCE), false);
    checkTime(() -> shift.isInFuture(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1), TIME_TOLERANCE), false);
  }

  @Test
   void isNow() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)));
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

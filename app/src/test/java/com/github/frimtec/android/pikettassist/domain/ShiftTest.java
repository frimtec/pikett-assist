package com.github.frimtec.android.pikettassist.domain;


import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.function.Supplier;

import static com.github.frimtec.android.pikettassist.domain.Shift.TIME_TOLERANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.threeten.bp.Duration.ofMinutes;

 class ShiftTest {

  @Test
   void isOver() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)));
    checkTime(() -> shift.isOver(now.minus(TIME_TOLERANCE).minusMillis(1)), false);
    checkTime(() -> shift.isOver(now.minus(TIME_TOLERANCE)), false);
    checkTime(() -> shift.isOver(now), false);
    checkTime(() -> shift.isOver(now.plus(TIME_TOLERANCE).plus(ofMinutes(1))), false);
    checkTime(() -> shift.isOver(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1)), true);
  }

  @Test
   void isInFuture() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)));
    checkTime(() -> shift.isInFuture(now.minus(TIME_TOLERANCE).minusMillis(1)), true);
    checkTime(() -> shift.isInFuture(now.minus(TIME_TOLERANCE)), false);
    checkTime(() -> shift.isInFuture(now), false);
    checkTime(() -> shift.isInFuture(now.plus(TIME_TOLERANCE).plus(ofMinutes(1))), false);
    checkTime(() -> shift.isInFuture(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1)), false);
  }

  @Test
   void isNow() {
    Instant now = Shift.now();
    Shift shift = new Shift(0L, "Test", now, now.plus(ofMinutes(1)));
    checkTime(() -> shift.isNow(now.minus(TIME_TOLERANCE).minusMillis(1)), false);
    checkTime(() -> shift.isNow(now.minus(TIME_TOLERANCE)), true);
    checkTime(() -> shift.isNow(now), true);
    checkTime(() -> shift.isNow(now.plus(TIME_TOLERANCE).plus(ofMinutes(1))), true);
    checkTime(() -> shift.isNow(now.plus(TIME_TOLERANCE).plus(ofMinutes(1)).plusMillis(1)), false);
  }

  private void checkTime(Supplier<Boolean> test, boolean expectedResult) {
    // act
    boolean result = test.get();

    // assert
    assertThat(result).isEqualTo(expectedResult);
  }
}

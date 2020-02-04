package com.github.frimtec.android.pikettassist.ui.shifts;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftArrayAdapterTest {

  @Test
  void roundToDaysExact() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofDays(4))).isEqualTo(4);
  }

  @Test
  void roundToDaysRoundUp() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofDays(4).plusHours(12))).isEqualTo(5);
  }

  @Test
  void roundToDaysRoundDown() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofDays(4).plusHours(12).minusMinutes(1))).isEqualTo(4);
  }

  @Test
  void roundToDaysZero() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofMillis(0))).isEqualTo(0);
  }
}
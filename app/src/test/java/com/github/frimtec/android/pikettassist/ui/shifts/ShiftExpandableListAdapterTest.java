package com.github.frimtec.android.pikettassist.ui.shifts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.time.Duration;

class ShiftExpandableListAdapterTest {

  @Test
  void roundToDaysExact() {
    assertThat(ShiftExpandableListAdapter.roundToDays(Duration.ofDays(4))).isEqualTo(4);
  }

  @Test
  void roundToDaysRoundUp() {
    assertThat(ShiftExpandableListAdapter.roundToDays(Duration.ofDays(4).plusHours(12))).isEqualTo(5);
  }

  @Test
  void roundToDaysRoundDown() {
    assertThat(ShiftExpandableListAdapter.roundToDays(Duration.ofDays(4).plusHours(12).minusMinutes(1))).isEqualTo(4);
  }

  @Test
  void roundToDaysZero() {
    assertThat(ShiftExpandableListAdapter.roundToDays(Duration.ofMillis(0))).isEqualTo(0);
  }
}
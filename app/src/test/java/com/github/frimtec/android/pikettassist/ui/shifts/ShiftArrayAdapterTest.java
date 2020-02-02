package com.github.frimtec.android.pikettassist.ui.shifts;

import org.junit.Test;
import org.threeten.bp.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ShiftArrayAdapterTest {

  @Test
  public void roundToDaysExact() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofDays(4)), is(4));
  }

  @Test
  public void roundToDaysRoundUp() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofDays(4).plusHours(12)), is(5));
  }

  @Test
  public void roundToDaysRoundDown() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofDays(4).plusHours(12).minusMinutes(1)), is(4));
  }

  @Test
  public void roundToDaysZero() {
    assertThat(ShiftArrayAdapter.roundToDays(Duration.ofMillis(0)), is(0));
  }
}
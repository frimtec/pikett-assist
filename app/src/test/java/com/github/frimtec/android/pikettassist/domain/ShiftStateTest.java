package com.github.frimtec.android.pikettassist.domain;

import static com.github.frimtec.android.pikettassist.domain.OnOffState.OFF;
import static com.github.frimtec.android.pikettassist.domain.OnOffState.ON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class ShiftStateTest {

  @Test
  public void createWithShift() {
    Shift shift = mock(Shift.class);
    ShiftState shiftState = new ShiftState(shift);

    assertThat(shiftState.isOn()).isTrue();
    assertThat(shiftState.getState()).isEqualTo(ON);
    assertThat(shiftState.getShift()).isPresent();
    assertThat(shiftState.getShift().get()).isSameAs(shift);
  }

  @Test
  public void createWithStetOff() {
    ShiftState shiftState = new ShiftState(OFF);

    assertThat(shiftState.isOn()).isFalse();
    assertThat(shiftState.getState()).isEqualTo(OFF);
    assertThat(shiftState.getShift()).isNotPresent();
  }

  @Test
  public void createWithStetOn() {
    ShiftState shiftState = new ShiftState(ON);

    assertThat(shiftState.isOn()).isTrue();
    assertThat(shiftState.getState()).isEqualTo(ON);
    assertThat(shiftState.getShift()).isNotPresent();
  }

}
package com.github.frimtec.android.pikettassist.domain;

import static com.github.frimtec.android.pikettassist.domain.OnOffState.ON;

import java.util.Optional;

public class ShiftState {

  private final OnOffState state;
  private final Shift shift;

  public ShiftState(Shift shift) {
    this.state = ON;
    this.shift = shift;
  }

  public ShiftState(OnOffState state) {
    this.state = state;
    this.shift = null;
  }

  public boolean isOn() {
    return this.state == ON;
  }

  public OnOffState getState() {
    return this.state;
  }

  public Optional<Shift> getShift() {
    return Optional.ofNullable(this.shift);
  }
}

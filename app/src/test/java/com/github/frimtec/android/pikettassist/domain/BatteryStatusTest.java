package com.github.frimtec.android.pikettassist.domain;

import com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging.NO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BatteryStatusTest {

  @Test
  void getLevel() {
    BatteryStatus batteryStatus = new BatteryStatus(50, NO);
    assertThat(batteryStatus.getLevel()).isEqualTo(50);
  }

  @Test
  void getCharging() {
    BatteryStatus batteryStatus = new BatteryStatus(50, NO);
    assertThat(batteryStatus.getCharging()).isEqualTo(NO);
  }

  @Test
  void isCharging() {
    EnumSet<Charging> nonChargingStates = EnumSet.of(NO);
    nonChargingStates.forEach(state -> assertThat(state.isCharging()).isFalse());
    EnumSet.complementOf(nonChargingStates).forEach(state -> assertThat(state.isCharging()).isTrue());
  }
}
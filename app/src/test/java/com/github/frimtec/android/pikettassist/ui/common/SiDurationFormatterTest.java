package com.github.frimtec.android.pikettassist.ui.common;

import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.siFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

import org.junit.jupiter.api.Test;

import java.time.Duration;

class SiDurationFormatterTest {

  private static final UnitNameProvider UNIT_NAME_PROVIDER = siFormatter();

  @Test
  void toDurationStringDays() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("2d");
  }

  @Test
  void toDurationStringDay() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("1d");
  }

  @Test
  void toDurationStringHours() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("2h");
  }

  @Test
  void toDurationStringHour() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("1h");
  }

  @Test
  void toDurationStringMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("2m");
  }

  @Test
  void toDurationStringMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1), UNIT_NAME_PROVIDER)).isEqualTo("1m");
  }

  @Test
  void toDurationStringZeroMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0), UNIT_NAME_PROVIDER)).isEqualTo("0m");
  }
}
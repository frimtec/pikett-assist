package com.github.frimtec.android.pikettassist.ui.common;

import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.siFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static org.assertj.core.api.Assertions.assertThat;

class SiDurationFormatterTest {

  private static final UnitNameProvider UNIT_NAME_PROVIDER = siFormatter();

  @Test
  void toDurationStringDaysOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(12), UNIT_NAME_PROVIDER)).isEqualTo("3d");
  }

  @Test
  void toDurationStringDaysOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(11), UNIT_NAME_PROVIDER)).isEqualTo("2d");
  }

  @Test
  void toDurationStringDaysOnly() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("2d");
  }

  @Test
  void toDurationStringDaysAndHours() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("1d 2h");
  }

  @Test
  void toDurationStringOneDay() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("1d");
  }

  @Test
  void toDurationStringHoursOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(30), UNIT_NAME_PROVIDER)).isEqualTo("3h");
  }

  @Test
  void toDurationStringHoursOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(29), UNIT_NAME_PROVIDER)).isEqualTo("2h");
  }

  @Test
  void toDurationStringHoursOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("2h");
  }

  @Test
  void toDurationStringHoursAndMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("1h 2m");
  }

  @Test
  void toDurationStringOneHour() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(0), UNIT_NAME_PROVIDER)).isEqualTo("1h");
  }

  @Test
  void toDurationStringMinutesOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER)).isEqualTo("2m");
  }

  @Test
  void toDurationStringOneMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1), UNIT_NAME_PROVIDER)).isEqualTo("1m");
  }

  @Test
  void toDurationStringZeroMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0), UNIT_NAME_PROVIDER)).isEqualTo("0m");
  }
}
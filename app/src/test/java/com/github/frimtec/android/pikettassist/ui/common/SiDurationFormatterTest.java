package com.github.frimtec.android.pikettassist.ui.common;

import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

import org.junit.Test;
import org.threeten.bp.Duration;

import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.siFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SiDurationFormatterTest {

  private static final UnitNameProvider UNIT_NAME_PROVIDER = siFormatter();

  @Test
  public void toDurationStringDaysOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(12), UNIT_NAME_PROVIDER), is("3d"));
  }

  @Test
  public void toDurationStringDaysOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(11), UNIT_NAME_PROVIDER), is("2d"));
  }

  @Test
  public void toDurationStringDaysOnly() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER), is("2d"));
  }

  @Test
  public void toDurationStringDaysAndHours() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER), is("1d 2h"));
  }

  @Test
  public void toDurationStringOneDay() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER), is("1d"));
  }

  @Test
  public void toDurationStringHoursOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(30), UNIT_NAME_PROVIDER), is("3h"));
  }

  @Test
  public void toDurationStringHoursOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(29), UNIT_NAME_PROVIDER), is("2h"));
  }

  @Test
  public void toDurationStringHoursOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER), is("2h"));
  }

  @Test
  public void toDurationStringHoursAndMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(2), UNIT_NAME_PROVIDER), is("1h 2m"));
  }

  @Test
  public void toDurationStringOneHour() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(0), UNIT_NAME_PROVIDER), is("1h"));
  }

  @Test
  public void toDurationStringMinutesOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER), is("2m"));
  }

  @Test
  public void toDurationStringOneMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1), UNIT_NAME_PROVIDER), is("1m"));
  }

  @Test
  public void toDurationStringZeroMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0), UNIT_NAME_PROVIDER), is("0m"));
  }
}
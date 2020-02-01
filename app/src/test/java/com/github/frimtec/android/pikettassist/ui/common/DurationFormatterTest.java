package com.github.frimtec.android.pikettassist.ui.common;

import org.junit.Test;
import org.threeten.bp.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DurationFormatterTest {

  private static final DurationFormatter.UnitNameProvider UNIT_NAME_PROVIDER = new DurationFormatter.UnitNameProvider() {
    @Override
    public String getDay(boolean plural) {
      return plural ? "days" : "day";
    }

    @Override
    public String getHour(boolean plural) {
      return plural ? "hours" : "hour";
    }

    @Override
    public String getMinute(boolean plural) {
      return plural ? "minutes" : "minute";
    }

    @Override
    public String getAnd() {
      return "and";
    }
  };

  @Test
  public void toDurationStringDaysOnlyRoundUp() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(2).plusHours(12), UNIT_NAME_PROVIDER), is("3 days"));
  }

  @Test
  public void toDurationStringDaysOnlyRoundDown() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(2).plusHours(11), UNIT_NAME_PROVIDER), is("2 days"));
  }

  @Test
  public void toDurationStringDaysOnly() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(2).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER), is("2 days"));
  }

  @Test
  public void toDurationStringDaysAndHours() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(1).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER), is("1 day and 2 hours"));
  }

  @Test
  public void toDurationStringOneDay() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(1).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER), is("1 day"));
  }

  @Test
  public void toDurationStringHoursOnlyRoundUp() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(30), UNIT_NAME_PROVIDER), is("3 hours"));
  }

  @Test
  public void toDurationStringHoursOnlyRoundDown() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(29), UNIT_NAME_PROVIDER), is("2 hours"));
  }

  @Test
  public void toDurationStringHoursOnly() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(2), UNIT_NAME_PROVIDER), is("2 hours"));
  }

  @Test
  public void toDurationStringHoursAndMinutes() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(2), UNIT_NAME_PROVIDER), is("1 hour and 2 minutes"));
  }

  @Test
  public void toDurationStringOneHour() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(0), UNIT_NAME_PROVIDER), is("1 hour"));
  }

  @Test
  public void toDurationStringMinutesOnly() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(2), UNIT_NAME_PROVIDER), is("2 minutes"));
  }

  @Test
  public void toDurationStringOneMinute() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1), UNIT_NAME_PROVIDER), is("1 minute"));
  }

  @Test
  public void toDurationStringZeroMinutes() {
    assertThat(DurationFormatter.toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0), UNIT_NAME_PROVIDER), is("0 minutes"));
  }
}
package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.threeten.bp.Duration;

import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.translatedFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class TranslatedDurationFormatterTest {

  private final Context context = Mockito.mock(Context.class);
  private final UnitNameProvider unitNameProvider = translatedFormatter(context);

  @Before
  public void setupContext() {
    when(context.getString(R.string.units_days)).thenReturn("days");
    when(context.getString(R.string.units_day)).thenReturn("day");
    when(context.getString(R.string.units_hours)).thenReturn("hours");
    when(context.getString(R.string.units_hour)).thenReturn("hour");
    when(context.getString(R.string.units_minutes)).thenReturn("minutes");
    when(context.getString(R.string.units_minute)).thenReturn("minute");
    when(context.getString(R.string.units_and)).thenReturn("and");
  }

  @Test
  public void toDurationStringDaysOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(12), unitNameProvider), is("3 days"));
  }

  @Test
  public void toDurationStringDaysOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(11), unitNameProvider), is("2 days"));
  }

  @Test
  public void toDurationStringDaysOnly() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(2).plusMinutes(2), unitNameProvider), is("2 days"));
  }

  @Test
  public void toDurationStringDaysAndHours() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(2).plusMinutes(2), unitNameProvider), is("1 day and 2 hours"));
  }

  @Test
  public void toDurationStringOneDay() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(0).plusMinutes(2), unitNameProvider), is("1 day"));
  }

  @Test
  public void toDurationStringHoursOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(30), unitNameProvider), is("3 hours"));
  }

  @Test
  public void toDurationStringHoursOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(29), unitNameProvider), is("2 hours"));
  }

  @Test
  public void toDurationStringHoursOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(2), unitNameProvider), is("2 hours"));
  }

  @Test
  public void toDurationStringHoursAndMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(2), unitNameProvider), is("1 hour and 2 minutes"));
  }

  @Test
  public void toDurationStringOneHour() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(0), unitNameProvider), is("1 hour"));
  }

  @Test
  public void toDurationStringMinutesOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(2), unitNameProvider), is("2 minutes"));
  }

  @Test
  public void toDurationStringOneMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1), unitNameProvider), is("1 minute"));
  }

  @Test
  public void toDurationStringZeroMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0), unitNameProvider), is("0 minutes"));
  }
}
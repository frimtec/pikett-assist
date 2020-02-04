package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.threeten.bp.Duration;

import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.translatedFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


class TranslatedDurationFormatterTest {

  private final Context context = Mockito.mock(Context.class);
  private final UnitNameProvider unitNameProvider = translatedFormatter(context);

  @BeforeEach
  void setupContext() {
    when(context.getString(R.string.units_days)).thenReturn("days");
    when(context.getString(R.string.units_day)).thenReturn("day");
    when(context.getString(R.string.units_hours)).thenReturn("hours");
    when(context.getString(R.string.units_hour)).thenReturn("hour");
    when(context.getString(R.string.units_minutes)).thenReturn("minutes");
    when(context.getString(R.string.units_minute)).thenReturn("minute");
    when(context.getString(R.string.units_and)).thenReturn("and");
  }

  @Test
  void toDurationStringDaysOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(12), unitNameProvider)).isEqualTo("3 days");
  }

  @Test
  void toDurationStringDaysOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(11), unitNameProvider)).isEqualTo("2 days");
  }

  @Test
  void toDurationStringDaysOnly() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(2).plusMinutes(2), unitNameProvider)).isEqualTo("2 days");
  }

  @Test
  void toDurationStringDaysAndHours() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(2).plusMinutes(2), unitNameProvider)).isEqualTo("1 day and 2 hours");
  }

  @Test
  void toDurationStringOneDay() {
    assertThat(toDurationString(Duration.ofDays(1).plusHours(0).plusMinutes(2), unitNameProvider)).isEqualTo("1 day");
  }

  @Test
  void toDurationStringHoursOnlyRoundUp() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(30), unitNameProvider)).isEqualTo("3 hours");
  }

  @Test
  void toDurationStringHoursOnlyRoundDown() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(29), unitNameProvider)).isEqualTo("2 hours");
  }

  @Test
  void toDurationStringHoursOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(2).plusMinutes(2), unitNameProvider)).isEqualTo("2 hours");
  }

  @Test
  void toDurationStringHoursAndMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(2), unitNameProvider)).isEqualTo("1 hour and 2 minutes");
  }

  @Test
  void toDurationStringOneHour() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(0), unitNameProvider)).isEqualTo("1 hour");
  }

  @Test
  void toDurationStringMinutesOnly() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(2), unitNameProvider)).isEqualTo("2 minutes");
  }

  @Test
  void toDurationStringOneMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1), unitNameProvider)).isEqualTo("1 minute");
  }

  @Test
  void toDurationStringZeroMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0), unitNameProvider)).isEqualTo("0 minutes");
  }
}
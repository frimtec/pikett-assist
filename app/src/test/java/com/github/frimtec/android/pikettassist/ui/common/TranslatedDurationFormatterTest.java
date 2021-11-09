package com.github.frimtec.android.pikettassist.ui.common;

import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.translatedFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;


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
  }


  @Test
  void toDurationStringDays3() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(11).plusMinutes(29). plusSeconds(30), unitNameProvider)).isEqualTo("3 days");
  }

  @Test
  void toDurationStringDays2() {
    assertThat(toDurationString(Duration.ofDays(2).plusHours(11).plusMinutes(29). plusSeconds(29), unitNameProvider)).isEqualTo("2 days");
  }

  @Test
  void toDurationStringDays1() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(11).plusMinutes(29).plusSeconds(30), unitNameProvider)).isEqualTo("1 day");
  }

  @Test
  void toDurationStringHours11() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(11).plusMinutes(29).plusSeconds(29), unitNameProvider)).isEqualTo("11 hours");
  }

  @Test
  void toDurationStringHours2() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(29).plusSeconds(30), unitNameProvider)).isEqualTo("2 hours");
  }

  @Test
  void toDurationStringHours1() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(1).plusMinutes(29).plusSeconds(29), unitNameProvider)).isEqualTo("1 hour");
  }

  @Test
  void toDurationStringHours1Up() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(29).plusSeconds(30), unitNameProvider)).isEqualTo("1 hour");
  }

  @Test
  void toDurationStringMinutes29() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(29).plusSeconds(29), unitNameProvider)).isEqualTo("29 minutes");
  }

  @Test
  void toDurationStringMinutes2() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1).plusSeconds(30), unitNameProvider)).isEqualTo("2 minutes");
  }

  @Test
  void toDurationStringOneMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(1).plusSeconds(29), unitNameProvider)).isEqualTo("1 minute");
  }

  @Test
  void toDurationStringOneMinuteUp() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0).plusSeconds(30), unitNameProvider)).isEqualTo("1 minute");
  }

  @Test
  void toDurationStringMinutes0Down() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0).plusSeconds(29), unitNameProvider)).isEqualTo("0 minutes");
  }

  @Test
  void toDurationStringMinutes0() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0).plusSeconds(0), unitNameProvider)).isEqualTo("0 minutes");
  }

  @Test
  void toDurationStringMinusMinute() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(0).plusSeconds(-31), unitNameProvider)).isEqualTo("-1 minute");
  }

  @Test
  void toDurationStringMinusMinutes() {
    assertThat(toDurationString(Duration.ofDays(0).plusHours(0).plusMinutes(-1).plusSeconds(-31), unitNameProvider)).isEqualTo("-2 minutes");
  }
}
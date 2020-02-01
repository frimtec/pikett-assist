package com.github.frimtec.android.pikettassist.ui.common;

import org.threeten.bp.Duration;

import java.util.Locale;

public class DurationFormatter {

  private static final int HOURS_PER_DAY = 24;
  private static final int MINUTES_PER_HOUR = 60;

  public interface UnitNameProvider {

    String getDay(boolean plural);

    String getHour(boolean plural);

    String getMinute(boolean plural);

    String getAnd();
  }

  public static String toDurationString(Duration duration, UnitNameProvider unitNameProvider) {
    long days = duration.toDays();
    long hours = duration.toHours();
    long minutes = duration.toMinutes();
    if (days >= 2) {
      return String.format(Locale.getDefault(), "%d %s",
          days + (hours - HOURS_PER_DAY * days >= (HOURS_PER_DAY / 2) ? 1 : 0), unitNameProvider.getDay(true));
    } else if (days > 0 && (hours - HOURS_PER_DAY * days) != 0) {
      return String.format(Locale.getDefault(), "%d %s %s %d %s",
          days, unitNameProvider.getDay(false), unitNameProvider.getAnd(), (hours - HOURS_PER_DAY * days), unitNameProvider.getHour(true));
    } else if (days > 0) {
      return String.format(Locale.getDefault(), "%d %s",
          days, unitNameProvider.getDay(false));
    } else if (hours >= 2) {
      return String.format(Locale.getDefault(), "%d %s",
          hours + (minutes - MINUTES_PER_HOUR * hours >= (MINUTES_PER_HOUR / 2) ? 1 : 0), unitNameProvider.getHour(true));
    } else if (hours > 0 && (minutes - MINUTES_PER_HOUR * hours) != 0) {
      return String.format(Locale.getDefault(), "%d %s %s %d %s",
          hours, unitNameProvider.getHour(false), unitNameProvider.getAnd(), (minutes - MINUTES_PER_HOUR * hours), unitNameProvider.getMinute(true));
    } else if (hours > 0) {
      return String.format(Locale.getDefault(), "%d %s",
          hours, unitNameProvider.getHour(false));
    } else if (minutes != 1) {
      return String.format(Locale.getDefault(), "%d %s",
          minutes, unitNameProvider.getMinute(true));
    } else {
      return String.format(Locale.getDefault(), "%d %s",
          minutes, unitNameProvider.getMinute(false));
    }

  }
}

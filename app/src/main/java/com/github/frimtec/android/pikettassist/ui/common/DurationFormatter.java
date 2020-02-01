package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;

import org.threeten.bp.Duration;

import java.util.Locale;

public class DurationFormatter {

  private static final int HOURS_PER_DAY = 24;
  private static final int MINUTES_PER_HOUR = 60;

  public interface UnitNameProvider {

    String getDay(boolean plural);

    String getHour(boolean plural);

    String getMinute(boolean plural);

    String getSeparator();

    String getAnd();

    static UnitNameProvider siFormatter() {
      return SiUnitNameProvider.getInstance();
    }

    static UnitNameProvider translatedFormatter(Context context) {
      return new TranslatedUnitNameProvider(context);
    }
  }

  public static String toDurationString(Duration duration, UnitNameProvider unitNameProvider) {
    long days = duration.toDays();
    long hours = duration.toHours();
    long minutes = duration.toMinutes();
    if (days >= 2) {
      return String.format(Locale.getDefault(), "%d%s%s",
          days + (hours - HOURS_PER_DAY * days >= (HOURS_PER_DAY / 2) ? 1 : 0), unitNameProvider.getSeparator(), unitNameProvider.getDay(true));
    } else if (days > 0 && (hours - HOURS_PER_DAY * days) != 0) {
      return String.format(Locale.getDefault(), "%d%s%s%s%s %d%s%s",
          days, unitNameProvider.getSeparator(), unitNameProvider.getDay(false), unitNameProvider.getSeparator(), unitNameProvider.getAnd(), (hours - HOURS_PER_DAY * days), unitNameProvider.getSeparator(), unitNameProvider.getHour(true));
    } else if (days > 0) {
      return String.format(Locale.getDefault(), "%d%s%s",
          days, unitNameProvider.getSeparator(), unitNameProvider.getDay(false));
    } else if (hours >= 2) {
      return String.format(Locale.getDefault(), "%d%s%s",
          hours + (minutes - MINUTES_PER_HOUR * hours >= (MINUTES_PER_HOUR / 2) ? 1 : 0), unitNameProvider.getSeparator(), unitNameProvider.getHour(true));
    } else if (hours > 0 && (minutes - MINUTES_PER_HOUR * hours) != 0) {
      return String.format(Locale.getDefault(), "%d%s%s%s%s %d%s%s",
          hours, unitNameProvider.getSeparator(), unitNameProvider.getHour(false), unitNameProvider.getSeparator(), unitNameProvider.getAnd(), (minutes - MINUTES_PER_HOUR * hours), unitNameProvider.getSeparator(), unitNameProvider.getMinute(true));
    } else if (hours > 0) {
      return String.format(Locale.getDefault(), "%d%s%s",
          hours, unitNameProvider.getSeparator(), unitNameProvider.getHour(false));
    } else if (minutes != 1) {
      return String.format(Locale.getDefault(), "%d%s%s",
          minutes, unitNameProvider.getSeparator(), unitNameProvider.getMinute(true));
    } else {
      return String.format(Locale.getDefault(), "%d%s%s",
          minutes, unitNameProvider.getSeparator(), unitNameProvider.getMinute(false));
    }

  }
}

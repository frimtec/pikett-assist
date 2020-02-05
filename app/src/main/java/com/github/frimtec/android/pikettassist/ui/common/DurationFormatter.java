package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;

import org.threeten.bp.Duration;

import java.util.Locale;

public class DurationFormatter {

  private static final int HOURS_PER_DAY = 24;
  private static final int MINUTES_PER_HOUR = 60;
  private static final int SECONDS_PER_MINUTE = 60;

  private static final int UNIT_CHANGE_LIMIT = 1;

  public interface UnitNameProvider {

    String getDay(boolean plural);

    String getHour(boolean plural);

    String getMinute(boolean plural);

    String getSeparator();

    static UnitNameProvider siFormatter() {
      return SiUnitNameProvider.getInstance();
    }

    static UnitNameProvider translatedFormatter(Context context) {
      return new TranslatedUnitNameProvider(context);
    }
  }

  public static String toDurationString(Duration duration, UnitNameProvider unitNameProvider) {
    long minutes = Math.round((float) duration.getSeconds() / SECONDS_PER_MINUTE);
    long hours = Math.round((float) minutes / MINUTES_PER_HOUR);
    long days = Math.round((float) hours / HOURS_PER_DAY);

    if (days >= UNIT_CHANGE_LIMIT) {
      return format(days, unitNameProvider.getSeparator(), unitNameProvider.getDay(Math.abs(days) != 1));
    } else if (hours >= UNIT_CHANGE_LIMIT) {
      return format(hours, unitNameProvider.getSeparator(), unitNameProvider.getHour(Math.abs(hours) != 1));
    } else {
      return format(minutes, unitNameProvider.getSeparator(), unitNameProvider.getMinute(Math.abs(minutes) != 1));
    }
  }

  private static String format(long value, String separator, String unit) {
    return String.format(Locale.getDefault(), "%d%s%s", value, separator, unit);
  }
}

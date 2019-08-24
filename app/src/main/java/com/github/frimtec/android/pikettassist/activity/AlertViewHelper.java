package com.github.frimtec.android.pikettassist.activity;

import android.content.Context;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AlertViewHelper {

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMM yyyy HH:mm";
  private static final String TIME_FORMAT = "HH:mm";

  private AlertViewHelper() {
  }

  public static String getTimeWindow(Alert alert) {
    String timeWindowText = formatDateTime(alert.getStartTime(), DATE_TIME_FORMAT);
    if (alert.isClosed()) {
      timeWindowText = String.format("%s - %s", timeWindowText, formatDateTime(alert.getEndTime(), TIME_FORMAT));
    }
    return timeWindowText;
  }

  public static String getDurations(Context context, Alert alert) {
    String confirmText = "";
    if (alert.isConfirmed()) {
      Duration confirmDuration = Duration.between(alert.getStartTime(), alert.getConfirmTime());
      confirmText = String.format(context.getString(R.string.alert_view_confirm_time), confirmDuration.getSeconds());
    }
    String durationText = "";
    if (alert.isClosed()) {
      Duration duration = Duration.between(alert.getStartTime(), alert.getEndTime());
      durationText = String.format(context.getString(R.string.alert_view_duration), duration.getSeconds() / 60d);
    }
    return Stream.of(confirmText, durationText).filter(((Predicate<String>) String::isEmpty).negate()).collect(Collectors.joining("\n"));
  }

  public static String getState(Context context, Alert alert) {
    String currentStateText;
    if (alert.isClosed()) {
      currentStateText = context.getString(R.string.alert_view_state_closed);
    } else if (alert.isConfirmed()) {
      currentStateText = context.getString(R.string.alert_view_state_wip);
    } else {
      currentStateText = context.getString(R.string.alert_view_state_to_be_confirmed);
    }
    return String.format("%s: %s", context.getString(R.string.alert_view_state_title), currentStateText);
  }

  private static String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }

}

package com.github.frimtec.android.pikettassist.activity;

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
    if(alert.isClosed()) {
      timeWindowText = String.format("%s - %s", timeWindowText, formatDateTime(alert.getEndTime(), TIME_FORMAT));
    }
    return timeWindowText;
  }

  public static String getDurations(Alert alert) {
    String confirmText = "";
    if(alert.isConfirmed()) {
      Duration confirmDuration = Duration.between(alert.getStartTime(), alert.getConfirmTime());
      confirmText = String.format("Time to confirm: %d sec", confirmDuration.getSeconds());
    }
    String durationText = "";
    if(alert.isClosed()) {
      Duration duration = Duration.between(alert.getStartTime(), alert.getEndTime());
      durationText = String.format("Duration: %.1f min", duration.getSeconds() / 60d);
    }
    return Stream.of(confirmText, durationText).filter(((Predicate<String>) String::isEmpty).negate()).collect(Collectors.joining("\n"));
  }

  public static String getState(Alert alert) {
    String currentStateText;
    if(alert.isClosed()) {
      currentStateText = "Closed";
    } else if(alert.isConfirmed() ) {
      currentStateText = "Work in progress";
    } else {
      currentStateText = "Awaiting confirmation";
    }
    return "State: " + currentStateText;
  }

  private static String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }

}

package com.github.frimtec.android.pikettassist.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ApplicationPreferences {

  private static final String PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN = "calendar_event_pikett_title_pattern";
  private static final String PREF_KEY_CALENDAR_SELECTION = "calendar_selection";
  private static final String PREF_KEY_PRE_POST_RUN_TIME_SECONDS = "pre_post_run_time_seconds";
  private static final String PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT = "alarm_operations_center_contact";
  private static final String PREF_KEY_TEST_ALARM_MESSAGE_PATTERN = "test_alarm_message_pattern";
  private static final String PREF_KEY_TEST_ALARM_CHECK_TIME = "test_alarm_check_time";
  private static final String PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS = "test_alarm_check_weekdays";
  private static final String PREF_KEY_TEST_ALARM_ENABLED = "test_alarm_enabled";
  private static final String PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES = "test_alarm_accept_time_window_minutes";
  private static final String PREF_KEY_SEND_CONFIRM_SMS = "send_confirm_sms";
  private static final String PREF_KEY_SMS_CONFIRM_TEXT = "sms_confirm_text";
  private static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH = "supervise_signal_strength";
  private static final String PREF_KEY_NOTIFY_LOW_SIGNAL = "notify_low_signal";
  private static final String PREF_KEY_LOW_SIGNAL_FILTER = "low_signal_filter";
  public static final int PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR = 15;
  private static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_MIN_LEVEL = "supervise_signal_strength_min_level";
  private static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION = "supervise_signal_strength_subscription";
  private static final String PREF_KEY_ALARM_RING_TONE = "alarm_ring_tone";
  private static final String PREF_KEY_TEST_ALARM_RING_TONE = "test_alarm_ring_tone";
  private static final String PREF_KEY_SUPERVISE_TEST_CONTEXTS = "supervise_test_contexts";
  public static final String CALENDAR_FILTER_ALL = "-1";
  private static final String PREF_KEY_MANAGE_VOLUME = "manage_volume";
  private static final String PREF_KEY_ON_CALL_DAY_VOLUME = "on_call_day_volume";
  private static final String PREF_KEY_ON_CALL_NIGHT_VOLUME = "on_call_night_volume";
  private static final String PREF_KEY_DAY_START_TIME = "day_start_time";
  private static final String PREF_KEY_NIGHT_START_TIME = "night_start_time";

  private ApplicationPreferences() {
  }

  public static String getCalendarEventPikettTitlePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN, context.getString(R.string.pref_default_calendar_event_pikett_title_pattern)).trim();
  }

  public static String getCalendarSelection(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_SELECTION, CALENDAR_FILTER_ALL);
  }

  public static Duration getPrePostRunTime(Context context) {
    return Duration.ofSeconds(Integer.parseInt(getSharedPreferences(context, PREF_KEY_PRE_POST_RUN_TIME_SECONDS, context.getString(R.string.pref_default_pre_post_run_time_seconds))));
  }

  public static ContactReference getOperationsCenterContactReference(Context context) {
    return ContactReference.fromSerializedString(getSharedPreferences(context, PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, ContactReference.NO_SELECTION.getSerializedString()));
  }

  public static void setOperationsCenterContactReference(Context context, ContactReference contactReference) {
    setSharedPreferences(context, setter -> setter.putString(PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, contactReference.getSerializedString()));
  }

  public static String getSmsTestMessagePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_MESSAGE_PATTERN, context.getString(R.string.pref_default_test_alarm_message_pattern)).trim();
  }

  public static boolean getSendConfirmSms(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SEND_CONFIRM_SMS, true);
  }

  public static String getSmsConfirmText(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_CONFIRM_TEXT, context.getString(R.string.pref_default_sms_confirm_text));
  }

  public static boolean getSuperviseSignalStrength(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, true);
  }

  public static int getSuperviseSignalStrengthMinLevel(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_MIN_LEVEL, context.getString(R.string.pref_default_supervise_signal_strength_min_level)));
  }

  public static int getSuperviseSignalStrengthSubscription(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION, context.getString(R.string.pref_default_supervise_signal_strength_subscription)));
  }

  public static void setSuperviseSignalStrength(Context context, boolean supervise) {
    setSharedPreferences(context, setter -> setter.putBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, supervise));
  }

  public static boolean getNotifyLowSignal(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_NOTIFY_LOW_SIGNAL, true);
  }

  public static int getLowSignalFilter(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_LOW_SIGNAL_FILTER, R.integer.default_low_signal_filter);
  }

  public static boolean getTestAlarmEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_TEST_ALARM_ENABLED, false);
  }

  public static String getAlarmRingTone(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(PREF_KEY_ALARM_RING_TONE, "");
  }

  public static String getTestAlarmRingTone(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(PREF_KEY_TEST_ALARM_RING_TONE, "");
  }

  public static Set<TestAlarmContext> getSupervisedTestAlarms(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, Collections.emptySet()).stream()
        .map(TestAlarmContext::new)
        .collect(Collectors.toSet());
  }

  public static String getTestAlarmCheckTime(Context context) {
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_CHECK_TIME, context.getString(R.string.pref_default_test_alarm_check_time));
  }

  public static Set<String> getTestAlarmCheckWeekdays(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getStringSet(PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS, Collections.emptySet());
  }

  public static int getTestAlarmAcceptTimeWindowMinutes(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES, context.getString(R.string.pref_default_test_alarm_accept_time_window_minutes)));
  }

  public static void setSuperviseTestContexts(Context context, Set<TestAlarmContext> values) {
    setSharedPreferences(context, setter -> setter.putStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, values.stream()
        .map(TestAlarmContext::getContext)
        .collect(Collectors.toSet())));
  }

  public static boolean getManageVolumeEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_MANAGE_VOLUME, false);
  }

  private static int getOnCallDayVolume(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_ON_CALL_DAY_VOLUME, R.integer.default_volume_day);
  }

  public static int getOnCallVolume(Context context, LocalTime currentTime) {
    return currentTime.isAfter(getDayProfileStartTime(context)) && currentTime.isBefore(getNightProfileStartTime(context)) ? getOnCallDayVolume(context) : getOnCallNightVolume(context);
  }

  private static int getOnCallNightVolume(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_ON_CALL_NIGHT_VOLUME, R.integer.default_volume_night);
  }

  private static LocalTime getDayProfileStartTime(Context context) {
    return LocalTime.parse(getSharedPreferences(context, PREF_KEY_DAY_START_TIME, context.getString(R.string.pref_default_day_start_time)), DateTimeFormatter.ISO_TIME);
  }

  private static LocalTime getNightProfileStartTime(Context context) {
    return LocalTime.parse(getSharedPreferences(context, PREF_KEY_NIGHT_START_TIME, context.getString(R.string.pref_default_night_start_time)), DateTimeFormatter.ISO_TIME);
  }

  private static void setSharedPreferences(Context context, Consumer<SharedPreferences.Editor> setter) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    setter.accept(editor);
    editor.apply();
  }

  private static String getSharedPreferences(Context context, String key, String defaultValue) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(key, defaultValue);
  }

}

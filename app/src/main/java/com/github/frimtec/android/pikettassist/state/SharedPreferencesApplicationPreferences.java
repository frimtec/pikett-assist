package com.github.frimtec.android.pikettassist.state;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

final class SharedPreferencesApplicationPreferences implements ApplicationPreferences {

  private static final String PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN = "calendar_event_pikett_title_pattern";
  private static final String PREF_KEY_PARTNER_SEARCH_EXTRACT_PATTERN = "partner_search_extract_pattern";
  private static final String PREF_KEY_USE_PARTNER_EXTRACTION = "use_partner_extraction";
  private static final String PREF_KEY_CALENDAR_SELECTION = "calendar_selection";
  private static final String PREF_KEY_PRE_POST_RUN_TIME_SECONDS = "pre_post_run_time_seconds";
  private static final String PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT = "alarm_operations_center_contact";
  private static final String PREF_KEY_TEST_ALARM_MESSAGE_PATTERN = "test_alarm_message_pattern";
  private static final String PREF_KEY_META_SMS_MESSAGE_PATTERN = "meta_sms_message_pattern";
  private static final String PREF_KEY_TEST_ALARM_CHECK_TIME = "test_alarm_check_time";
  private static final String PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS = "test_alarm_check_weekdays";
  private static final String PREF_KEY_TEST_ALARM_ENABLED = "test_alarm_enabled";
  private static final String PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES = "test_alarm_accept_time_window_minutes";
  private static final String PREF_KEY_SEND_CONFIRM_SMS = "send_confirm_sms";
  private static final String PREF_KEY_SMS_CONFIRM_TEXT = "sms_confirm_text";
  private static final String PREF_KEY_SUPERVISE_BATTERY_LEVEL = "supervise_battery_level";
  private static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH = "supervise_signal_strength";
  private static final String PREF_KEY_NOTIFY_LOW_SIGNAL = "notify_low_signal";
  private static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_MIN_LEVEL = "supervise_signal_strength_min_level";
  private static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION = "supervise_signal_strength_subscription";
  private static final String PREF_KEY_ALARM_RING_TONE = "alarm_ring_tone";
  private static final String PREF_KEY_TEST_ALARM_RING_TONE = "test_alarm_ring_tone";
  private static final String PREF_KEY_SUPERVISE_TEST_CONTEXTS = "supervise_test_contexts";
  private static final String PREF_KEY_MANAGE_VOLUME = "manage_volume";
  private static final String PREF_KEY_ON_CALL_DAY_VOLUME = "on_call_day_volume";
  private static final String PREF_KEY_ON_CALL_NIGHT_VOLUME = "on_call_night_volume";
  private static final String PREF_KEY_DAY_START_TIME = "day_start_time";
  private static final String PREF_KEY_NIGHT_START_TIME = "night_start_time";
  private static final String PREF_APP_THEME = "app_theme";

  public static final SharedPreferencesApplicationPreferences INSTANCE = new SharedPreferencesApplicationPreferences();

  private SharedPreferencesApplicationPreferences() {
  }

  @Override
  public String getCalendarEventPikettTitlePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN, context.getString(R.string.pref_default_calendar_event_pikett_title_pattern)).trim();
  }

  @Override
  public boolean getPartnerExtractionEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_USE_PARTNER_EXTRACTION, true);
  }

  @Override
  public String getPartnerSearchExtractPattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_PARTNER_SEARCH_EXTRACT_PATTERN, context.getString(R.string.pref_default_partner_search_pattern)).trim();
  }

  @Override
  public String getCalendarSelection(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_SELECTION, CALENDAR_FILTER_ALL);
  }

  @Override
  public Duration getPrePostRunTime(Context context) {
    return Duration.ofSeconds(Integer.parseInt(getSharedPreferences(context, PREF_KEY_PRE_POST_RUN_TIME_SECONDS, context.getString(R.string.pref_default_pre_post_run_time_seconds))));
  }

  @Override
  public ContactReference getOperationsCenterContactReference(Context context) {
    return ContactReference.fromSerializedString(getSharedPreferences(context, PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, ContactReference.NO_SELECTION.getSerializedString()));
  }

  @Override
  public void setOperationsCenterContactReference(Context context, ContactReference contactReference) {
    setSharedPreferences(context, setter -> setter.putString(PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, contactReference.getSerializedString()));
  }

  @Override
  public String getSmsTestMessagePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_MESSAGE_PATTERN, context.getString(R.string.pref_default_test_alarm_message_pattern)).trim();
  }

  @Override
  public String getMetaSmsMessagePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_META_SMS_MESSAGE_PATTERN, "").trim();
  }

  @Override
  public boolean getSendConfirmSms(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SEND_CONFIRM_SMS, true);
  }

  @Override
  public String getSmsConfirmText(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_CONFIRM_TEXT, context.getString(R.string.pref_default_sms_confirm_text));
  }

  @Override
  public boolean getSuperviseSignalStrength(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, true);
  }

  @Override
  public int getSuperviseSignalStrengthMinLevel(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_MIN_LEVEL, context.getString(R.string.pref_default_supervise_signal_strength_min_level)));
  }

  @Override
  public int getSuperviseSignalStrengthSubscription(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION, context.getString(R.string.pref_default_supervise_signal_strength_subscription)));
  }

  @Override
  public void setSuperviseSignalStrengthSubscription(Context context, int subscriptionId) {
    setSharedPreferences(context, setter -> setter.putString(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION, String.valueOf(subscriptionId)));
  }

  @Override
  public void setSuperviseSignalStrength(Context context, boolean supervise) {
    setSharedPreferences(context, setter -> setter.putBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, supervise));
  }

  @Override
  public boolean getNotifyLowSignal(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_NOTIFY_LOW_SIGNAL, true);
  }

  @Override
  public int getLowSignalFilterSeconds(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return convertLowSignalFilerToSeconds(preferences.getInt(PREF_KEY_LOW_SIGNAL_FILTER, context.getResources().getInteger(R.integer.default_low_signal_filter)));
  }

  @Override
  public int convertLowSignalFilerToSeconds(int filterValue) {
    return LOW_SIGNAL_FILTER_PREFERENCE.getValue(filterValue);
  }

  @Override
  public boolean getSuperviseBatteryLevel(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SUPERVISE_BATTERY_LEVEL, true);
  }

  @Override
  public void setSuperviseBatteryLevel(Context context, boolean supervise) {
    setSharedPreferences(context, setter -> setter.putBoolean(PREF_KEY_SUPERVISE_BATTERY_LEVEL, supervise));
  }

  @Override
  public int getBatteryWarnLevel(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_KEY_BATTERY_WARN_LEVEL, context.getResources().getInteger(R.integer.default_battery_warn_level));
  }

  @Override
  public boolean getTestAlarmEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_TEST_ALARM_ENABLED, false);
  }

  @Override
  public String getAlarmRingTone(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(PREF_KEY_ALARM_RING_TONE, "");
  }

  @Override
  public String getTestAlarmRingTone(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(PREF_KEY_TEST_ALARM_RING_TONE, "");
  }

  @Override
  public Set<TestAlarmContext> getSupervisedTestAlarms(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, Collections.emptySet()).stream()
        .map(TestAlarmContext::new)
        .collect(Collectors.toSet());
  }

  @Override
  public String getTestAlarmCheckTime(Context context) {
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_CHECK_TIME, context.getString(R.string.pref_default_test_alarm_check_time));
  }

  @Override
  public Set<String> getTestAlarmCheckWeekdays(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getStringSet(PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS, Collections.emptySet());
  }

  @Override
  public int getTestAlarmAcceptTimeWindowMinutes(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES, context.getString(R.string.pref_default_test_alarm_accept_time_window_minutes)));
  }

  @Override
  public int getAppTheme(Context context) {
    return Integer.parseInt(getSharedPreferences(context, PREF_APP_THEME, String.valueOf(context.getResources().getInteger(R.integer.default_app_theme))));
  }

  @Override
  public void setSuperviseTestContexts(Context context, Set<TestAlarmContext> values) {
    setSharedPreferences(context, setter -> setter.putStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, values.stream()
        .map(TestAlarmContext::context)
        .collect(Collectors.toSet())));
  }

  @Override
  public boolean getManageVolumeEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_MANAGE_VOLUME, false);
  }

  @Override
  public boolean getBatterySaferAtNightEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_BATTERY_SAFER_AT_NIGHT, false);
  }

  private static int getOnCallDayVolume(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_ON_CALL_DAY_VOLUME, R.integer.default_volume_day);
  }

  @Override
  public int getOnCallVolume(Context context, LocalTime currentTime) {
    return isDayProfile(context, currentTime) ? getOnCallDayVolume(context) : getOnCallNightVolume(context);
  }

  @Override
  public boolean isDayProfile(Context context, LocalTime currentTime) {
    return currentTime.isAfter(getDayProfileStartTime(context)) && currentTime.isBefore(getNightProfileStartTime(context));
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

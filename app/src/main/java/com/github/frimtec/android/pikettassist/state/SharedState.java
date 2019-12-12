package com.github.frimtec.android.pikettassist.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.OnOffState;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.github.frimtec.android.pikettassist.helper.CalendarEventHelper.hasPikettEventForNow;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_TRUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;

public final class SharedState {

  public static final String PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN = "calendar_event_pikett_title_pattern";
  public static final String PREF_KEY_CALENDAR_SELECTION = "calendar_selection";
  public static final String PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT = "alarm_operations_center_contact";
  public static final String PREF_KEY_TEST_ALARM_MESSAGE_PATTERN = "test_alarm_message_pattern";
  public static final String PREF_KEY_TEST_ALARM_CHECK_TIME = "test_alarm_check_time";
  public static final String PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS = "test_alarm_check_weekdays";
  public static final String PREF_KEY_TEST_ALARM_ENABLED = "test_alarm_enabled";
  public static final String PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES = "test_alarm_accept_time_window_minutes";
  public static final String PREF_KEY_SMS_CONFIRM_TEXT = "sms_confirm_text";
  public static final String PREF_KEY_SMS_ADAPTER_SECRET = "sms_adapter_secret";
  public static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH = "supervise_signal_strength";
  public static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_MIN_LEVEL = "supervise_signal_strength_min_level";
  public static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION = "supervise_signal_strength_subscription";
  public static final String PREF_KEY_ALARM_RING_TONE = "alarm_ring_tone";
  public static final String PREF_KEY_TEST_ALARM_RING_TONE = "test_alarm_ring_tone";
  public static final String PREF_KEY_SUPERVISE_TEST_CONTEXTS = "supervise_test_contexts";
  public static final String CALENDAR_FILTER_ALL = "-1";
  public static final String PREF_KEY_PIKETT_STATE_MANUALLY_ON = "pikett_state_manually_on";
  private static final String PREF_KEY_DEFAULT_VOLUME = "default_volume";
  private static final String PREF_KEY_MANAGE_VOLUME = "manage_volume";
  private static final String PREF_KEY_ON_CALL_DAY_VOLUME = "on_call_day_volume";
  private static final String PREF_KEY_ON_CALL_NIGHT_VOLUME = "on_call_night_volume";
  public static final String PREF_KEY_DAY_START_TIME = "day_start_time";
  public static final String PREF_KEY_NIGHT_START_TIME = "night_start_time";
  public static final int DEFAULT_VALUE_NOT_SET = -1;

  private static final String LAST_ALARM_SMS_NUMBER = "last_alarm_sms_number";

  public static final long EMPTY_CONTACT = -1;

  private static final String TAG = "SharedState";

  private SharedState() {
  }

  public static OnOffState getPikettState(Context context) {
    return getPikettStateManuallyOn(context) || hasPikettEventForNow(context, getCalendarEventPikettTitlePattern(context), SharedState.getCalendarSelection(context)) ? OnOffState.ON : OnOffState.OFF;
  }

  public static Pair<AlarmState, Long> getAlarmState() {
    try (SQLiteDatabase db = PAssist.getReadableDatabase();
         Cursor cursor = db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null)) {
      if (cursor.getCount() == 0) {
        return Pair.create(AlarmState.OFF, null);
      } else if (cursor.getCount() > 0)
        if (cursor.getCount() > 1) {
          Log.e(TAG, "More then one open case selected");
        }
      cursor.moveToFirst();
      long id = cursor.getLong(0);
      boolean confirmed = cursor.getInt(1) == BOOLEAN_TRUE;
      return Pair.create(confirmed ? AlarmState.ON_CONFIRMED : AlarmState.ON, id);
    }
  }

  public static String getCalendarEventPikettTitlePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN, context.getString(R.string.pref_default_calendar_event_pikett_title_pattern))
        .trim();
  }

  public static String getCalendarSelection(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_SELECTION, CALENDAR_FILTER_ALL);
  }

  public static long getAlarmOperationsCenterContact(Context context) {
    return Long.parseLong(getSharedPreferences(context, PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, String.valueOf(EMPTY_CONTACT)));
  }

  public static void setAlarmOperationsCenterContact(Context context, Contact contact) {
    setSharedPreferences(context, PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, String.valueOf(contact.getId()));
  }

  public static String getSmsTestMessagePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_MESSAGE_PATTERN, context.getString(R.string.pref_default_test_alarm_message_pattern))
        .trim();
  }

  public static String getSmsConfirmText(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_CONFIRM_TEXT, context.getString(R.string.pref_default_sms_confirm_text));
  }

  public static String getSmsAdapterSecret(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_ADAPTER_SECRET, "");
  }

  public static void setSmsAdapterSecret(Context context, String secret) {
    setSharedPreferences(context, PREF_KEY_SMS_ADAPTER_SECRET, secret);
  }

  public static boolean getSuperviseSignalStrength(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, true);
  }

  public static int getSuperviseSignalStrengthMinLevel(Context context) {
    return Integer.valueOf(getSharedPreferences(context, PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_MIN_LEVEL, context.getString(R.string.pref_default_supervise_signal_strength_min_level)));
  }

  public static int getSuperviseSignalStrengthSubscription(Context context) {
    return Integer.valueOf(getSharedPreferences(context, PREF_KEY_SUPERVISE_SIGNAL_STRENGTH_SUBSCRIPTION, context.getString(R.string.pref_default_supervise_signal_strength_subscription)));
  }

  public static void setSuperviseSignalStrength(Context context, boolean supervise) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, supervise);
    editor.apply();
  }

  public static boolean getPikettStateManuallyOn(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_PIKETT_STATE_MANUALLY_ON, false);
  }

  public static void setPikettStateManuallyOn(Context context, boolean manuallyOn) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putBoolean(PREF_KEY_PIKETT_STATE_MANUALLY_ON, manuallyOn);
    editor.apply();
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

  public static Set<String> getSuperviseTestContexts(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return new HashSet<>(preferences.getStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, Collections.emptySet()));
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

  public static void setSuperviseTestContexts(Context context, Set<String> values) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, new HashSet<>(values));
    editor.apply();
  }

  public static Pair<String, Integer> getLastAlarmSmsNumberWithSubscriptionId(Context context) {
    String sharedPreferences = getSharedPreferences(context, LAST_ALARM_SMS_NUMBER, ";");
    String[] split = sharedPreferences.split(";");
    return Pair.create(split[0], split.length > 1 && !split[1].isEmpty() ? Integer.valueOf(split[1]) : null);
  }

  public static void setLastAlarmSmsNumberWithSubscriptionId(Context context, String smsNumber, Integer subscriptionId) {
    setSharedPreferences(context, LAST_ALARM_SMS_NUMBER, smsNumber + ";" + (subscriptionId != null ? subscriptionId : ""));
  }

  public static boolean getManageVolumeEnabled(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_MANAGE_VOLUME, false);
  }

  public static int getDefaultVolume(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_DEFAULT_VOLUME, DEFAULT_VALUE_NOT_SET);
  }

  public static void setDefaultVolume(Context context, int volume) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putInt(PREF_KEY_DEFAULT_VOLUME, volume);
    editor.apply();
  }

  public static int getOnCallDayVolume(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_ON_CALL_DAY_VOLUME, R.integer.default_volume_day);
  }

  public static int getOnCallVolume(Context context, LocalTime currentTime) {
    return currentTime.isAfter(getDayProfileStartTime(context)) && currentTime.isBefore(getNightProfileStartTime(context)) ? getOnCallDayVolume(context) : getOnCallNightVolume(context);
  }

  public static int getOnCallNightVolume(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getInt(PREF_KEY_ON_CALL_NIGHT_VOLUME, R.integer.default_volume_night);
  }

  public static LocalTime getDayProfileStartTime(Context context) {
    return LocalTime.parse(getSharedPreferences(context, PREF_KEY_DAY_START_TIME, context.getString(R.string.pref_default_day_start_time)), DateTimeFormatter.ISO_TIME);
  }

  public static LocalTime getNightProfileStartTime(Context context) {
    return LocalTime.parse(getSharedPreferences(context, PREF_KEY_NIGHT_START_TIME, context.getString(R.string.pref_default_night_start_time)), DateTimeFormatter.ISO_TIME);
  }

  private static void setSharedPreferences(Context context, String key, String value) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(key, value);
    editor.apply();
  }

  private static String getSharedPreferences(Context context, String key, String defaultValue) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(key, defaultValue);
  }

}

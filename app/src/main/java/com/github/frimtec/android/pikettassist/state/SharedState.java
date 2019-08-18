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
import com.github.frimtec.android.pikettassist.domain.DualState;

import java.util.Collections;
import java.util.Set;

import static com.github.frimtec.android.pikettassist.helper.CalendarEventHelper.hasPikettEventForNow;

public final class SharedState {

  private static final String TAG = "SharedState";
  public static final String PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN = "calendar_event_pikett_title_pattern";
  public static final String PREF_KEY_CALENDAR_SELECTION = "calendar_selection";
  public static final String PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT = "alarm_operations_center_contact";
  public static final String PREF_KEY_SMS_TEST_MESSAGE_PATTERN = "sms_test_message_pattern";
  public static final String PREF_KEY_TEST_ALARM_CHECK_TIME = "test_alarm_check_time";
  public static final String PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS = "test_alarm_check_weekdays";
  public static final String PREF_KEY_SMS_CONFIRM_TEXT = "sms_confirm_text";
  public static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH = "supervise_signal_strength";
  public static final String PREF_KEY_ALARM_RING_TONE = "alarm_ring_tone";
  public static final String PREF_KEY_SUPERVISE_TEST_CONTEXTS = "supervise_test_contexts";

  private static final String PREF_KEY_TEST_ALARM_STATE_PREFIX = "test_alarm_state_";

  public static final String START_OF_TIME = "0";
  public static final String CALENDAR_FILTER_ALL = "-1";
  public static final long EMPTY_CONTACT = -1;

  private SharedState() {
  }

  public static DualState getPikettState(Context context) {
    return hasPikettEventForNow(context, getCalendarEventPikettTitlePattern(context), SharedState.getCalendarSelection(context)) ? DualState.ON : DualState.OFF;
  }

  public static Pair<AlarmState, Long> getAlarmState(Context context) {
    try (SQLiteDatabase db = PikettAssist.getReadableDatabase();
         Cursor cursor = db.query("t_alert", new String[]{"_id", "confirm_time"}, "end_time is null", null, null, null, null)) {
      if (cursor.getCount() == 0) {
        return Pair.create(AlarmState.OFF, null);
      } else if (cursor.getCount() > 0)
        if (cursor.getCount() > 1) {
          Log.e(TAG, "More then one open case selected");
        }
      cursor.moveToFirst();
      long id = cursor.getLong(0);
      long confirmTime = cursor.getLong(1);
      return Pair.create(confirmTime == 0 ? AlarmState.ON : AlarmState.ON_CONFIRMED, id);
    }
  }

  public static String getCalendarEventPikettTitlePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN, context.getString(R.string.pref_default_calendar_event_pikett_title_pattern));
  }

  public static String getCalendarSelection(Context context) {
    return getSharedPreferences(context, PREF_KEY_CALENDAR_SELECTION, CALENDAR_FILTER_ALL);
  }

  public static long getAlarmOperationsCenterContact(Context context) {
    return Long.parseLong(getSharedPreferences(context, PREF_KEY_ALARM_OPERATIONS_CENTER_CONTACT, String.valueOf(EMPTY_CONTACT)));
  }

  public static String getSmsTestMessagePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_TEST_MESSAGE_PATTERN, ".*Test.*");
  }

  public static String getSmsConfirmText(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_CONFIRM_TEXT, "OK");
  }

  public static boolean getSuperviseSignalStrength(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, true);
  }

  public static String getAlarmRingTone(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getString(PREF_KEY_ALARM_RING_TONE, "");
  }

  public static Set<String> getSuperviseTestContexts(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, Collections.emptySet());
  }

  public static String getTestAlarmCheckTime(Context context) {
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_CHECK_TIME, "12:00");
  }

  public static Set<String> getTestAlarmCheckWeekdays(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getStringSet(PREF_KEY_TEST_ALARM_CHECK_WEEKDAYS, Collections.emptySet());
  }

  public static void setSuperviseTestContexts(Context context, Set<String> values) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = preferences.edit();
    editor.putStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, values);
    editor.apply();
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

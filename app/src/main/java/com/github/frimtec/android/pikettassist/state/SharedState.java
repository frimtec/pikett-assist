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

import java.util.Collections;
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
  public static final String PREF_KEY_TEST_ALARM_ACCEPT_TIME_WINDOW_MINUTES = "test_alarm_accept_time_window_minutes";
  public static final String PREF_KEY_SMS_CONFIRM_TEXT = "sms_confirm_text";
  public static final String PREF_KEY_SUPERVISE_SIGNAL_STRENGTH = "supervise_signal_strength";
  public static final String PREF_KEY_ALARM_RING_TONE = "alarm_ring_tone";
  public static final String PREF_KEY_TEST_ALARM_RING_TONE = "test_alarm_ring_tone";
  public static final String PREF_KEY_SUPERVISE_TEST_CONTEXTS = "supervise_test_contexts";
  public static final String CALENDAR_FILTER_ALL = "-1";
  public static final String PREF_KEY_PIKETT_STATE_MANUALLY_ON = "pikett_state_manually_on";

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
    return getSharedPreferences(context, PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN, context.getString(R.string.pref_default_calendar_event_pikett_title_pattern));
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
    return getSharedPreferences(context, PREF_KEY_TEST_ALARM_MESSAGE_PATTERN, context.getString(R.string.pref_default_test_alarm_message_pattern));
  }

  public static String getSmsConfirmText(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_CONFIRM_TEXT, context.getString(R.string.pref_default_sms_confirm_text));
  }

  public static boolean getSuperviseSignalStrength(Context context) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    return preferences.getBoolean(PREF_KEY_SUPERVISE_SIGNAL_STRENGTH, true);
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
    return preferences.getStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, Collections.emptySet());
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
    editor.putStringSet(PREF_KEY_SUPERVISE_TEST_CONTEXTS, values);
    editor.apply();
  }

  public static String getLastAlarmSmsNumber(Context context) {
    return getSharedPreferences(context, LAST_ALARM_SMS_NUMBER, "");
  }

  public static void setLastAlarmSmsNumber(Context context, String smsNumber) {
    setSharedPreferences(context, LAST_ALARM_SMS_NUMBER, smsNumber);
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

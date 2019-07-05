package com.github.frimtec.android.pikettassist.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.PikettState;

import java.time.Instant;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.helper.CelendarEventHelper.hasPikettEventForNow;

public final class SharedState {

  private static final String TAG = "SharedState";
  public static final String PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN = "calendar_event_pikett_title_pattern";
  public static final String PREF_KEY_SMS_SENDER_NUMBER = "sms_sender_number";
  public static final String PREF_KEY_SMS_TEST_MESSAGE_PATTERN = "sms_test_message_pattern";
  public static final String PREF_KEY_USE_VIBRATE = "use_vibrate";
  public static final String START_OF_TIME = "0";

  private SharedState() {
  }

  public static PikettState getPikettState(Context context) {
    return hasPikettEventForNow(context, getCalendarEventPikettTitlePattern(context)) ? PikettState.ON : PikettState.OFF;
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
    return getSharedPreferences(context, PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN, ".*Pikett.*");
  }

  public static String getSmsSenderNumber(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_SENDER_NUMBER, "");
  }

  public static String getSmsTestMessagePattern(Context context) {
    return getSharedPreferences(context, PREF_KEY_SMS_TEST_MESSAGE_PATTERN, ".*Test.*");
  }

  public static boolean getUseVibrate(Context context) {
    return Boolean.valueOf(getSharedPreferences(context, PREF_KEY_USE_VIBRATE, "true"));
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

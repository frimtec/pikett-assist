package com.github.frimtec.android.pikettassist.state;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.PikettState;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

import static com.github.frimtec.android.pikettassist.helper.CelendarEventHelper.hasPikettEventForNow;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public final class SharedState {

    private static final String TAG = "SharedState";
    public static final String PREF_KEY_ALARM_STATE = "alarm_state";
    public static final String PREF_KEY_ALARM_START_TIME = "alarm_start_time";
    public static final String PREF_KEY_CALENDAR_EVENT_PIKETT_TITLE_PATTERN = "calendar_event_pikett_title_pattern";
    public static final String PREF_KEY_SMS_SENDER_NUMBER = "sms_sender_number";
    public static final String PREF_KEY_SMS_TEST_MESSAGE_PATTERN = "sms_test_message_pattern";
    public static final String PREF_KEY_SMS_LAST_TEST_MESSAGE_RECEIVED_TIME = "last_test_message_received_time";
    public static final String PREF_KEY_USE_VIBRATE = "use_vibrate";
    public static final String START_OF_TIME = "0";

    private SharedState() {
    }

    public static PikettState getPikettState(Context context) {
        return hasPikettEventForNow(context, getCalendarEventPikettTitlePattern(context)) ? PikettState.ON : PikettState.OFF;
    }

    public static AlarmState getAlarmState(Context context) {
        return AlarmState.valueOf(getSharedPreferences(context, PREF_KEY_ALARM_STATE, PikettState.OFF.name()));
    }

    public static Instant getAlarmStartTime(Context context) {
        return Instant.ofEpochMilli(Long.getLong(getSharedPreferences(context, PREF_KEY_ALARM_START_TIME, START_OF_TIME)));
    }

    public static void setAlarmState(Context context, AlarmState alarmState) {
        if(alarmState == AlarmState.ON && getAlarmState(context) == AlarmState.OFF) {
            setSharedPreferences(context, PREF_KEY_ALARM_START_TIME, String.valueOf(Instant.now().toEpochMilli()));
        }
        setSharedPreferences(context, PREF_KEY_ALARM_STATE, alarmState.name());
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

    public static Instant getSmsLastTestMessageReceivedTime(Context context) {
        return Instant.ofEpochMilli(Long.getLong(getSharedPreferences(context, PREF_KEY_SMS_LAST_TEST_MESSAGE_RECEIVED_TIME, START_OF_TIME)));
    }

    public static void setSmsLastTestMessageReceivedTime(Context context, Instant time) {
        setSharedPreferences(context, PREF_KEY_SMS_LAST_TEST_MESSAGE_RECEIVED_TIME, String.valueOf(time.toEpochMilli()));
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

package com.github.frimtec.android.pikettassist.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.helper.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.YELLOW;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;

public class StateFragment extends AbstractListFragment<State> {

  private static final String DATE_TIME_FORMAT = "dd.MM.yy\nHH:mm:ss";
  private static final String TAG = "StateFragment";

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      State selectedState = (State) listView.getItemAtPosition(position);
      selectedState.onClick(getContext());
    });
  }

  protected ArrayAdapter<State> createAdapter() {
    OnOffState pikettState = SharedState.getPikettState(getContext());
    AlarmState alarmState = SharedState.getAlarmState().first;
    State.TrafficLight alarmTrafficLight;
    String alarmValue;
    if (alarmState == AlarmState.ON) {
      alarmTrafficLight = RED;
      alarmValue = getString(R.string.alarm_state_on);
    } else if (alarmState == AlarmState.ON_CONFIRMED) {
      alarmTrafficLight = YELLOW;
      alarmValue = getString(R.string.alarm_state_on_confirmed);
    } else {
      alarmTrafficLight = pikettState == OnOffState.ON ? GREEN : OFF;
      alarmValue = getString(R.string.alarm_state_off);
    }

    boolean superviseSignalStrength = SharedState.getSuperviseSignalStrength(getContext());
    SignalStrengthHelper.SignalLevel level = SignalStrengthHelper.getSignalStrength(getContext());
    String signalStrength = level.toString(getContext());
    State.TrafficLight signalStrengthTrafficLight;
    if (!superviseSignalStrength) {
      signalStrengthTrafficLight = YELLOW;
    } else if (pikettState == OnOffState.OFF) {
      signalStrengthTrafficLight = OFF;
    } else if (level.ordinal() <= SignalStrengthHelper.SignalLevel.NONE.ordinal()) {
      signalStrengthTrafficLight = RED;
    } else if (level.ordinal() <= SignalStrengthHelper.SignalLevel.POOR.ordinal()) {
      signalStrengthTrafficLight = YELLOW;
    } else {
      signalStrengthTrafficLight = GREEN;
    }

    Supplier<Button> alarmCloseButtonSupplier = null;
    if (alarmState != AlarmState.OFF) {
      alarmCloseButtonSupplier = () -> {
        Button button = new Button(getContext());
        button.setText(getString(R.string.main_state_button_close_alert));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
        button.setOnClickListener(v -> {
          try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("end_time", Instant.now().toEpochMilli());
            int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " is null", null);
            if (update != 1) {
              Log.e(TAG, "One open case expected, but got " + update);
            }
          }
          NotificationHelper.cancel(getContext(), NotificationHelper.ALERT_NOTIFICATION_ID);
          refresh();
        });
        return button;
      };
    }

    Contact operationCenter = ContactHelper.getContact(getContext(), SharedState.getAlarmOperationsCenterContact(getContext()));
    List<State> states = new ArrayList<>(Arrays.asList(
        new State(R.drawable.ic_phone_black_24dp, getString(R.string.state_fragment_operations_center), operationCenter.getName(), null, operationCenter.isValid() ? GREEN : RED, context -> {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          long alarmOperationsCenterContact = SharedState.getAlarmOperationsCenterContact(context);
          if (ContactHelper.getContact(context, alarmOperationsCenterContact).isValid()) {
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(alarmOperationsCenterContact));
            intent.setData(uri);
            startActivity(intent);
          } else {
            Toast.makeText(getContext(), R.string.state_fragment_toast_open_unknown_contact, Toast.LENGTH_SHORT).show();
          }
        }),
        new State(R.drawable.ic_eye, getString(R.string.state_fragment_pikett_state), getString(pikettState == OnOffState.ON ? R.string.pikett_state_on : R.string.pikett_state_off), null, pikettState == OnOffState.ON ? GREEN : OFF, context -> {
          Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
          builder.appendPath("time");
          ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
          Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
          startActivity(intent);
        }),
        new State(R.drawable.ic_siren, getString(R.string.state_fragment_alarm_state), alarmValue, alarmCloseButtonSupplier, alarmTrafficLight, context -> {
        }),
        new State(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp, getString(R.string.state_fragment_signal_level),
            superviseSignalStrength ? (pikettState == OnOffState.ON ? signalStrength : getString(R.string.state_fragment_signal_level_supervise_enabled)) : getString(R.string.state_fragment_signal_level_supervise_disabled), null, signalStrengthTrafficLight, context -> {
        }))
    );
    String lastReceived = getString(R.string.state_fragment_test_alarm_never_received);
    for (String testContext : SharedState.getSuperviseTestContexts(getContext())) {
      OnOffState testAlarmState = OnOffState.OFF;
      Supplier<Button> testAlarmCloseButtonSupplier = null;
      try (SQLiteDatabase db = PAssist.getReadableDatabase()) {
        try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_ID, TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{testContext}, null, null, null)) {
          if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            Instant lastReceiveTime = Instant.ofEpochMilli(cursor.getLong(1));
            lastReceived = formatDateTime(lastReceiveTime);
            testAlarmState = OnOffState.valueOf(cursor.getString(2));

            if (testAlarmState != OnOffState.OFF) {
              testAlarmCloseButtonSupplier = () -> {
                Button button = new Button(getContext());

                button.setText(getString(R.string.main_state_button_close_alert));
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
                button.setOnClickListener(v -> {
                  TestAlarmDao.updateAlarmState(testContext, OnOffState.OFF);
                  refresh();
                });
                return button;
              };
            }
          }
        }
        states.add(new State(R.drawable.ic_test_alarm, testContext, lastReceived, testAlarmCloseButtonSupplier, pikettState == OnOffState.ON ? (testAlarmState == OnOffState.ON ? RED : GREEN) : OFF, context -> {
        }));
      }
    }
    return new StateArrayAdapter(getContext(), states);
  }

  private String formatDateTime(Instant time) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(StateFragment.DATE_TIME_FORMAT, Locale.getDefault())) : "";
  }
}

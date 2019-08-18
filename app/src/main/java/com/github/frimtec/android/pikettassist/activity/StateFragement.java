package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.DualState;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.*;

public class StateFragement extends Fragment {

  private static final String DATE_TIME_FORMAT = "dd.MM.yy\nHH:mm:ss";

  private static final String TAG = "StateFragement";

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_list, container, false);
    ListView listView = view.findViewById(R.id.activity_list);
    listView.setAdapter(createAdapter());
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      State selectedState = (State) listView.getItemAtPosition(position);
      selectedState.onClick(getContext());
    });
    return view;
  }

  private StateArrayAdapter createAdapter() {
    DualState pikettState = SharedState.getPikettState(getContext());
    AlarmState alarmState = SharedState.getAlarmState(getContext()).first;
    State.TrafficLight alarmTrafficLight;
    String alarmValue;
    if (alarmState == AlarmState.ON) {
      alarmTrafficLight = RED;
      alarmValue = getString(R.string.alarm_state_on);
    } else if (alarmState == AlarmState.ON_CONFIRMED) {
      alarmTrafficLight = YELLOW;
      alarmValue = getString(R.string.alarm_state_on_confirmed);
    } else {
      alarmTrafficLight = pikettState == DualState.ON ? GREEN : OFF;
      alarmValue = getString(R.string.alarm_state_off);
    }

    boolean superviseSignalStrength = SharedState.getSuperviseSignalStrength(getContext());
    SignalStremgthHelper.SignalLevel level = SignalStremgthHelper.getSignalStrength(getContext());
    String signalStrength = level.toString(getContext());
    State.TrafficLight signalStrengthTrafficLight;
    if (!superviseSignalStrength) {
      signalStrengthTrafficLight = YELLOW;
    } else if (pikettState == DualState.OFF) {
      signalStrengthTrafficLight = OFF;
    } else if (level.ordinal() <= SignalStremgthHelper.SignalLevel.NONE.ordinal()) {
      signalStrengthTrafficLight = RED;
    } else if (level.ordinal() <= SignalStremgthHelper.SignalLevel.POOR.ordinal()) {
      signalStrengthTrafficLight = YELLOW;
    } else {
      signalStrengthTrafficLight = GREEN;
    }

    Button alarmCloseButton = null;
    if (alarmState != AlarmState.OFF) {
      alarmCloseButton = new Button(getContext());
      alarmCloseButton.setText(getString(R.string.main_state_button_close_alert));
      alarmCloseButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
      alarmCloseButton.setOnClickListener(v -> {
        try (SQLiteDatabase writableDatabase = PikettAssist.getWritableDatabase()) {
          Log.v(TAG, "Close alert button pressed.");
          ContentValues values = new ContentValues();
          values.put("end_time", Instant.now().toEpochMilli());
          int update = writableDatabase.update("t_alert", values, "end_time is null", null);
          if (update != 1) {
            Log.e(TAG, "One open case expected, but got " + update);
          }
        }
        NotificationHelper.cancel(getContext(), NotificationHelper.ALERT_NOTIFICATION_ID);
        refresh();
      });
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
            Toast.makeText(getContext(), R.string.state_fragment_toast_open_unknown_contact,Toast.LENGTH_SHORT).show();
          }
        }),
        new State(R.drawable.ic_eye, getString(R.string.state_fragment_pikett_state), getString(pikettState == DualState.ON ? R.string.pikett_state_on : R.string.pikett_state_off), null, pikettState == DualState.ON ? GREEN : OFF, context -> {
          Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
          builder.appendPath("time");
          ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
          Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
          startActivity(intent);
        }),
        new State(R.drawable.ic_siren, getString(R.string.state_fragment_alarm_state), alarmValue, alarmCloseButton, alarmTrafficLight, context -> {
        }),
        new State(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp, getString(R.string.state_fragment_signal_level),
            superviseSignalStrength ? (pikettState == DualState.ON ? signalStrength : getString(R.string.state_fragment_signal_level_supervise_enabled)) : getString(R.string.state_fragment_signal_level_supervise_disabled), null, signalStrengthTrafficLight, context -> {
        }))
    );
    String lastReceived = getString(R.string.state_fragment_test_alarm_never_received);
    DualState testAlarmState = DualState.OFF;
    for (String testContext : SharedState.getSuperviseTestContexts(getContext())) {
      try (SQLiteDatabase db = PikettAssist.getReadableDatabase()) {
        try (Cursor cursor = db.query("t_test_alarm_state", new String[]{"_id", "last_received_time"}, "_id=?", new String[]{testContext}, null, null, null)) {
          if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            Instant lastReceiveTime = Instant.ofEpochMilli(cursor.getLong(1));
            lastReceived = formatDateTime(lastReceiveTime, DATE_TIME_FORMAT);
            testAlarmState = SharedState.getTestAlarmState(getContext(), testContext);
          }
        }
        states.add(new State(R.drawable.ic_test_alarm, testContext, lastReceived, null, pikettState == DualState.ON ? (testAlarmState == DualState.ON ? RED : GREEN) : OFF, context -> {
        }));
      }
    }
    return new StateArrayAdapter(getContext(), states);
  }

  void refresh() {
    ListView listView = view.findViewById(R.id.activity_list);
    listView.setAdapter(createAdapter());
  }

  private String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }
}

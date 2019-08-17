package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.PikettState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Arrays;

import static com.github.frimtec.android.pikettassist.activity.State.TrafficLight.*;

public class StateFragement extends Fragment {

  private static final String TAG = "StateFragement";

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_list, container, false);
    ListView listView = view.findViewById(R.id.activity_list);
    listView.setAdapter(createAdapter());
    listView.setClickable(false);
    return view;
  }

  private StateArrayAdapter createAdapter() {
    PikettState pikettState = SharedState.getPikettState(getContext());
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
      alarmTrafficLight = pikettState == PikettState.ON ? GREEN : OFF;
      alarmValue = getString(R.string.alarm_state_off);
    }

    boolean superviseSignalStrength = SharedState.getSuperviseSignalStrength(getContext());
    SignalStremgthHelper.SignalLevel level = SignalStremgthHelper.getSignalStrength(getContext());
    String signalStrength = level.toString(getContext());
    State.TrafficLight signalStrengthTrafficLight;
    if (!superviseSignalStrength) {
      signalStrengthTrafficLight = YELLOW;
    } else if (pikettState == PikettState.OFF) {
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
    return new StateArrayAdapter(getContext(), Arrays.asList(
        new State(R.drawable.ic_eye, getString(R.string.state_fragment_pikett_state), getString(pikettState == PikettState.ON ? R.string.pikett_state_on : R.string.pikett_state_off), null, pikettState == PikettState.ON ? GREEN : OFF),
        new State(R.drawable.ic_siren, getString(R.string.state_fragment_alarm_state), alarmValue, alarmCloseButton, alarmTrafficLight),
        new State(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp, getString(R.string.state_fragment_signal_level),
            superviseSignalStrength ? (pikettState == PikettState.ON ? signalStrength : getString(R.string.state_fragment_signal_level_supervise_enabled)) : getString(R.string.state_fragment_signal_level_supervise_disabled), null, signalStrengthTrafficLight))
    );
  }


  void refresh() {
    ListView listView = view.findViewById(R.id.activity_list);
    listView.setAdapter(createAdapter());
  }

}

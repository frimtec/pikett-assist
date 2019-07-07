package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;

public class StateFragement extends Fragment {

  private static final String TAG = "StateFragement";

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_state, container, false);
    TextView textView = (TextView) view.findViewById(R.id.main_state);
    textView.setText(Html.fromHtml("Pikett state: " + SharedState.getPikettState(getContext()) + "<br/>" +
        "Alarm state: " + SharedState.getAlarmState(getContext()).first, Html.FROM_HTML_MODE_COMPACT));

    Button button = (Button) view.findViewById(R.id.close_alert_button);
    button.setOnClickListener(v -> {
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
    refresh();
    return view;
  }


  void refresh() {
    TextView textView = (TextView) view.findViewById(R.id.main_state);
    textView.setText(Html.fromHtml("Pikett state: " + SharedState.getPikettState(getContext()) + "<br/>" +
        "Alarm state: " + SharedState.getAlarmState(getContext()).first, Html.FROM_HTML_MODE_COMPACT));
    textView.invalidate();
    Button button = (Button) view.findViewById(R.id.close_alert_button);
    button.setEnabled(SharedState.getAlarmState(getContext()).first != AlarmState.OFF);
    button.invalidate();
  }

}

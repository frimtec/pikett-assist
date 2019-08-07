package com.github.frimtec.android.pikettassist.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;
import com.github.frimtec.android.pikettassist.state.PikettAssist;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Locale;

public class AlertDetailActivity extends AppCompatActivity {

  private static final String TAG = "AlertDetailActivity";

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMM yyyy HH:mm";
  private static final String TIME_FORMAT = "HH:mm";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_alert_detail);

    Bundle b = getIntent().getExtras();
    if (b != null) {
      long alertId = b.getLong("alertId");
      Alert alert = loadAlert(alertId);
      ListView listView = (ListView) findViewById(R.id.alert_call_list);
      ArrayAdapter<AlertCall> adapter = new AlertCallArrayAdapter(this, alert.getCalls());

      View headerView = getLayoutInflater().inflate(R.layout.activity_alert_detail_header, null);
      TextView timeWindow = (TextView) headerView.findViewById(R.id.alert_detail_header_time_window);
      TextView currentState = (TextView) headerView.findViewById(R.id.alert_detail_header_current_state);
      TextView durations = (TextView) headerView.findViewById(R.id.alert_detail_header_durations);

      timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
      currentState.setText(AlertViewHelper.getState(alert));
      durations.setText(AlertViewHelper.getDurations(alert));

      listView.addHeaderView(headerView);
      listView.setAdapter(adapter);
    }
  }

  private String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }

  private Alert loadAlert(long id) {
    try (SQLiteDatabase db = PikettAssist.getReadableDatabase();
         Cursor alertCursor = db.rawQuery("SELECT start_time, confirm_time, end_time FROM t_alert where _id=?", new String[]{String.valueOf(id)});
         Cursor callCursor = db.rawQuery("SELECT time, message FROM t_alert_call where case_id=? order by time", new String[]{String.valueOf(id)})) {
      if (alertCursor != null && alertCursor.getCount() > 0 && alertCursor.moveToFirst()) {
        LinkedList<AlertCall> calls = new LinkedList<>();
        if (callCursor != null && callCursor.getCount() > 0 && callCursor.moveToFirst()) {
          Instant timeOfLastCall = null;
          do {
            Instant time = Instant.ofEpochMilli(callCursor.getLong(0));
            String message = callCursor.getString(1);
            if (timeOfLastCall != null && Duration.between(timeOfLastCall, time).getSeconds() <= 1) {
              AlertCall alertCall = calls.removeLast();
              alertCall = new AlertCall(alertCall.getTime(), alertCall.getMessage() + message);
              calls.add(alertCall);
            } else {
              calls.add(new AlertCall(time, message));
            }
            timeOfLastCall = time;
          } while (callCursor.moveToNext());
        }
        return new Alert(
            id,
            Instant.ofEpochMilli(alertCursor.getLong(0)),
            alertCursor.getLong(1) > 0 ? Instant.ofEpochMilli(alertCursor.getLong(1)) : null,
            alertCursor.getLong(2) > 0 ? Instant.ofEpochMilli(alertCursor.getLong(2)) : null,
            calls);
      } else {
        throw new IllegalStateException("No alert found with id: " + id);
      }
    }
  }

}

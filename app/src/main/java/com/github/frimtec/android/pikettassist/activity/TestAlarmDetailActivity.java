package com.github.frimtec.android.pikettassist.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE;

public class TestAlarmDetailActivity extends AppCompatActivity {

  public static final String EXTRA_TEST_ALARM_CONTEXT = "testAlarmContext";

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMM yyyy HH:mm:ss";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      String testAlarmContext = extras.getString(EXTRA_TEST_ALARM_CONTEXT);
      setContentView(R.layout.activity_test_alarm_detail);

      TextView context = findViewById(R.id.test_alarm_details_context);
      context.setText(testAlarmContext);

      TextView lastReceived = findViewById(R.id.test_alarm_details_last_received);
      TextView alarmState = findViewById(R.id.test_alarm_details_alarm_state);
      TextView supervised = findViewById(R.id.test_alarm_details_supervised);
      Set<String> superviseTestContexts = SharedState.getSuperviseTestContexts(this);
      supervised.setText(getOnOffText(superviseTestContexts.contains(testAlarmContext)));
      TextView message = findViewById(R.id.test_alarm_details_message);

      try (SQLiteDatabase db = PAssist.getReadableDatabase()) {
        try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_ID, TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE, TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext}, null, null, null)) {
          if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            Instant lastReceiveTime = cursor.getLong(1) > 0 ? Instant.ofEpochMilli(cursor.getLong(1)) : null;
            lastReceived.setText(formatDateTime(lastReceiveTime));
            alarmState.setText(getOnOffText(OnOffState.valueOf(cursor.getString(2)) == OnOffState.ON));
            message.setText(cursor.getString(3));
          }
        }
      }
    }
  }

  private String getOnOffText(boolean on) {
    return on ? getString(R.string.state_on) : getString(R.string.state_off);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private String formatDateTime(Instant time) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault())) : getString(R.string.test_alarm_received_never);
  }

}

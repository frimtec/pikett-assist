package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

public class TestAlarmDetailActivity extends AppCompatActivity {

  public static final String EXTRA_TEST_ALARM_CONTEXT = "testAlarmContext";

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMM yyyy HH:mm:ss";

  private final TestAlarmDao testAlarmDao;

  @SuppressWarnings("unused")
  public TestAlarmDetailActivity() {
    this(new TestAlarmDao());
  }

  TestAlarmDetailActivity(TestAlarmDao testAlarmDao) {
    this.testAlarmDao = testAlarmDao;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      String testAlarmContext = extras.getString(EXTRA_TEST_ALARM_CONTEXT);
      setContentView(R.layout.activity_test_alarm_detail);

      TextView contextText = findViewById(R.id.test_alarm_details_context);
      contextText.setText(testAlarmContext);
      TestAlarmContext testAlarm = new TestAlarmContext(testAlarmContext);

      TextView lastReceived = findViewById(R.id.test_alarm_details_last_received);
      TextView alarmState = findViewById(R.id.test_alarm_details_alarm_state);
      SwitchCompat supervisedSwitch = findViewById(R.id.test_alarm_enabling_switch);
      supervisedSwitch.setChecked(ApplicationPreferences.instance().getSupervisedTestAlarms(this).contains(testAlarm));
      supervisedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        Set<TestAlarmContext> supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(this);
        if (isChecked) {
          supervisedTestAlarmContexts.add(testAlarm);
        } else {
          supervisedTestAlarmContexts.remove(testAlarm);
        }
        ApplicationPreferences.instance().setSuperviseTestContexts(this, supervisedTestAlarmContexts);
      });
      TextView message = findViewById(R.id.test_alarm_details_message);

      this.testAlarmDao.loadDetails(testAlarm).ifPresent(details -> {
        lastReceived.setText(formatDateTime(details.receivedTime()));
        alarmState.setText(getOnOffText(details.alertState()));
        message.setText(details.message());
      });
    }
  }

  private String getOnOffText(OnOffState state) {
    return state == OnOffState.ON ? getString(R.string.state_on) : getString(R.string.state_off);
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

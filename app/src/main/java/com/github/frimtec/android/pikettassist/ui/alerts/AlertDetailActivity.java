package com.github.frimtec.android.pikettassist.ui.alerts;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;

public class AlertDetailActivity extends AppCompatActivity {

  public static final String EXTRA_ALERT_ID = "alertId";

  private final AlertDao alertDao;

  @SuppressWarnings("unused")
  public AlertDetailActivity() {
    this(new AlertDao());
  }

  AlertDetailActivity(AlertDao alertDao) {
    this.alertDao = alertDao;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_alert_detail);

    Bundle b = getIntent().getExtras();
    if (b != null) {
      Alert alert = this.alertDao.load(b.getLong(EXTRA_ALERT_ID));
      ListView listView = findViewById(R.id.alert_call_list);
      ArrayAdapter<AlertCall> adapter = new AlertCallArrayAdapter(this, alert.getCalls());

      View headerView = getLayoutInflater().inflate(R.layout.activity_alert_detail_header, listView, false);
      TextView timeWindow = headerView.findViewById(R.id.alert_detail_header_time_window);
      TextView currentState = headerView.findViewById(R.id.alert_detail_header_current_state);
      TextView durations = headerView.findViewById(R.id.alert_detail_header_durations);

      timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
      currentState.setText(AlertViewHelper.getState(this, alert));
      durations.setText(AlertViewHelper.getDurations(this, alert));

      listView.addHeaderView(headerView);
      listView.setAdapter(adapter);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      // Override home navigation button to call onBackPressed (b/35152749).
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}

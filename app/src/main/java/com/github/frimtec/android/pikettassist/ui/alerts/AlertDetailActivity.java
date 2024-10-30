package com.github.frimtec.android.pikettassist.ui.alerts;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.ui.common.BaseActivity;

public class AlertDetailActivity extends BaseActivity {

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
  protected void doOnCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_alert_detail);

    Bundle b = getIntent().getExtras();
    if (b != null) {
      Alert alert = this.alertDao.load(b.getLong(EXTRA_ALERT_ID));
      ListView listView = findViewById(R.id.alert_call_list);
      ArrayAdapter<AlertCall> adapter = new AlertCallArrayAdapter(this, alert.calls());

      View headerView = getLayoutInflater().inflate(R.layout.activity_alert_detail_header, listView, false);

      ImageView playIcon = headerView.findViewById(R.id.alert_detail_header_image_play);
      playIcon.setVisibility(alert.isClosed() ? View.INVISIBLE : View.VISIBLE);

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
}

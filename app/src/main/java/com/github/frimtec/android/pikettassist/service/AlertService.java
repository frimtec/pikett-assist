package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.service.system.NotificationService.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.util.GsonHelper.GSON;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.pikettassist.ui.MainActivity;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertActivity;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class AlertService {

  private static final String TAG = "AlertService";

  private final Context context;
  private final AlertDao alertDao;
  private final AcknowledgmentService acknowledgmentService;
  private final NotificationService notificationService;

  public AlertService(Context context) {
    this(context, new AlertDao());
  }

  AlertService(Context context, AlertDao alertDao) {
    this.context = context;
    this.alertDao = alertDao;
    this.acknowledgmentService = new AcknowledgmentService(context, new SmsService(context));
    this.notificationService = new NotificationService(context);
  }

  public void newAlert(Sms sms) {
    ApplicationState applicationState = ApplicationState.instance();
    applicationState.addLastAlarmSms(sms);
    switch (
        this.alertDao.insertOrUpdateAlert(
            Instant.now(),
            sms.getText(),
            false,
            ApplicationPreferences.instance().getAutoConfirmTime(context)
        )
    ) {
      case TRIGGER -> AlertActivity.trigger(context);
      case UNCHANGED -> {
        // nothing to do
      }
      case AUTO_CONFIRMED -> {
        this.acknowledgmentService.acknowledge(List.of(sms));
        applicationState.clearLastAlarmSms();
        Toast.makeText(context, context.getText(R.string.toast_alert_auto_confirm), Toast.LENGTH_LONG).show();
      }
    }
  }

  public void newManuallyAlert(Instant startTime, String reason) {
    this.alertDao.insertOrUpdateAlert(startTime, String.format("%s: %s", context.getString(R.string.manually_created_alarm_reason_prefix), reason), true, Duration.ZERO);
    notificationService.notifyAlarm(
        new Intent(context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        context.getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  public void confirmAlert() {
    ApplicationState applicationState = ApplicationState.instance();
    List<Sms> receivedSms = applicationState.getLastAlarmSms();
    try {
      this.alertDao.confirmOpenAlert();
      this.acknowledgmentService.acknowledge(receivedSms);
    } finally {
      applicationState.clearLastAlarmSms();
    }
    notificationService.notifyAlarm(
        new Intent(this.context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        this.context.getString(R.string.alert_action_close),
        new Intent(this.context, MainActivity.class)
    );
  }

  public void closeAlert() {
    this.alertDao.closeOpenAlert();
    notificationService.cancelNotification(NotificationService.ALERT_NOTIFICATION_ID);
  }

  public String exportAllAlerts() {
    return GSON.toJson(
        alertDao.loadAll().stream()
            .map(alert -> alertDao.load(alert.id()))
            .collect(Collectors.toList())
    );
  }

  public boolean importAllAlerts(String alertsAsJson) {
    Type listType = new TypeToken<List<Alert>>() {
    }.getType();
    List<Alert> alerts;
    try {
      alerts = GSON.fromJson(alertsAsJson, listType);
      if (alerts == null || alerts.isEmpty()) {
        Log.w(TAG, "Empty list of alerts are not imported");
        return false;
      }
    } catch (JsonSyntaxException e) {
      Log.e(TAG, "Alerts cannot be de-serialized", e);
      return false;
    }
    alertDao.loadAll().forEach(alertDao::delete);
    alerts.forEach(alertDao::saveImportedAlert);
    return true;
  }

}

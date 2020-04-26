package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

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

import org.threeten.bp.Instant;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.service.system.NotificationService.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.util.GsonHelper.GSON;

public class AlertService {

  private static final String TAG = "AlertService";

  private final Context context;
  private final AlertDao alertDao;
  private final SmsService smsService;
  private final NotificationService notificationService;

  public AlertService(Context context) {
    this(context, new AlertDao());
  }

  AlertService(Context context, AlertDao alertDao) {
    this.context = context;
    this.alertDao = alertDao;
    this.smsService = new SmsService(context);
    this.notificationService = new NotificationService(context);
  }

  public void newAlert(Sms sms) {
    ApplicationState.setLastAlarmSmsNumberWithSubscriptionId(sms.getNumber(), sms.getSubscriptionId());
    if (this.alertDao.insertOrUpdateAlert(Instant.now(), sms.getText(), false)) {
      AlertActivity.trigger(sms.getNumber(), sms.getSubscriptionId(), context);
    }
  }

  public void newManuallyAlert(Instant startTime, String reason) {
    this.alertDao.insertOrUpdateAlert(startTime, String.format("%s: %s", context.getString(R.string.manually_created_alarm_reason_prefix), reason), true);
    notificationService.notifyAlarm(
        new Intent(context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        context.getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  public void confirmAlert() {
    Pair<String, Integer> smsNumberWithSubscriptionId = ApplicationState.getLastAlarmSmsNumberWithSubscriptionId();
    confirmAlert(this.context, smsNumberWithSubscriptionId.first, smsNumberWithSubscriptionId.second);
  }

  public void confirmAlert(Context context, String smsNumber, Integer subscriptionId) {
    this.alertDao.confirmOpenAlert();
    this.smsService.sendSms(ApplicationPreferences.getSmsConfirmText(context), smsNumber, subscriptionId);
    notificationService.notifyAlarm(
        new Intent(context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        context.getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  public void closeAlert() {
    this.alertDao.closeOpenAlert();
    notificationService.cancelNotification(NotificationService.ALERT_NOTIFICATION_ID);
  }

  public String exportAllAlerts() {
    return GSON.toJson(
        alertDao.loadAll().stream()
            .map(alert -> alertDao.load(alert.getId()))
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

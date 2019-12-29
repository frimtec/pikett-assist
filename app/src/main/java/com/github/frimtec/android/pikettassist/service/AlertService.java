package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.ui.MainActivity;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertActivity;
import com.github.frimtec.android.pikettassist.utility.NotificationHelper;
import com.github.frimtec.android.pikettassist.utility.SmsHelper;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.threeten.bp.Instant;

import static com.github.frimtec.android.pikettassist.utility.NotificationHelper.ACTION_CLOSE_ALARM;

public class AlertService {

  private final Context context;
  private final AlertDao alertDao;

  public AlertService(Context context) {
    this(context, new AlertDao());
  }

  AlertService(Context context, AlertDao alertDao) {
    this.context = context;
    this.alertDao = alertDao;
  }

  public void newAlert(Sms sms) {
    SharedState.setLastAlarmSmsNumberWithSubscriptionId(context, sms.getNumber(), sms.getSubscriptionId());
    if (this.alertDao.insertOrUpdateAlert(Instant.now(), sms.getText(), false)) {
      AlertActivity.trigger(sms.getNumber(), sms.getSubscriptionId(), context);
    }
  }

  public void newManuallyAlert(Instant startTime, String reason) {
    this.alertDao.insertOrUpdateAlert(startTime, String.format("%s: %s", context.getString(R.string.manually_created_alarm_reason_prefix), reason), true);
    NotificationHelper.notifyAlarm(
        context,
        new Intent(context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        context.getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  public void confirmAlert() {
    final Pair<String, Integer> smsNumberWithSubscriptionId = SharedState.getLastAlarmSmsNumberWithSubscriptionId(context);
    confirmAlert(context, smsNumberWithSubscriptionId.first, smsNumberWithSubscriptionId.second);
  }

  public void confirmAlert(Context context, String smsNumber, Integer subscriptionId) {
    this.alertDao.confirmOpenAlert();
    SmsHelper.confirmSms(context, SharedState.getSmsConfirmText(context), smsNumber, subscriptionId);
    NotificationHelper.notifyAlarm(
        context,
        new Intent(context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        context.getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  public void closeAlert() {
    this.alertDao.closeOpenAlert();
    NotificationHelper.cancelNotification(context, NotificationHelper.ALERT_NOTIFICATION_ID);
  }
}

package com.github.frimtec.android.pikettassist.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.ContactDao;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  private final TestAlarmDao testAlarmDao;

  public SmsListener() {
    this(new TestAlarmDao());
  }

  SmsListener(TestAlarmDao testAlarmDao) {
    this.testAlarmDao = testAlarmDao;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    ContactDao contactDao = new ContactDao(context);
    ShiftService shiftService = new ShiftService(context);
    if (Action.SMS_RECEIVED.getId().equals(intent.getAction())) {
      SmsService smsService = new SmsService(context);
      Log.d(TAG, "SMS received");
      List<Sms> receivedSms = smsService.getSmsFromReceivedIntent(intent);
      receivedSms.stream()
          .filter(sms -> SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK.equals(sms.getNumber()))
          .forEach(sms -> Toast.makeText(context, context.getString(R.string.sms_listener_loopback_sms_received), Toast.LENGTH_SHORT).show());
      if (shiftService.getState() == OnOffState.OFF) {
        Log.d(TAG, "Drop SMS, not on-call");
        return;
      }
      long operationCenterContactId = SharedState.getAlarmOperationsCenterContact(context);
      for (Sms sms : receivedSms) {
        Set<Long> contactIds = contactDao.lookupContactIdByPhoneNumber(sms.getNumber());
        if (operationCenterContactId != SharedState.EMPTY_CONTACT && contactIds.contains(operationCenterContactId)) {
          Log.i(TAG, "SMS from pikett number");
          Pattern testSmsPattern = Pattern.compile(SharedState.getSmsTestMessagePattern(context), Pattern.DOTALL);
          Matcher matcher = testSmsPattern.matcher(sms.getText());
          if (SharedState.getTestAlarmEnabled(context) && matcher.matches()) {
            TestAlarmContext testAlarmContext = new TestAlarmContext(matcher.groupCount() > 0 ? matcher.group(1) : context.getString(R.string.test_alarm_context_general));
            Log.i(TAG, "TEST alarm with context: " + testAlarmContext.getContext());
            smsService.sendSms(SharedState.getSmsConfirmText(context), sms.getNumber(), sms.getSubscriptionId());
            if (this.testAlarmDao.updateReceivedTestAlert(testAlarmContext, Instant.now(), sms.getText())) {
              Set<TestAlarmContext> supervisedTestAlarmContexts = SharedState.getSupervisedTestAlarms(context);
              supervisedTestAlarmContexts.add(testAlarmContext);
              SharedState.setSuperviseTestContexts(context, supervisedTestAlarmContexts);
            }
          } else {
            Log.i(TAG, "New alert");
            new AlertService(context).newAlert(sms);
          }
        }
      }
      context.sendBroadcast(new Intent(Action.REFRESH.getId()));
    }
  }

}

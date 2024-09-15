package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcknowledgmentService {

  private static final String TAG = AcknowledgmentService.class.getSimpleName();

  private final Context context;
  private final SmsService smsService;
  private final ApplicationPreferences applicationPreferences;
  private final Consumer<Context> notifyHandlerSkipAcknowledge;

  public AcknowledgmentService(Context context, SmsService smsService) {
    this(
        context,
        smsService,
        ApplicationPreferences.instance(),
        ctx -> Toast.makeText(
            ctx,
            ctx.getString(R.string.toast_acknowledge_skipped),
            Toast.LENGTH_LONG
        ).show()
    );
  }

  AcknowledgmentService(
      Context context,
      SmsService smsService,
      ApplicationPreferences applicationPreferences,
      Consumer<Context> notifyHandlerSkipAcknowledge
  ) {
    this.context = context;
    this.smsService = smsService;
    this.applicationPreferences = applicationPreferences;
    this.notifyHandlerSkipAcknowledge = notifyHandlerSkipAcknowledge;
  }

  public void acknowledge(Sms receivedSms) {
    switch (this.applicationPreferences.getAlertConfirmMethod(context)) {
      case SMS_STATIC_TEXT -> this.smsService.sendSms(
          this.applicationPreferences.getSmsConfirmText(context),
          receivedSms.getNumber(),
          receivedSms.getSubscriptionId()
      );
      case SMS_DYNAMIC_TEXT -> {
        String pattern = this.applicationPreferences.getSmsConfirmPattern(context);
        Matcher matcher = Pattern.compile(pattern).matcher(receivedSms.getText());
        if (matcher.find() && matcher.groupCount() >= 1) {
          this.smsService.sendSms(
              matcher.group(1),
              receivedSms.getNumber(),
              receivedSms.getSubscriptionId()
          );
        } else {
          Log.w(TAG, "No value found in received SMS with extraction pattern '" + pattern + "', acknowledgement not sent!");
          notifyHandlerSkipAcknowledge.accept(context);
        }
      }
      case NO_ACKNOWLEDGE -> Log.i(TAG, "Alert acknowledgement disabled.");
    }
  }
}

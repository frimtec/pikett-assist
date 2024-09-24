package com.github.frimtec.android.pikettassist.service.confirmation;

import android.util.Log;

import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class DynamicSmsConfirmStrategy implements ConfirmStrategy {

  private static final String TAG = DynamicSmsConfirmStrategy.class.getSimpleName();

  private final SmsService smsService;
  private final Supplier<String> textExtractionPatternSupplier;

  public DynamicSmsConfirmStrategy(
      SmsService smsService,
      Supplier<String> textExtractionPatternSupplier
  ) {
    this.smsService = smsService;
    this.textExtractionPatternSupplier = textExtractionPatternSupplier;
  }

  @Override
  public boolean confirm(Sms receivedAlarmSms) {
    String pattern = this.textExtractionPatternSupplier.get();
    Matcher matcher;
    try {
      matcher = Pattern.compile(pattern).matcher(receivedAlarmSms.getText());
    } catch (PatternSyntaxException e) {
      Log.e(TAG, "Invalid extraction pattern '" + pattern + "'", e);
      return false;
    }
    if (matcher.find() && matcher.groupCount() >= 1) {
      this.smsService.sendSms(
          matcher.group(1),
          receivedAlarmSms.getNumber(),
          receivedAlarmSms.getSubscriptionId()
      );
      return true;
    } else {
      Log.w(TAG, "No value found in received SMS with extraction pattern '" + pattern + "'");
      return false;
    }
  }
}
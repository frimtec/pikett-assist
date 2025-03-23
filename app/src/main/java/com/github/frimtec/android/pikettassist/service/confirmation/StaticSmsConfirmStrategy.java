package com.github.frimtec.android.pikettassist.service.confirmation;

import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StaticSmsConfirmStrategy implements ConfirmStrategy {

  private final SmsService smsService;
  private final Supplier<String> textSupplier;

  public StaticSmsConfirmStrategy(
      SmsService smsService,
      Supplier<String> textSupplier
  ) {
    this.smsService = smsService;
    this.textSupplier = textSupplier;
  }

  @Override
  public boolean confirm(List<Sms> receivedAlarmSms) {
    receivedAlarmSms.stream()
        .collect(Collectors.groupingBy(Sms::getNumber))
        .forEach((key, value) -> confirm(value.get(0)));
    return true;
  }

  @Override
  public boolean confirm(Sms receivedAlarmSms) {
    this.smsService.sendSms(
        textSupplier.get(),
        receivedAlarmSms.getNumber(),
        receivedAlarmSms.getSubscriptionId()
    );
    return true;
  }
}

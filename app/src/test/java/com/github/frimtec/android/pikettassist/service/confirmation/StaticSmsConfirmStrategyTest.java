package com.github.frimtec.android.pikettassist.service.confirmation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.jupiter.api.Test;

import java.util.List;

class StaticSmsConfirmStrategyTest {

  @Test
  void confirm() {
    SmsService smsService = mock(SmsService.class);
    ConfirmStrategy strategy = new StaticSmsConfirmStrategy(smsService, () -> "OK");
    assertTrue(strategy.confirm(new Sms("111", "ANY", 1)));
    verify(smsService).sendSms("OK", "111", 1);
  }

  @Test
  void confirmMultipleSms() {
    SmsService smsService = mock(SmsService.class);
    ConfirmStrategy strategy = new StaticSmsConfirmStrategy(smsService, () -> "OK");
    assertTrue(strategy.confirm(List.of(
        new Sms("111", "ANY1", 1),
        new Sms("111", "ANY2", 1),
        new Sms("112", "ANY3", 1)
    )));
    verify(smsService).sendSms("OK", "111", 1);
    verify(smsService).sendSms("OK", "112", 1);
    verifyNoMoreInteractions(smsService);
  }
}
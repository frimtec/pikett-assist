package com.github.frimtec.android.pikettassist.service.confirmation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.jupiter.api.Test;

class DynamicSmsConfirmStrategyTest {

  @Test
  void confirm() {
    SmsService smsService = mock(SmsService.class);
    ConfirmStrategy strategy = new DynamicSmsConfirmStrategy(smsService, () -> "<(.*?)>");
    assertTrue(strategy.confirm(new Sms("111", "ANY <abc123> ANY", 1)));
    verify(smsService).sendSms("abc123", "111", 1);
  }

  @Test
  void confirmNoTextFound() {
    SmsService smsService = mock(SmsService.class);
    ConfirmStrategy strategy = new DynamicSmsConfirmStrategy(smsService, () -> "<(.*?)>");
    assertFalse(strategy.confirm(new Sms("111", "ANY (abc123) ANY", 1)));
    verifyNoInteractions(smsService);
  }

  @Test
  void confirmBadPattern() {
    SmsService smsService = mock(SmsService.class);
    ConfirmStrategy strategy = new DynamicSmsConfirmStrategy(smsService, () -> "<(.*?>");
    assertFalse(strategy.confirm(new Sms("111", "ANY <abc123> ANY", 1)));
    verifyNoInteractions(smsService);
  }

  @Test
  void confirmNoGroupInPattern() {
    SmsService smsService = mock(SmsService.class);
    ConfirmStrategy strategy = new DynamicSmsConfirmStrategy(smsService, () -> "<.*>");
    assertFalse(strategy.confirm(new Sms("111", "ANY <abc123> ANY", 1)));
    verifyNoInteractions(smsService);
  }
}
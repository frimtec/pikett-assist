package com.github.frimtec.android.pikettassist.service.confirmation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.jupiter.api.Test;

import java.util.List;

class NoConfirmStrategyTest {

  @Test
  void confirm() {
    ConfirmStrategy strategy = new NoConfirmStrategy();
    assertTrue(strategy.confirm(List.of(new Sms(null, null))));
  }

}
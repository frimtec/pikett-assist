package com.github.frimtec.android.pikettassist.service.confirmation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NoConfirmStrategyTest {

  @Test
  void confirm() {
    ConfirmStrategy strategy = new NoConfirmStrategy();
    assertTrue(strategy.confirm(null));
  }

}
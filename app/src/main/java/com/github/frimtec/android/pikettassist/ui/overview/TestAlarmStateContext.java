package com.github.frimtec.android.pikettassist.ui.overview;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;

class TestAlarmStateContext {

  private final StateContext stateContext;
  private final TestAlarmContext testAlarmContext;
  private final String lastReceived;
  private final OnOffState testAlarmState;

  TestAlarmStateContext(StateContext stateContext, TestAlarmContext testAlarmContext, String lastReceived, OnOffState testAlarmState) {
    this.stateContext = stateContext;
    this.testAlarmContext = testAlarmContext;
    this.lastReceived = lastReceived;
    this.testAlarmState = testAlarmState;
  }

  StateContext getStateContext() {
    return stateContext;
  }

  TestAlarmContext getTestAlarmContext() {
    return testAlarmContext;
  }

  String getLastReceived() {
    return lastReceived;
  }

  OnOffState getTestAlarmState() {
    return testAlarmState;
  }
}

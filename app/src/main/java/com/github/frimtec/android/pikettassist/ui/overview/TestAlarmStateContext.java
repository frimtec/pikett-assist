package com.github.frimtec.android.pikettassist.ui.overview;

import com.github.frimtec.android.pikettassist.domain.TestAlarm;

record TestAlarmStateContext(
    StateContext stateContext,
    TestAlarm testAlarm,
    String lastReceived
) {

}

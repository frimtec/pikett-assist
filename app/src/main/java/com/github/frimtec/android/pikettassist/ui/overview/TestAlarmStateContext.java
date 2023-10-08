package com.github.frimtec.android.pikettassist.ui.overview;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;

record TestAlarmStateContext(
    StateContext stateContext,
    TestAlarmContext testAlarmContext,
    String lastReceived,
    OnOffState testAlarmState
) {

}

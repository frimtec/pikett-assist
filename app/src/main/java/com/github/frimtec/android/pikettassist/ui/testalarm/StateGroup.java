package com.github.frimtec.android.pikettassist.ui.testalarm;

import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;

import java.util.List;

public record StateGroup(boolean active, List<TestAlarmContext> testAlarmContexts) {

}

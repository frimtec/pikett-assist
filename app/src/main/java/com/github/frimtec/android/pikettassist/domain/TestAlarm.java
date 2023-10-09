package com.github.frimtec.android.pikettassist.domain;

import java.time.Instant;

public record TestAlarm(
    TestAlarmContext context,
    Instant receivedTime,
    OnOffState alertState,
    String message
) {

}

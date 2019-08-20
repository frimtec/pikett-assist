package com.github.frimtec.android.pikettassist.service;

import android.util.Log;
import com.github.frimtec.android.pikettassist.state.SharedState;
import org.junit.Assert;
import org.junit.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestAlertServiceTest {

    @Test
    public void isTestMessageAvailableForAcceptableTime() {
        // arrange
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(5);
        Instant lastReceiveTime = Instant.now().minusSeconds((4 * 60) - 1);

        // act
        boolean result = isTestMessageAvailable(messageAcceptedTime.toInstant(), lastReceiveTime);

        // assert
        assertThat(result, is(true));
    }

    @Test
    public void isTestMessageAvailableForNonAcceptableTime() {
        // arrange
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(5);
        Instant lastReceiveTime = Instant.now().minusSeconds((6 * 60) + 1);

        // act
        boolean result = isTestMessageAvailable(messageAcceptedTime.toInstant(), lastReceiveTime);

        // assert
        assertThat(result, is(false));
    }

    private ZonedDateTime getTodaysCheckTime(ZonedDateTime now) {
        String[] testAlarmCheckTime = now.format(DateTimeFormatter.ofPattern("HH:mm")).split(":");
        return now.truncatedTo(ChronoUnit.MINUTES).with(ChronoField.HOUR_OF_DAY, Integer.parseInt(testAlarmCheckTime[0])).with(ChronoField.MINUTE_OF_HOUR, Integer.parseInt(testAlarmCheckTime[1]));
    }

    private boolean isTestMessageAvailable(Instant messageAcceptedTime, Instant lastReceiveTime) {
        return lastReceiveTime.isAfter(messageAcceptedTime);
    }
}

package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class AlertTest {

  @Test
  void getId() {
    long id = 12L;
    Alert alert = new Alert(id, Instant.now(), Instant.now(), true, Instant.now(), Collections.emptyList());
    assertThat(alert.getId()).isEqualTo(id);
  }

  @Test
  void getStartTime() {
    Instant startTime = Instant.now().minusSeconds(10);
    Alert alert = new Alert(12L, startTime, Instant.now(), true, Instant.now(), Collections.emptyList());
    assertThat(alert.getStartTime()).isEqualTo(startTime);
  }

  @Test
  void getConfirmTime() {
    Instant confirmTime = Instant.now().minusSeconds(10);
    Alert alert = new Alert(12L, Instant.now(), confirmTime, true, Instant.now(), Collections.emptyList());
    assertThat(alert.getConfirmTime()).isEqualTo(confirmTime);
  }

  @Test
  void isConfirmed() {
    boolean confirmed = true;
    Alert alert = new Alert(12L, Instant.now(), Instant.now(), confirmed, Instant.now(), Collections.emptyList());
    assertThat(alert.isConfirmed()).isEqualTo(confirmed);
  }

  @Test
  void getEndTime() {
    Instant endTime = Instant.now().minusSeconds(10);
    Alert alert = new Alert(12L, Instant.now(), Instant.now(), true, endTime, Collections.emptyList());
    assertThat(alert.getEndTime()).isEqualTo(endTime);
  }

  @Test
  void getCalls() {
    AlertCall msg1 = new AlertCall(Instant.now(), "msg1");
    AlertCall msg2 = new AlertCall(msg1.getTime().plusMillis(1), "msg2");
    List<AlertCall> calls = Arrays.asList(
        msg2,
        msg1
    );
    Alert alert = new Alert(12L, Instant.now(), Instant.now(), true, Instant.now(), calls);
    assertThat(alert.getCalls()).isEqualTo(Arrays.asList(msg1, msg2));
  }

  @Test
  void isClosedForNoEndTimeReturnsFalse() {
    Alert alert = new Alert(12L, Instant.now(), Instant.now(), true, null, Collections.emptyList());
    assertThat(alert.isClosed()).isFalse();
  }

  @Test
  void isClosedForEndTimeReturnsTrue() {
    Instant endTime = Instant.now().minusSeconds(10);
    Alert alert = new Alert(12L, Instant.now(), Instant.now(), true, endTime, Collections.emptyList());
    assertThat(alert.isClosed()).isTrue();
  }

  @Test
  void testToString() {
    Instant now = Instant.now();
    Alert alert = new Alert(12L, now, now, true, now, Arrays.asList(
        new AlertCall(now.plusMillis(1), "msg2"),
        new AlertCall(now, "msg1")
    ));
    assertThat(alert.toString()).matches("Alert\\{id=12, startTime=.*, confirmTime=.*, confirmed=true, endTime=.*, calls=\\[AlertCall\\{time=.*, message='msg1'}, AlertCall\\{time=.*, message='msg2'}]}");
  }
}
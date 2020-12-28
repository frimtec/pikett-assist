package com.github.frimtec.android.pikettassist.domain;

import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AlertCallTest {

  @Test
  void getTime() {
    Instant time = Instant.now().minusSeconds(10);
    AlertCall alertCall = new AlertCall(time, "message");
    assertThat(alertCall.getTime()).isEqualTo(time);
  }

  @Test
  void getMessage() {
    String message = "message";
    AlertCall alertCall = new AlertCall(Instant.now(), message);
    assertThat(alertCall.getMessage()).isEqualTo(message);
  }

  @Test
  void testToString() {
    Instant now = Instant.now();
    AlertCall alertCall = new AlertCall(now, "message");
    assertThat(alertCall.toString()).matches("AlertCall\\{time=.*, message='message'}");
  }
}
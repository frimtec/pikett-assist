package com.github.frimtec.android.pikettassist.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.frimtec.android.pikettassist.domain.Alert;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;


class GsonHelperTest {

  @Test
  void serializeAlert() {
    Instant startTime = Instant.parse("2020-04-26T18:11:06.641Z");
    Instant confirmationTime = startTime.plusSeconds(12);
    Instant endTime = startTime.plusSeconds(200);
    Alert alert = new Alert(5, startTime, confirmationTime, true, endTime, Arrays.asList(
        new Alert.AlertCall(startTime.minusMillis(20), "Message 1"),
        new Alert.AlertCall(startTime.plusSeconds(20), "Message 2")
    ));
    String jsonString = GsonHelper.GSON.toJson(alert);
    assertThat(jsonString).isEqualTo("""
        {
          "id": 5,
          "startTime": "2020-04-26T18:11:06.641Z",
          "confirmTime": "2020-04-26T18:11:18.641Z",
          "confirmed": true,
          "endTime": "2020-04-26T18:14:26.641Z",
          "calls": [
            {
              "time": "2020-04-26T18:11:06.621Z",
              "message": "Message 1"
            },
            {
              "time": "2020-04-26T18:11:26.641Z",
              "message": "Message 2"
            }
          ]
        }""");

    Alert alertDeserialized = GsonHelper.GSON.fromJson(jsonString, Alert.class);
    assertThat(alertDeserialized.toString()).isEqualTo(alert.toString());
  }

}
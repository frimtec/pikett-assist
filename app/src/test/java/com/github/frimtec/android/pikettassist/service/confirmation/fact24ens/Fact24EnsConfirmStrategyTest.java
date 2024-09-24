package com.github.frimtec.android.pikettassist.service.confirmation.fact24ens;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.frimtec.android.pikettassist.service.confirmation.ConfirmStrategy;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import retrofit2.Call;

class Fact24EnsConfirmStrategyTest {

  private static final String REQUEST_ID = "request-id";
  private static final String TOKEN = "9bc93b37-784a-474a-9b45-20fd4b733ae9";
  private static final String API_BASE_URL = "http://localhost:8080";

  private WireMockServer wireMockServer;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(options().port(8080)); // Choose any available port
    wireMockServer.start();
    WireMock.configureFor("localhost", 8080);
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void confirm() {
    Fact24EnsConfirmService service = Fact24EnsConfirmStrategy.buildFact24EnsService(API_BASE_URL);
    ConfirmStrategy strategy = new Fact24EnsConfirmStrategy(service);

    stubFor(get(urlEqualTo("/ens/api/alarm/alarm-use-case/" + TOKEN))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"requestId\": \"%s\" }".formatted(REQUEST_ID))));
    stubFor(put(urlEqualTo("/ens/api/alarm/feedback/" + REQUEST_ID))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"requestId\": \"%s\" }".formatted(REQUEST_ID))));

    assertTrue(
        strategy.confirm(
            new Sms(
                "111",
                "You can confirm the alarm via this link: https://ensplus.fact24.com/ens/#/c?t=%s ...".formatted(TOKEN),
                1
            )
        )
    );
  }

  @Test
  void confirmAlreadyClosedUseCase() {
    Fact24EnsConfirmService service = Fact24EnsConfirmStrategy.buildFact24EnsService(API_BASE_URL);
    ConfirmStrategy strategy = new Fact24EnsConfirmStrategy(service);

    stubFor(get(urlEqualTo("/ens/api/alarm/alarm-use-case/" + TOKEN))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"requestId\": \"%s\" }".formatted(REQUEST_ID))));

    assertFalse(
        strategy.confirm(
            new Sms(
                "111",
                "You can confirm the alarm via this link: https://ensplus.fact24.com/ens/#/c?t=%s ...".formatted(TOKEN),
                1
            )
        )
    );
  }

  @Test
  void confirmAlreadyUnsuccessfulFeedback() {
    Fact24EnsConfirmService service = Fact24EnsConfirmStrategy.buildFact24EnsService(API_BASE_URL);
    ConfirmStrategy strategy = new Fact24EnsConfirmStrategy(service);

    stubFor(get(urlEqualTo("/ens/api/alarm/alarm-use-case/" + TOKEN))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"requestId\": \"%s\" }".formatted(REQUEST_ID))));
    stubFor(put(urlEqualTo("/ens/api/alarm/feedback/" + REQUEST_ID))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"requestId\": \"%s\" }".formatted(REQUEST_ID))));

    assertFalse(
        strategy.confirm(
            new Sms(
                "111",
                "You can confirm the alarm via this link: https://ensplus.fact24.com/ens/#/c?t=%s ...".formatted(TOKEN),
                1
            )
        )
    );
  }

  @Test
  void confirmAlreadyIoException() throws IOException {
    Fact24EnsConfirmService service = mock(Fact24EnsConfirmService.class);
    //noinspection unchecked
    Call<AlarmUseCase> call = mock(Call.class);
    when(service.getAlarmUseCase(TOKEN)).thenReturn(call);
    when(call.execute()).thenThrow(new IOException());
    ConfirmStrategy strategy = new Fact24EnsConfirmStrategy(service);

    assertFalse(
        strategy.confirm(
            new Sms(
                "111",
                "You can confirm the alarm via this link: https://ensplus.fact24.com/ens/#/c?t=%s ...".formatted(TOKEN),
                1
            )
        )
    );
  }

  @Test
  void confirmNoTokenFound() {
    Fact24EnsConfirmService service = mock(Fact24EnsConfirmService.class);
    ConfirmStrategy strategy = new Fact24EnsConfirmStrategy(service);
    assertFalse(
        strategy.confirm(
            new Sms(
                "111",
                "You can confirm the alarm",
                1
            )
        )
    );
  }

}
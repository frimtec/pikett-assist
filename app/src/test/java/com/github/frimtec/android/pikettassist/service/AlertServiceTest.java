package com.github.frimtec.android.pikettassist.service;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AlertServiceTest {

  @Test
  void exportAllAlerts() {
    AlertDao dao = mock(AlertDao.class);
    AlertService alertService = new AlertService(mock(Context.class), dao);
    Instant startTime = Instant.parse("2020-04-26T18:11:06.641Z");
    when(dao.loadAll()).thenReturn(
        Arrays.asList(
            new Alert(1L, startTime, null, false, null, Collections.emptyList()),
            new Alert(2L, startTime, null, false, null, Collections.emptyList())
        )
    );
    when(dao.load(1L)).thenReturn(new Alert(1L, startTime, null, false, null, Collections.singletonList(new Alert.AlertCall(startTime, "message"))));
    when(dao.load(2L)).thenReturn(new Alert(2L, startTime, null, false, null, Collections.emptyList()));
    String exportedAlerts = alertService.exportAllAlerts();
    assertThat(exportedAlerts).isEqualTo("[\n" +
        "  {\n" +
        "    \"id\": 1,\n" +
        "    \"startTime\": \"2020-04-26T18:11:06.641Z\",\n" +
        "    \"confirmed\": false,\n" +
        "    \"calls\": [\n" +
        "      {\n" +
        "        \"time\": \"2020-04-26T18:11:06.641Z\",\n" +
        "        \"message\": \"message\"\n" +
        "      }\n" +
        "    ]\n" +
        "  },\n" +
        "  {\n" +
        "    \"id\": 2,\n" +
        "    \"startTime\": \"2020-04-26T18:11:06.641Z\",\n" +
        "    \"confirmed\": false,\n" +
        "    \"calls\": []\n" +
        "  }\n" +
        "]");
  }

  @Test
  void importAllAlerts() {
    AlertDao dao = mock(AlertDao.class);
    AlertService alertService = new AlertService(mock(Context.class), dao);

    final Alert oldAlert1 = new Alert(1L, Instant.now(), Instant.now(), false, null, Collections.emptyList());
    final Alert oldAlert2 = new Alert(2L, Instant.now(), Instant.now(), false, null, Collections.emptyList());
    final Alert oldAlert3 = new Alert(3L, Instant.now(), Instant.now(), false, null, Collections.emptyList());
    when(dao.loadAll()).thenReturn(
        Arrays.asList(
            oldAlert1,
            oldAlert2,
            oldAlert3
        )
    );

    boolean success = alertService.importAllAlerts("[\n" +
        "  {\n" +
        "    \"id\": 1,\n" +
        "    \"startTime\": \"2020-04-26T18:11:06.641Z\",\n" +
        "    \"confirmed\": false,\n" +
        "    \"calls\": [\n" +
        "      {\n" +
        "        \"time\": \"2020-04-26T18:11:06.641Z\",\n" +
        "        \"message\": \"message\"\n" +
        "      }\n" +
        "    ]\n" +
        "  },\n" +
        "  {\n" +
        "    \"id\": 2,\n" +
        "    \"startTime\": \"2020-04-26T18:11:06.641Z\",\n" +
        "    \"confirmed\": false,\n" +
        "    \"calls\": []\n" +
        "  }\n" +
        "]");
    assertThat(success).isTrue();
    verify(dao).loadAll();
    verify(dao).delete(oldAlert1);
    verify(dao).delete(oldAlert2);
    verify(dao).delete(oldAlert3);
    verify(dao, times(2)).saveImportedAlert(any());
    verifyNoMoreInteractions(dao);
  }

  @Test
  void importAllAlertsFromEmptyList() {
    AlertDao dao = mock(AlertDao.class);
    AlertService alertService = new AlertService(mock(Context.class), dao);

    Alert oldAlert1 = new Alert(1L, Instant.now(), Instant.now(), false, null, Collections.emptyList());
    when(dao.loadAll()).thenReturn(Collections.singletonList(oldAlert1));

    boolean success = alertService.importAllAlerts("[]");
    assertThat(success).isFalse();
    verifyNoMoreInteractions(dao);
  }

  @Test
  void importAllAlertsFromIllegalString() {
    AlertDao dao = mock(AlertDao.class);
    AlertService alertService = new AlertService(mock(Context.class), dao);

    Alert oldAlert1 = new Alert(1L, Instant.now(), Instant.now(), false, null, Collections.emptyList());
    when(dao.loadAll()).thenReturn(Collections.singletonList(oldAlert1));

    boolean success = alertService.importAllAlerts("BAD_JSON");
    assertThat(success).isFalse();
    verifyNoMoreInteractions(dao);
  }
}
package com.github.frimtec.android.pikettassist.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarTest {

  @Test
  void getId() {
    Calendar calendar = new Calendar(3, "name");
    assertThat(calendar.getId()).isEqualTo(3);
  }

  @Test
  void getName() {
    Calendar calendar = new Calendar(3, "name");
    assertThat(calendar.getName()).isEqualTo("name");
  }
}
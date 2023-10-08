package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CalendarTest {

  @Test
  void getId() {
    Calendar calendar = new Calendar(3, "name");
    assertThat(calendar.id()).isEqualTo(3);
  }

  @Test
  void getName() {
    Calendar calendar = new Calendar(3, "name");
    assertThat(calendar.name()).isEqualTo("name");
  }
}
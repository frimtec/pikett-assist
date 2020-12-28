package com.github.frimtec.android.pikettassist.action;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ActionTest {

  @Test
  void uniqueIds() {
    Set<String> ids = Stream.of(Action.values())
        .map(Action::getId)
        .collect(Collectors.toSet());
    assertThat(ids.size()).isEqualTo(Action.values().length);
  }
}
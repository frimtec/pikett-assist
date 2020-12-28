package com.github.frimtec.android.pikettassist.action;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JobServiceTest {

  @Test
  void uniqueIds() {
    Set<Integer> ids = Stream.of(JobService.values())
        .map(JobService::getId)
        .collect(Collectors.toSet());
    assertThat(ids.size()).isEqualTo(JobService.values().length);
  }
}
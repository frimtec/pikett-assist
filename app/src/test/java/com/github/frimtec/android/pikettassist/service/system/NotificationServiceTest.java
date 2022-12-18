package com.github.frimtec.android.pikettassist.service.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.frimtec.android.pikettassist.service.system.NotificationService.Progress;

import org.junit.jupiter.api.Test;

class NotificationServiceTest {

  @Test
  void progressForLongValue() {
    Progress progress = new Progress(Integer.MAX_VALUE * 100L, Integer.MAX_VALUE * 100L / 2);
    assertThat(progress.getMax()).isEqualTo(Integer.MAX_VALUE);
    assertThat(progress.getProgress()).isEqualTo(progress.getMax() / 2);
  }

  @Test
  void progressForMax() {
    Progress progress = new Progress(10_000, 10_000);
    assertThat(progress.getProgress()).isEqualTo(progress.getMax());
  }

  @Test
  void progressForMin() {
    Progress progress = new Progress(10_000, 0);
    assertThat(progress.getProgress()).isEqualTo(0);
  }

  @Test
  void progressForHalf() {
    Progress progress = new Progress(10_000, 5_000);
    assertThat(progress.getProgress()).isEqualTo(progress.getMax() / 2);
  }

  @Test
  void progressForOvershoot() {
    Progress progress = new Progress(10_000, 12_000);
    assertThat(progress.getProgress()).isEqualTo(progress.getMax());
  }

  @Test
  void progressForUndershoot() {
    Progress progress = new Progress(10_000, -1_000);
    assertThat(progress.getProgress()).isEqualTo(0);
  }

}
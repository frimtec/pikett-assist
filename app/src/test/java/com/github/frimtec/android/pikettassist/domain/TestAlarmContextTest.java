package com.github.frimtec.android.pikettassist.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TestAlarmContextTest {

  @Test
  void getContext() {
    String context = "context";
    TestAlarmContext testAlarmContext = new TestAlarmContext(context);
    assertThat(testAlarmContext.getContext()).isSameAs(context);
  }

  @Test
  void testToString() {
    String context = "context";
    TestAlarmContext testAlarmContext = new TestAlarmContext(context);
    assertThat(testAlarmContext.toString()).isEqualTo(context);
  }

  @SuppressWarnings({"EqualsWithItself", "EqualsBetweenInconvertibleTypes"})
  @Test
  void testEquals() {
    String context1 = "context1";
    String context2 = "context2";
    TestAlarmContext testAlarmContext1 = new TestAlarmContext(context1);
    TestAlarmContext testAlarmContext2 = new TestAlarmContext(context2);

    assertThat(testAlarmContext1.equals(testAlarmContext1)).isTrue();
    assertThat(testAlarmContext2.equals(testAlarmContext2)).isTrue();
    assertThat(testAlarmContext1.equals(testAlarmContext2)).isFalse();
    assertThat(testAlarmContext2.equals(testAlarmContext1)).isFalse();
    assertThat(testAlarmContext1.equals("context1")).isFalse();
  }

  @Test
  void testHashCode() {
    String context1 = "context1";
    String context2 = "context2";
    TestAlarmContext testAlarmContext1 = new TestAlarmContext(context1);
    TestAlarmContext testAlarmContext2 = new TestAlarmContext(context2);

    assertThat(testAlarmContext1.hashCode()).isEqualTo(testAlarmContext1.hashCode());
    assertThat(testAlarmContext2.hashCode()).isEqualTo(testAlarmContext2.hashCode());
    assertThat(testAlarmContext1.hashCode()).isNotEqualTo(testAlarmContext2.hashCode());
    assertThat(testAlarmContext2.hashCode()).isNotEqualTo(testAlarmContext1.hashCode());
  }
}
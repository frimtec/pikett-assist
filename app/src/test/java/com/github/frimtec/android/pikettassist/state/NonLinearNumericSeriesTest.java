package com.github.frimtec.android.pikettassist.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NonLinearNumericSeriesTest {

  private final int[] values = {
      0,
      10,
      20,
      40,
      60,
      100
  };

  @Test
  void constructorWithNull() {
    // act
    Exception exception = assertThrows(NullPointerException.class, () -> new NonLinearNumericSeries(null));
    assertThat(exception.getMessage()).isEqualTo("'valueSeries' must not be null");
  }

  @Test
  void constructorWithEmptyArray() {
    // act
    Exception exception = assertThrows(IllegalArgumentException.class, () -> new NonLinearNumericSeries(new int[0]));
    assertThat(exception.getMessage()).isEqualTo("'valueSeries' must not be empty");
  }

  @Test
  void constructorWithNonIncreasingValues() {
    // act
    Exception exception = assertThrows(IllegalArgumentException.class, () -> new NonLinearNumericSeries(new int[] {5, 6, 6}));
    assertThat(exception.getMessage()).isEqualTo("'valueSeries' must contain increasing values");
  }

  @Test
  void getMinIndex() {
    // arrange
    NonLinearNumericSeries pref = new NonLinearNumericSeries(values);

    // act
    int minIndex = pref.getMinIndex();

    // assert
    assertThat(minIndex).isEqualTo(0);
  }

  @Test
  void getMaxIndex() {
    // arrange
    NonLinearNumericSeries pref = new NonLinearNumericSeries(values);

    // act
    int maxIndex = pref.getMaxIndex();

    // assert
    assertThat(maxIndex).isEqualTo(5);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "0=0",
      "1=10",
      "2=20",
      "3=40",
      "4=60",
      "5=100"
  })
  void getValue(String test) {
    // arrange
    NonLinearNumericSeries pref = new NonLinearNumericSeries(values);
    String[] splitArgs = test.split("=");
    int index = Integer.parseInt(splitArgs[0]);
    int expectedValue = Integer.parseInt(splitArgs[1]);

    // act
    int value = pref.getValue(index);

    // assert
    assertThat(value).isEqualTo(expectedValue);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "-100=0",
      "0=0",
      "4=0",
      "5=1",
      "10=1",
      "20=2",
      "40=3",
      "60=4",
      "79=4",
      "80=5",
      "100=5",
      "200=5"
  })
  void getIndex(String test) {
    // arrange
    NonLinearNumericSeries pref = new NonLinearNumericSeries(values);
    String[] splitArgs = test.split("=");
    int value = Integer.parseInt(splitArgs[0]);
    int expectedIndex = Integer.parseInt(splitArgs[1]);

    // act
    int index = pref.getIndex(value);

    // assert
    assertThat(index).isEqualTo(expectedIndex);
  }

}
package com.github.frimtec.android.pikettassist.state;

import androidx.core.util.Pair;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class NonLinearNumericSeries {

  private final int[] valueSeries;

  NonLinearNumericSeries(int[] valueSeries) {
    Objects.requireNonNull(valueSeries, "'valueSeries' must not be null");
    if (valueSeries.length == 0) {
      throw new IllegalArgumentException("'valueSeries' must not be empty");
    }
    int lastValue = valueSeries[0];
    for (int i = 1; i < valueSeries.length; i++) {
      int value = valueSeries[i];
      if(value <= lastValue) {
        throw new IllegalArgumentException("'valueSeries' must contain increasing values");
      }
      lastValue = value;
    }
    this.valueSeries = Arrays.copyOf(valueSeries, valueSeries.length);
  }

  public int getMinIndex() {
    return 0;
  }

  public int getMaxIndex() {
    return valueSeries.length - 1;
  }

  public int getValue(int index) {
    return valueSeries[index];
  }

  @SuppressWarnings("ConstantConditions")
  public int getIndex(int value) {
    return IntStream.range(0, valueSeries.length)
        .mapToObj(index -> Pair.create(index, Math.abs(value - valueSeries[index])))
        .min((c1, c2) -> {
          int compare = Integer.compare(c1.second, c2.second);
          return compare != 0 ? compare : Integer.compare(c2.first, c1.first);
        })
        .orElse(Pair.create(0, 0)).first;
  }
}

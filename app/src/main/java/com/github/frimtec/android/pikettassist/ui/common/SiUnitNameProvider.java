package com.github.frimtec.android.pikettassist.ui.common;

import com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider;

final class SiUnitNameProvider implements UnitNameProvider {

  private static final UnitNameProvider INSTANCE = new SiUnitNameProvider();

  static UnitNameProvider getInstance() {
    return INSTANCE;
  }

  private SiUnitNameProvider() {
  }

  @Override
  public String getDay(boolean plural) {
    return "d";
  }

  @Override
  public String getHour(boolean plural) {
    return "h";
  }

  @Override
  public String getMinute(boolean plural) {
    return "m";
  }

  @Override
  public String getSeparator() {
    return "";
  }

}

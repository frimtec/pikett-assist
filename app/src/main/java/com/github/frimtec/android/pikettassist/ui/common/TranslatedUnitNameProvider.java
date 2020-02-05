package com.github.frimtec.android.pikettassist.ui.common;

import android.content.Context;

import androidx.annotation.StringRes;

import com.github.frimtec.android.pikettassist.R;

final class TranslatedUnitNameProvider implements DurationFormatter.UnitNameProvider {

  private final Context context;

  TranslatedUnitNameProvider(Context context) {
    this.context = context;
  }

  @Override
  public String getDay(boolean plural) {
    return plural ? getString(R.string.units_days) : getString(R.string.units_day);
  }

  @Override
  public String getHour(boolean plural) {
    return plural ? getString(R.string.units_hours) : getString(R.string.units_hour);
  }

  @Override
  public String getMinute(boolean plural) {
    return plural ? getString(R.string.units_minutes) : getString(R.string.units_minute);
  }

  @Override
  public String getSeparator() {
    return " ";
  }

  private String getString(@StringRes int resId) {
    return context.getString(resId);
  }
}

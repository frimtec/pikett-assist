package com.github.frimtec.android.pikettassist.ui.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.github.frimtec.android.pikettassist.R;

public class TimePreference extends DialogPreference {

  private int lastHour = 0;
  private int lastMinute = 0;
  private TimePicker picker = null;

  public TimePreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setPositiveButtonText(context.getResources().getString(R.string.preferences_time_set));
    setNegativeButtonText(context.getResources().getString(R.string.preferences_time_cancel));
  }

  private static int getHour(String time) {
    return (Integer.parseInt(time.split(":")[0]));
  }

  private static int getMinute(String time) {
    return (Integer.parseInt(time.split(":")[1]));
  }

  @Override
  protected View onCreateDialogView() {
    picker = new TimePicker(getContext());
    picker.setIs24HourView(true);

    return (picker);
  }

  @Override
  protected void onBindDialogView(View v) {
    super.onBindDialogView(v);
    picker.setHour(lastHour);
    picker.setMinute(lastMinute);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      lastHour = picker.getHour();
      lastMinute = picker.getMinute();

      @SuppressLint("DefaultLocale")
      String time = String.format("%02d:%02d", lastHour, lastMinute);

      if (callChangeListener(time)) {
        persistString(time);
      }
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return (a.getString(index));
  }

  @Override
  protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    String time;
    if (restoreValue) {
      if (defaultValue == null) {
        time = getPersistedString("00:00");
      } else {
        time = getPersistedString(defaultValue.toString());
      }
    } else {
      time = defaultValue.toString();
    }

    lastHour = getHour(time);
    lastMinute = getMinute(time);
  }
}
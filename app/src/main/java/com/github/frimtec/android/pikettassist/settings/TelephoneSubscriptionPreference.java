package com.github.frimtec.android.pikettassist.settings;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;

import java.util.ArrayList;
import java.util.List;

public class TelephoneSubscriptionPreference extends ListPreference {

  private static final String TAG = "TelephoneSubscriptionPreference";
  private static final int MAX_SUPPORTED_SIMS = 2;

  public TelephoneSubscriptionPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    List<CharSequence> entries = new ArrayList<>();
    List<CharSequence> entriesValues = new ArrayList<>();

    for (int i = 0; i < MAX_SUPPORTED_SIMS; i++) {
      SignalStrengthHelper signalStrengthHelper = new SignalStrengthHelper(context, i);
      String networkOperatorName = signalStrengthHelper.getNetworkOperatorName();
      if (networkOperatorName != null) {
        entriesValues.add(String.valueOf(i));
        entries.add(String.format("%s %d: %s", context.getString(R.string.subscription), i, networkOperatorName));
      } else {
        Log.d(TAG, "No phone manager for subscriptionId " + i);
      }
    }
    setEntries(entries.toArray(new CharSequence[]{}));
    setEntryValues(entriesValues.toArray(new CharSequence[]{}));
    setDefaultValue(context.getString(R.string.pref_default_supervise_signal_strength_subscription));
    if (entries.size() < 2) {
      setEnabled(false);
    }
  }
}
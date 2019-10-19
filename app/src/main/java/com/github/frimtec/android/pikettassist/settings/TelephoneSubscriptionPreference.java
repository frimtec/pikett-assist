package com.github.frimtec.android.pikettassist.settings;

import android.content.Context;
import android.preference.ListPreference;
import android.telephony.SubscriptionManager;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;

import java.util.ArrayList;
import java.util.List;

public class TelephoneSubscriptionPreference extends ListPreference {

  public TelephoneSubscriptionPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    List<CharSequence> entries = new ArrayList<>();
    List<CharSequence> entriesValues = new ArrayList<>();

    SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
    for (int i = 1; i <= subscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
      SignalStrengthHelper signalStrengthHelper = new SignalStrengthHelper(context, i);
      String networkOperatorName = signalStrengthHelper.getNetworkOperatorName();
      if (networkOperatorName != null) {
        entriesValues.add(String.valueOf(i));
        entries.add(String.format("%s (%s %d)", networkOperatorName, context.getString(R.string.subscription), i));
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
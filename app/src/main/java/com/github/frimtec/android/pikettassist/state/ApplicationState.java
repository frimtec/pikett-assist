package com.github.frimtec.android.pikettassist.state;

import android.util.Pair;

public interface ApplicationState {

  int DEFAULT_VOLUME_NOT_SET = -1;

  /**
   * @noinspection SameReturnValue
   */
  static ApplicationState instance() {
    return KeyValueStoreApplicationState.INSTANCE;
  }

  String getSmsAdapterSecret();

  void setSmsAdapterSecret(String secret);

   boolean getPikettStateManuallyOn();

   void setPikettStateManuallyOn(boolean manuallyOn);

  Pair<String, Integer> getLastAlarmSmsNumberWithSubscriptionId();

   void setLastAlarmSmsNumberWithSubscriptionId(String smsNumber, Integer subscriptionId);

   int getDefaultVolume();

   void setDefaultVolume(int volume);
}

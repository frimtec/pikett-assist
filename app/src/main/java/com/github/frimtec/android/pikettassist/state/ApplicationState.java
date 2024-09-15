package com.github.frimtec.android.pikettassist.state;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.Optional;

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

  Optional<Sms> getLastAlarmSms();

  void setLastAlarmSms(Sms sms);

   int getDefaultVolume();

   void setDefaultVolume(int volume);
}

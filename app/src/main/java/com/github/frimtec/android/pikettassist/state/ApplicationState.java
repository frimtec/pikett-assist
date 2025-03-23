package com.github.frimtec.android.pikettassist.state;

import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;

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

  List<Sms> getLastAlarmSms();

  void addLastAlarmSms(Sms sms);

  void clearLastAlarmSms();

   int getDefaultVolume();

   void setDefaultVolume(int volume);
}

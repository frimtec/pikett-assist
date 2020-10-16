package com.github.frimtec.android.pikettassist.state;

import android.util.Pair;

import static com.github.frimtec.android.pikettassist.PAssistApplication.getKeyValueStore;

final class KeyValueStoreApplicationState implements ApplicationState {

  private static final String DELIMITER = ";";

  private static final String KEY_SMS_ADAPTER_SECRET = "sms_adapter.secret";
  private static final String KEY_PIKETT_STATE_MANUALLY_ON = "pikett_state.manually_on";
  private static final String KEY_LAST_ALARM_SMS_NUMBER = "last_alarm.sms_number_subscription_id";
  private static final String KEY_DEFAULT_VOLUME = "volume.default";

  public static final KeyValueStoreApplicationState INSTANCE = new KeyValueStoreApplicationState();

  private KeyValueStoreApplicationState() {
  }

  @Override
  public String getSmsAdapterSecret() {
    return getKeyValueStore().get(KEY_SMS_ADAPTER_SECRET, "");
  }

  @Override
  public void setSmsAdapterSecret(String secret) {
    getKeyValueStore().put(KEY_SMS_ADAPTER_SECRET, secret);
  }

  @Override
  public boolean getPikettStateManuallyOn() {
    return Boolean.parseBoolean(getKeyValueStore().get(KEY_PIKETT_STATE_MANUALLY_ON, String.valueOf(false)));
  }

  @Override
  public void setPikettStateManuallyOn(boolean manuallyOn) {
    getKeyValueStore().put(KEY_PIKETT_STATE_MANUALLY_ON, String.valueOf(manuallyOn));
  }

  @Override
  public Pair<String, Integer> getLastAlarmSmsNumberWithSubscriptionId() {
    String value = getKeyValueStore().get(KEY_LAST_ALARM_SMS_NUMBER, DELIMITER);
    String[] split = value.split(DELIMITER);
    return Pair.create(split[0], split.length > 1 && !split[1].isEmpty() ? Integer.valueOf(split[1]) : null);
  }

  @Override
  public void setLastAlarmSmsNumberWithSubscriptionId(String smsNumber, Integer subscriptionId) {
    getKeyValueStore().put(KEY_LAST_ALARM_SMS_NUMBER, smsNumber + DELIMITER + (subscriptionId != null ? subscriptionId : ""));
  }

  @Override
  public int getDefaultVolume() {
    return Integer.parseInt(getKeyValueStore().get(KEY_DEFAULT_VOLUME, String.valueOf(DEFAULT_VOLUME_NOT_SET)));
  }

  @Override
  public void setDefaultVolume(int volume) {
    getKeyValueStore().put(KEY_DEFAULT_VOLUME, String.valueOf(volume));
  }
}

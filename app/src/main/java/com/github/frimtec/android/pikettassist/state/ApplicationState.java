package com.github.frimtec.android.pikettassist.state;

import android.util.Pair;

import static com.github.frimtec.android.pikettassist.PAssistApplication.getKeyValueStore;

public final class ApplicationState {

  public static final int DEFAULT_VOLUME_NOT_SET = -1;

  private static final String DELIMITER = ";";

  private static final String KEY_SMS_ADAPTER_SECRET = "sms_adapter.secret";
  private static final String KEY_PIKETT_STATE_MANUALLY_ON = "pikett_state.manually_on";
  private static final String KEY_LAST_ALARM_SMS_NUMBER = "last_alarm.sms_number_subscription_id";
  private static final String KEY_DEFAULT_VOLUME = "volume.default";

  private ApplicationState() {
  }

  public static String getSmsAdapterSecret() {
    return getKeyValueStore().get(KEY_SMS_ADAPTER_SECRET, "");
  }

  public static void setSmsAdapterSecret(String secret) {
    getKeyValueStore().put(KEY_SMS_ADAPTER_SECRET, secret);
  }

  public static boolean getPikettStateManuallyOn() {
    return Boolean.parseBoolean(getKeyValueStore().get(KEY_PIKETT_STATE_MANUALLY_ON, String.valueOf(false)));
  }

  public static void setPikettStateManuallyOn(boolean manuallyOn) {
    getKeyValueStore().put(KEY_PIKETT_STATE_MANUALLY_ON, String.valueOf(manuallyOn));
  }

  public static Pair<String, Integer> getLastAlarmSmsNumberWithSubscriptionId() {
    String value = getKeyValueStore().get(KEY_LAST_ALARM_SMS_NUMBER, DELIMITER);
    String[] split = value.split(DELIMITER);
    return Pair.create(split[0], split.length > 1 && !split[1].isEmpty() ? Integer.valueOf(split[1]) : null);
  }

  public static void setLastAlarmSmsNumberWithSubscriptionId(String smsNumber, Integer subscriptionId) {
    getKeyValueStore().put(KEY_LAST_ALARM_SMS_NUMBER, smsNumber + DELIMITER + (subscriptionId != null ? subscriptionId : ""));
  }

  public static int getDefaultVolume() {
    return Integer.parseInt(getKeyValueStore().get(KEY_DEFAULT_VOLUME, String.valueOf(DEFAULT_VOLUME_NOT_SET)));
  }

  public static void setDefaultVolume(int volume) {
    getKeyValueStore().put(KEY_DEFAULT_VOLUME, String.valueOf(volume));
  }
}

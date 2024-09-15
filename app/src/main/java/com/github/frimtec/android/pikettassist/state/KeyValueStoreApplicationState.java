package com.github.frimtec.android.pikettassist.state;

import static com.github.frimtec.android.pikettassist.PAssistApplication.getKeyValueStore;

import android.util.Log;

import com.github.frimtec.android.pikettassist.util.GsonHelper;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.google.gson.JsonSyntaxException;

import java.util.Optional;

final class KeyValueStoreApplicationState implements ApplicationState {

  private static final String TAG = "KeyValueStoreApplicationState";

  private static final String KEY_SMS_ADAPTER_SECRET = "sms_adapter.secret";
  private static final String KEY_PIKETT_STATE_MANUALLY_ON = "pikett_state.manually_on";
  private static final String KEY_LAST_ALARM_SMS = "last_alarm.sms";
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
  public Optional<Sms> getLastAlarmSms() {
    String value = getKeyValueStore().get(KEY_LAST_ALARM_SMS, "");
    try {
      return Optional.of(GsonHelper.GSON.fromJson(value, Sms.class));
    } catch (JsonSyntaxException e) {
      Log.e(TAG, "Cannot parse last alarm sms: '" + value + "'", e);
    }
    return Optional.empty();
  }

  @Override
  public void setLastAlarmSms(Sms sms) {
    getKeyValueStore().put(KEY_LAST_ALARM_SMS, GsonHelper.GSON.toJson(sms));
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

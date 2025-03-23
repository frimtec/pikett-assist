package com.github.frimtec.android.pikettassist.state;

import static com.github.frimtec.android.pikettassist.PAssistApplication.getKeyValueStore;

import android.text.TextUtils;
import android.util.Log;

import com.github.frimtec.android.pikettassist.util.GsonHelper;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  public List<Sms> getLastAlarmSms() {
    String value = getKeyValueStore().get(KEY_LAST_ALARM_SMS, "");
    if (!TextUtils.isEmpty(value)) {
      try {
        Type smsListType = new TypeToken<List<Sms>>() {
        }.getType();
        return GsonHelper.GSON.fromJson(value, smsListType);
      } catch (JsonSyntaxException e) {
        Log.e(TAG, "Cannot parse last alarm sms: '" + value + "'", e);
      }
    }
    return Collections.emptyList();
  }

  @Override
  public void addLastAlarmSms(Sms sms) {
    List<Sms> smsList = new ArrayList<>(getLastAlarmSms());
    smsList.add(sms);
    getKeyValueStore().put(KEY_LAST_ALARM_SMS, GsonHelper.GSON.toJson(smsList));
  }

  @Override
  public void clearLastAlarmSms() {
    getKeyValueStore().put(KEY_LAST_ALARM_SMS, "");
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

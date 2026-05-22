package com.github.frimtec.android.pikettassist.state;

import static com.github.frimtec.android.pikettassist.PAssistApplication.getKeyValueStore;

import android.text.TextUtils;
import android.util.Log;

import com.github.frimtec.android.pikettassist.service.dao.ContactCopy;
import com.github.frimtec.android.pikettassist.ui.common.ReleaseMessages;
import com.github.frimtec.android.pikettassist.util.GsonHelper;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class KeyValueStoreApplicationState implements ApplicationState {

  private static final String TAG = "KeyValueStoreApplicationState";

  private static final String KEY_SMS_ADAPTER_SECRET = "sms_adapter.secret";
  private static final String KEY_PIKETT_STATE_MANUALLY_ON = "pikett_state.manually_on";
  private static final String KEY_LAST_ALARM_SMS = "last_alarm.sms";
  private static final String KEY_DEFAULT_VOLUME = "volume.default";
  private static final String KEY_PREFIX_CONTACT_COPY = "contact.";
  private static final String KEY_PREFIX_RELEASE_MESSAGE_READ = "releaseMessage.read.";
  private static final String RELEASE_MESSAGE_READ_NO = "no";
  private static final String RELEASE_MESSAGE_READ_YES = "yes";

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

  @Override
  public void saveContact(ContactCopy contact) {
    getKeyValueStore().put(KEY_PREFIX_CONTACT_COPY + contact.reference().id(), GsonHelper.GSON.toJson(contact));
  }

  @Override
  public void saveContact(ContactCopy contact, String nickname) {
    getKeyValueStore().put(KEY_PREFIX_CONTACT_COPY + nickname, GsonHelper.GSON.toJson(contact));
  }

  @Override
  public Optional<ContactCopy> loadContact(long id) {
    String value = getKeyValueStore().get(KEY_PREFIX_CONTACT_COPY + id, "");
    return TextUtils.isEmpty(value) ? Optional.empty() :
        Optional.of(GsonHelper.GSON.fromJson(value, ContactCopy.class));
  }

  @Override
  public Optional<ContactCopy> loadContact(String nickname) {
    String value = getKeyValueStore().get(KEY_PREFIX_CONTACT_COPY + nickname, "");
    return TextUtils.isEmpty(value) ? Optional.empty() :
        Optional.of(GsonHelper.GSON.fromJson(value, ContactCopy.class));
  }

  @Override
  public boolean isReleaseMessageDisplayed(ReleaseMessages releaseMessage) {
    return RELEASE_MESSAGE_READ_NO.equals(getKeyValueStore().get(KEY_PREFIX_RELEASE_MESSAGE_READ + releaseMessage.id(), RELEASE_MESSAGE_READ_NO));
  }

  @Override
  public void checkReleaseMessageDisplayed(ReleaseMessages releaseMessage) {
    getKeyValueStore().put(KEY_PREFIX_RELEASE_MESSAGE_READ + releaseMessage.id(), RELEASE_MESSAGE_READ_YES);
  }
}

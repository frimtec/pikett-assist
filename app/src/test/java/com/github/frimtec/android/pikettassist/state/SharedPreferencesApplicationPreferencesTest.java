package com.github.frimtec.android.pikettassist.state;

import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_ALERT_CONFIRM_METHOD;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_BATTERY_SAFER_AT_NIGHT;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_SEND_CONFIRM_SMS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class SharedPreferencesApplicationPreferencesTest {

  private SharedPreferencesApplicationPreferences preferences;
  private Context context;
  private SharedPreferences sharedPreferences;
  private SharedPreferences.Editor editor;

  @BeforeEach
  void setUp() {
    preferences = SharedPreferencesApplicationPreferences.INSTANCE;
    context = mock(Context.class);
    sharedPreferences = mock(SharedPreferences.class);
    editor = mock(SharedPreferences.Editor.class);

    when(sharedPreferences.edit()).thenReturn(editor);
    when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
    when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
    when(editor.putString(anyString(), anyString())).thenReturn(editor);
    when(editor.putStringSet(anyString(), anySet())).thenReturn(editor);
    when(editor.putFloat(anyString(), anyFloat())).thenReturn(editor);
    when(editor.commit()).thenReturn(true);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void exportSettings() {
    Map<String, Object> allEntries = new HashMap<>();
    allEntries.put(PREF_KEY_ALERT_CONFIRM_METHOD, "value1");
    allEntries.put(PREF_KEY_SEND_CONFIRM_SMS, true);
    allEntries.put(PREF_KEY_LOW_SIGNAL_FILTER, 123);
    allEntries.put("unknown_key", "unknown_value");
    when(sharedPreferences.getAll()).thenReturn((Map) allEntries);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
      preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(sharedPreferences);

      boolean result = preferences.exportSettings(context, outputStream);

      assertThat(result).isTrue();
      String json = outputStream.toString(StandardCharsets.UTF_8);
      assertThat(json).contains("\"" + PREF_KEY_ALERT_CONFIRM_METHOD + "\": \"value1\"");
      assertThat(json).contains("\"" + PREF_KEY_SEND_CONFIRM_SMS + "\": true");
      assertThat(json).contains("\"" + PREF_KEY_LOW_SIGNAL_FILTER + "\": 123");
      assertThat(json).doesNotContain("unknown_key", "unknown_value");
    }
  }

  @Test
  void importSettings() {
    String json = "{\"" + PREF_KEY_ALERT_CONFIRM_METHOD + "\": \"value1\", \"" +
        PREF_KEY_SEND_CONFIRM_SMS + "\": true, \"" +
        PREF_KEY_LOW_SIGNAL_FILTER + "\": 123, \"" +
        PREF_KEY_BATTERY_SAFER_AT_NIGHT + "\": [\"s1\", \"s2\"]}";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

    try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
      preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(sharedPreferences);

      boolean result = preferences.importSettings(context, inputStream);

      assertThat(result).isTrue();
      verify(editor).putString(PREF_KEY_ALERT_CONFIRM_METHOD, "value1");
      verify(editor).putBoolean(PREF_KEY_SEND_CONFIRM_SMS, true);
      verify(editor).putInt(PREF_KEY_LOW_SIGNAL_FILTER, 123);
      verify(editor).putStringSet(PREF_KEY_BATTERY_SAFER_AT_NIGHT, Set.of("s1", "s2"));
      verify(editor).commit();
      verifyNoMoreInteractions(editor);
    }
  }

  @Test
  void importSettingsWithFloat() {
    String json = "{\"" + PREF_KEY_LOW_SIGNAL_FILTER + "\": 12.5}";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

    try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
      preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(sharedPreferences);

      boolean result = preferences.importSettings(context, inputStream);

      assertThat(result).isTrue();
      verify(editor).putFloat(PREF_KEY_LOW_SIGNAL_FILTER, 12.5f);
      verify(editor).commit();
    }
  }

  @Test
  void importSettingsEmpty() {
    String json = "null";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

    boolean result = preferences.importSettings(context, inputStream);

    assertThat(result).isFalse();
  }
}

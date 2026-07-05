package com.github.frimtec.android.pikettassist.state;

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
import static org.mockito.Mockito.when;

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
    when(editor.clear()).thenReturn(editor);
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
    allEntries.put("key1", "value1");
    allEntries.put("key2", true);
    allEntries.put("key3", 123);
    when(sharedPreferences.getAll()).thenReturn((Map) allEntries);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
      preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(sharedPreferences);

      boolean result = preferences.exportSettings(context, outputStream);

      assertThat(result).isTrue();
      String json = outputStream.toString(StandardCharsets.UTF_8);
      assertThat(json).contains("\"key1\": \"value1\"");
      assertThat(json).contains("\"key2\": true");
      assertThat(json).contains("\"key3\": 123");
    }
  }

  @Test
  void importSettings() {
    String json = "{\"key1\": \"value1\", \"key2\": true, \"key3\": 123, \"key4\": [\"s1\", \"s2\"]}";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

    try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
      preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(sharedPreferences);

      boolean result = preferences.importSettings(context, inputStream);

      assertThat(result).isTrue();
      verify(editor).clear();
      verify(editor).putString("key1", "value1");
      verify(editor).putBoolean("key2", true);
      verify(editor).putInt("key3", 123);
      verify(editor).putStringSet("key4", Set.of("s1", "s2"));
      verify(editor).commit();
    }
  }

  @Test
  void importSettingsWithFloat() {
    String json = "{\"key5\": 12.5}";
    ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

    try (MockedStatic<PreferenceManager> preferenceManagerMockedStatic = mockStatic(PreferenceManager.class)) {
      preferenceManagerMockedStatic.when(() -> PreferenceManager.getDefaultSharedPreferences(context)).thenReturn(sharedPreferences);

      boolean result = preferences.importSettings(context, inputStream);

      assertThat(result).isTrue();
      verify(editor).putFloat("key5", 12.5f);
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

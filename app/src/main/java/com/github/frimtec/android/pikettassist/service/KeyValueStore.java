package com.github.frimtec.android.pikettassist.service;

import androidx.annotation.NonNull;

import com.github.frimtec.android.pikettassist.service.dao.KeyValueDao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyValueStore {

  public interface KeyValueBacked {

    Map<String, String> load();

    void insert(String key, String value);

    void update(String key, String value);
  }

  private static final KeyValueStore INSTANCE = new KeyValueStore();

  private final KeyValueBacked backend;

  private final ConcurrentHashMap<String, String> keyValues = new ConcurrentHashMap<>();


  public static KeyValueStore getInstance() {
    return INSTANCE;
  }

  private KeyValueStore() {
    this(new KeyValueDao());
  }

  KeyValueStore(KeyValueBacked backend) {
    this.backend = backend;
    this.keyValues.putAll(backend.load());
  }

  public String get(@NonNull String key, String defaultValue) {
    return this.keyValues.getOrDefault(key, defaultValue);
  }

  public void put(@NonNull String key, @NonNull String value) {
    this.keyValues.compute(key, (mapKey, mapValue) -> {
      if (mapValue == null) {
        this.backend.insert(key, value);
      } else {
        this.backend.update(key, value);
      }
      return value;
    });
  }
}
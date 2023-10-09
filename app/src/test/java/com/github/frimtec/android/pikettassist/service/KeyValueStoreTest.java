package com.github.frimtec.android.pikettassist.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class KeyValueStoreTest {

  @Test
  void get() {
    Map<String, String> keyValues = new HashMap<>();
    keyValues.put("key.old", "old");

    KeyValueStore store = new KeyValueStore(new TestBackend(keyValues));
    assertThat(store.get("key.old", "new")).isEqualTo("old");
    assertThat(store.get("key.new", "new")).isEqualTo("new");
  }

  @Test
  void put() {
    Map<String, String> keyValues = new HashMap<>();
    keyValues.put("key.old", "old");

    KeyValueStore store = new KeyValueStore(new TestBackend(keyValues));
    store.put("key.new", "new");
    store.put("key.old", "older");

    assertThat(store.get("key.old", "X")).isEqualTo("older");
    assertThat(store.get("key.new", "X")).isEqualTo("new");
  }

  private record TestBackend(
      Map<String, String> backendState
  ) implements KeyValueStore.KeyValueBacked {

    @Override
    public Map<String, String> load() {
      return new HashMap<>(backendState);
    }

    @Override
    public void insert(String key, String value) {
      if (this.backendState.put(key, value) != null) {
        throw new IllegalStateException("Insert called but key already available");
      }
    }

    @Override
    public void update(String key, String value) {
      if (this.backendState.put(key, value) == null) {
        throw new IllegalStateException("Update called but key not yet available");
      }
    }
  }
}
package com.github.frimtec.android.pikettassist.service.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.frimtec.android.pikettassist.state.DbFactory;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.frimtec.android.pikettassist.service.dao.KeyValueDao.ALL_COLUMNS;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_KEY_VALUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_KEY_VALUE_COLUMN_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeyValueDaoTest {

  @Test
  void loadForFoundKeyValues() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    LinkedHashMap<String, String> expectedKeyValues = new LinkedHashMap<>();
    expectedKeyValues.put("key.1", "value1");
    expectedKeyValues.put("key.2", "value2");
    Cursor cursor = createCursor(expectedKeyValues);
    when(db.query(TABLE_KEY_VALUE, ALL_COLUMNS, null, null, null, null, null))
        .thenReturn(cursor);

    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    KeyValueDao dao = new KeyValueDao(dbFactory);

    Map<String, String> keyValues = dao.load();
    assertThat(keyValues).isEqualTo(expectedKeyValues);
  }

  @Test
  void loadForNullCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    Cursor cursor = createCursor(new LinkedHashMap<>());
    when(db.query(TABLE_KEY_VALUE, ALL_COLUMNS, null, null, null, null, null))
        .thenReturn(cursor);

    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    KeyValueDao dao = new KeyValueDao(dbFactory);

    Map<String, String> keyValues = dao.load();
    assertThat(keyValues).isEmpty();
  }

  @Test
  void loadForEmptyCursor() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    KeyValueDao dao = new KeyValueDao(dbFactory);

    Map<String, String> keyValues = dao.load();
    assertThat(keyValues).isEmpty();
  }

  @Test
  void insert() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    KeyValueDao dao = new KeyValueDao(dbFactory);

    dao.insert("key.new", "value");

    verify(db).insert(Mockito.eq(TABLE_KEY_VALUE), Mockito.isNull(), Mockito.any());
    verify(db).close();
  }

  @Test
  void update() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    KeyValueDao dao = new KeyValueDao(dbFactory);

    dao.update("key.update", "value");

    verify(db).update(Mockito.eq(TABLE_KEY_VALUE), Mockito.any(), Mockito.eq(TABLE_KEY_VALUE_COLUMN_ID + "=?"), Mockito.eq(new String[]{"key.update"}));
    verify(db).close();
  }

  @Test
  void delete() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);

    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    KeyValueDao dao = new KeyValueDao(dbFactory);

    dao.delete("key.to.delete");

    verify(db).delete(TABLE_KEY_VALUE, TABLE_KEY_VALUE_COLUMN_ID + "=?", new String[]{"key.to.delete"});
    verify(db).close();
  }

  private Cursor createCursor(LinkedHashMap<String, String> expectedKeyValues) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(!expectedKeyValues.isEmpty());
    if (!expectedKeyValues.isEmpty()) {
      Boolean[] moveResults = new Boolean[expectedKeyValues.size()];
      Arrays.fill(moveResults, true);
      moveResults[moveResults.length - 1] = false;
      when(cursor.moveToNext()).thenReturn(expectedKeyValues.size() > 1, moveResults);

      List<String> keys = new ArrayList<>(expectedKeyValues.keySet());
      List<String> values = new ArrayList<>(expectedKeyValues.values());

      when(cursor.getString(0)).thenReturn(keys.get(0), keys.subList(1, keys.size()).toArray(new String[0]));
      when(cursor.getString(1)).thenReturn(values.get(0), values.subList(1, values.size()).toArray(new String[0]));
    }
    return cursor;
  }
}
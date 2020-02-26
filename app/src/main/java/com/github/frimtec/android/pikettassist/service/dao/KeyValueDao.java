package com.github.frimtec.android.pikettassist.service.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.frimtec.android.pikettassist.service.KeyValueStore.KeyValueBacked;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import java.util.HashMap;
import java.util.Map;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_KEY_VALUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_KEY_VALUE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_KEY_VALUE_COLUMN_VALUE;

public class KeyValueDao implements KeyValueBacked {

  private static final String TAG = "KeyValueDao";

  static final String[] ALL_COLUMNS = {
      TABLE_KEY_VALUE_COLUMN_ID,
      TABLE_KEY_VALUE_COLUMN_VALUE
  };

  private final DbFactory dbFactory;

  public KeyValueDao(DbFactory dbFactory) {
    this.dbFactory = dbFactory;
  }

  @Override
  public Map<String, String> load() {
    Map<String, String> keyValues = new HashMap<>();
    try (SQLiteDatabase db = dbFactory.getDatabase(READ_ONLY);
         Cursor cursor = db.query(TABLE_KEY_VALUE, ALL_COLUMNS, null, null, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        do {
          keyValues.put(cursor.getString(0), cursor.getString(1));
        } while (cursor.moveToNext());
      }
    }
    Log.i(TAG, "Key value map loaded with " + keyValues.size() + " entries.");
    return keyValues;
  }

  @Override
  public void insert(String key, String value) {
    try (SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE)) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_KEY_VALUE_COLUMN_ID, key);
      contentValues.put(TABLE_KEY_VALUE_COLUMN_VALUE, value);
      long id = db.insert(TABLE_KEY_VALUE, null, contentValues);
      if (id < 0) {
        Log.e(TAG, "Could not insert key " + key);
      }
    }
  }

  @Override
  public void update(String key, String value) {
    try (SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE)) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_KEY_VALUE_COLUMN_ID, key);
      contentValues.put(TABLE_KEY_VALUE_COLUMN_VALUE, value);
      long id = db.update(TABLE_KEY_VALUE, contentValues, TABLE_KEY_VALUE_COLUMN_ID + "=?", new String[]{key});
      if (id == 0) {
        Log.e(TAG, "Could not update key " + key);
      }
    }
  }

  public void delete(String key) {
    try (SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE)) {
      long id = db.delete(TABLE_KEY_VALUE, TABLE_KEY_VALUE_COLUMN_ID + "=?", new String[]{key});
      if (id == 0) {
        Log.e(TAG, "Could not delete key " + key);
      }
    }
  }
}

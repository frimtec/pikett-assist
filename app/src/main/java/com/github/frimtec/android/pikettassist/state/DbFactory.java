package com.github.frimtec.android.pikettassist.state;

import android.database.sqlite.SQLiteDatabase;

import com.github.frimtec.android.pikettassist.PAssistApplication;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;

@FunctionalInterface
public interface DbFactory {

  enum Mode {
    READ_ONLY,
    WRITABLE
  }

  static DbFactory instance() {
    return (mode -> mode == READ_ONLY ? PAssistApplication.getReadableDatabase() : PAssistApplication.getWritableDatabase());
  }

  SQLiteDatabase getDatabase(Mode mode);
}
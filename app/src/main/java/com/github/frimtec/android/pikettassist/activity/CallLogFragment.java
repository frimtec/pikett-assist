package com.github.frimtec.android.pikettassist.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.state.PAssist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_TRUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_CONFIRM_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_START_TIME;

public class CallLogFragment extends AbstractListFragment<Alert> {

  private static final int MENU_CONTEXT_VIEW_ID = 1;
  private static final int MENU_CONTEXT_DELETE_ID = 2;

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Alert selectedAlert = (Alert) listView.getItemAtPosition(position);
      showAlertDetails(selectedAlert);
    });
    registerForContextMenu(listView);
  }

  @Override
  protected ArrayAdapter<Alert> createAdapter() {
    return new AlertArrayAdapter(getContext(), loadAlertList());
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW_ID, Menu.NONE, R.string.call_log_fragment_menu_view);
    menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.call_log_fragment_menu_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ListView listView = getListView();
    Alert selectedAlert = (Alert) listView.getItemAtPosition(info.position);
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW_ID:
        showAlertDetails(selectedAlert);
        return true;
      case MENU_CONTEXT_DELETE_ID:
        NotificationHelper.areYouSure(getContext(), (dialog, which) -> {
          deleteAlert(selectedAlert);
          refresh();
          Toast.makeText(getContext(), R.string.genaral_entry_deleted, Toast.LENGTH_SHORT).show();
        }, (dialog, which) -> {
        });
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  private void showAlertDetails(Alert selectedAlert) {
    Intent intent = new Intent(this.getContext(), AlertDetailActivity.class);
    Bundle b = new Bundle();
    b.putLong("alertId", selectedAlert.getId());
    intent.putExtras(b);
    startActivity(intent);
  }

  private void deleteAlert(Alert selectedAlert) {
    try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
      db.delete(TABLE_ALERT, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(selectedAlert.getId())});
    }
  }

  private List<Alert> loadAlertList() {
    List<Alert> alertList = new ArrayList<>();
    try (SQLiteDatabase db = PAssist.getReadableDatabase();
         Cursor cursor = db.rawQuery("SELECT " + TABLE_ALERT_COLUMN_ID + ", " + TABLE_ALERT_COLUMN_START_TIME + ", " + TABLE_ALERT_COLUMN_CONFIRM_TIME + ", " + TABLE_ALERT_COLUMN_END_TIME + ", " + TABLE_ALERT_COLUMN_IS_CONFIRMED + " FROM " + TABLE_ALERT + " ORDER BY " + TABLE_ALERT_COLUMN_START_TIME + " DESC", null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          alertList.add(new Alert(
              id,
              Instant.ofEpochMilli(cursor.getLong(1)),
              cursor.getLong(2) > 0 ? Instant.ofEpochMilli(cursor.getLong(2)) : null,
              cursor.getInt(4) == BOOLEAN_TRUE,
              cursor.getLong(3) > 0 ? Instant.ofEpochMilli(cursor.getLong(3)) : null,
              Collections.emptyList()));
        } while (cursor.moveToNext());
      }
    }
    return alertList;
  }
}

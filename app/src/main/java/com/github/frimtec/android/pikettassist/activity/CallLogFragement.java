package com.github.frimtec.android.pikettassist.activity;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.state.PikettAssist;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallLogFragement extends Fragment {

  private static final String TAG = "CallLogFragement";
  private static final int MENU_CONTEXT_VIEW_ID = 1;
  private static final int MENU_CONTEXT_DELETE_ID = 2;

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_list, container, false);
    ListView listView = view.findViewById(R.id.activity_list);
    ArrayAdapter<Alert> adapter = new AlertArrayAdapter(getContext(), loadAlertList());
    listView.setAdapter(adapter);
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Alert selectedAlert = (Alert) listView.getItemAtPosition(position);
      long eventId = selectedAlert.getId();
      Log.v(TAG, "Selected alert: " + selectedAlert.getId());
      showAlertDetails(selectedAlert);
    });
    registerForContextMenu(listView);
    return view;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    ListView view = (ListView) v;
    AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
    Alert selectedAlert = (Alert) view.getItemAtPosition(acmi.position);
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW_ID, Menu.NONE, "View");
    menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, "Delete");
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ListView listView = view.findViewById(R.id.activity_list);
    Alert selectedAlert = (Alert) listView.getItemAtPosition(info.position);
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW_ID:
        Log.v(TAG, "View alert: " + selectedAlert.getId());
        showAlertDetails(selectedAlert);
        return true;
      case MENU_CONTEXT_DELETE_ID:
        Log.v(TAG, "Delete alert: " + selectedAlert.getId());
        deleteAlert(selectedAlert);
        refresh();
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

  public void refresh() {
    ListView listView = view.findViewById(R.id.activity_list);
    ArrayAdapter<Alert> adapter = new AlertArrayAdapter(getContext(), loadAlertList());
    listView.setAdapter(adapter);
  }

  private void deleteAlert(Alert selectedAlert) {
    try (SQLiteDatabase db = PikettAssist.getWritableDatabase()) {
      db.delete("t_alert", "_id=?", new String[]{String.valueOf(selectedAlert.getId())});
    }
  }

  private List<Alert> loadAlertList() {
    List<Alert> alertList = new ArrayList<>();
    try (SQLiteDatabase db = PikettAssist.getReadableDatabase();
         Cursor cursor = db.rawQuery("SELECT _id, start_time, confirm_time, end_time FROM t_alert ORDER BY start_time DESC", null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          alertList.add(new Alert(
              id,
              Instant.ofEpochMilli(cursor.getLong(1)),
              cursor.getLong(2) > 0 ? Instant.ofEpochMilli(cursor.getLong(2)) : null,
              cursor.getLong(3) > 0 ? Instant.ofEpochMilli(cursor.getLong(3)) : null,
              Collections.emptyList()));
        } while (cursor.moveToNext());
      }
    }
    return alertList;
  }
}

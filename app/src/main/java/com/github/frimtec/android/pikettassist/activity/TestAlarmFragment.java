package com.github.frimtec.android.pikettassist.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE;

public class TestAlarmFragment extends AbstractListFragment<TestAlarm> {

  private static final int MENU_CONTEXT_VIEW_ID = 1;
  private static final int MENU_CONTEXT_DELETE_ID = 2;
  private static final int MENU_CONTEXT_ACTIVATE_ID = 3;
  private static final int MENU_CONTEXT_DEACTIVATE_ID = 4;

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      TestAlarm selectedAlert = (TestAlarm) listView.getItemAtPosition(position);
      showTestAlarmDetails(selectedAlert);
    });
    registerForContextMenu(listView);
  }

  @Override
  protected ArrayAdapter<TestAlarm> createAdapter() {
    return new TestAlarmArrayAdapter(getContext(), loadTestAlarmList());
  }

  @Override
  public void onResume() {
    super.onResume();
    refresh();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    TestAlarm selectedItem = (TestAlarm) getListView().getItemAtPosition(info.position);
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW_ID, Menu.NONE, R.string.list_item_menu_view);
    if (SharedState.getSuperviseTestContexts(getContext()).contains(selectedItem.getContext())) {
      menu.add(Menu.NONE, MENU_CONTEXT_DEACTIVATE_ID, Menu.NONE, R.string.list_item_menu_deactivate);
    } else {
      menu.add(Menu.NONE, MENU_CONTEXT_ACTIVATE_ID, Menu.NONE, R.string.list_item_menu_activate);
    }
    menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.list_item_menu_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ListView listView = getListView();
    TestAlarm selectedItem = (TestAlarm) listView.getItemAtPosition(info.position);
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW_ID:
        showTestAlarmDetails(selectedItem);
        return true;
      case MENU_CONTEXT_DELETE_ID:
        NotificationHelper.areYouSure(getContext(), (dialog, which) -> {
          deleteTestAlarm(selectedItem);
          refresh();
          Toast.makeText(getContext(), R.string.general_entry_deleted, Toast.LENGTH_SHORT).show();
        }, (dialog, which) -> {
        });
        return true;
      case MENU_CONTEXT_ACTIVATE_ID:
        Set<String> superviseTestContexts = SharedState.getSuperviseTestContexts(getContext());
        superviseTestContexts.add(selectedItem.getContext());
        SharedState.setSuperviseTestContexts(getContext(), superviseTestContexts);
        refresh();
        Toast.makeText(getContext(), R.string.test_alarm_activated_toast, Toast.LENGTH_SHORT).show();
        return true;
      case MENU_CONTEXT_DEACTIVATE_ID:
        superviseTestContexts = SharedState.getSuperviseTestContexts(getContext());
        superviseTestContexts.remove(selectedItem.getContext());
        SharedState.setSuperviseTestContexts(getContext(), superviseTestContexts);
        refresh();
        Toast.makeText(getContext(), R.string.test_alarm_deactivated_toast, Toast.LENGTH_SHORT).show();
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  protected Optional<View.OnClickListener> addAction() {
    return Optional.of(view -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      builder.setTitle(getString(R.string.test_alarm_add_title));
      EditText input = new EditText(getContext());
      input.setInputType(InputType.TYPE_CLASS_TEXT);
      input.requestFocus();
      builder.setView(input);
      builder.setPositiveButton(R.string.general_ok, (dialog, which) -> {
        dialog.dismiss();
        String newTestContext = input.getText().toString();

        try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
          try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_ID}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{newTestContext}, null, null, null)) {
            if (cursor.getCount() == 0) {
              ContentValues contentValues = new ContentValues();
              contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_ID, newTestContext);
              contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, 0);
              contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE, getString(R.string.test_alarm_message_empty));
              db.insert(TABLE_TEST_ALERT_STATE, null, contentValues);
              Set<String> superviseTestContexts = SharedState.getSuperviseTestContexts(getContext());
              superviseTestContexts.add(newTestContext);
              SharedState.setSuperviseTestContexts(getContext(), superviseTestContexts);
              refresh();
              Toast.makeText(getContext(), R.string.test_alarm_toast_added_success, Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(getContext(), R.string.test_alarm_toast_added_duplicate, Toast.LENGTH_LONG).show();
            }
          }
        }
      });
      builder.setNegativeButton(R.string.general_cancel, (dialog, which) -> dialog.cancel());
      builder.show();
    });
  }

  private void showTestAlarmDetails(TestAlarm selectedAlert) {
    Intent intent = new Intent(this.getContext(), TestAlarmDetailActivity.class);
    Bundle bundle = new Bundle();
    bundle.putString(TestAlarmDetailActivity.EXTRA_TEST_ALARM_CONTEXT, selectedAlert.getContext());
    intent.putExtras(bundle);
    startActivity(intent);
  }

  private void deleteTestAlarm(TestAlarm selectedTestAlarm) {
    try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
      db.delete(TABLE_TEST_ALERT_STATE, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(selectedTestAlarm.getContext())});
    }
    Set<String> superviseTestContexts = SharedState.getSuperviseTestContexts(getContext());
    superviseTestContexts.remove(selectedTestAlarm.getContext());
    SharedState.setSuperviseTestContexts(getContext(), superviseTestContexts);
  }

  private List<TestAlarm> loadTestAlarmList() {
    List<TestAlarm> list = new ArrayList<>();
    try (SQLiteDatabase db = PAssist.getReadableDatabase();
         Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_ALERT_COLUMN_ID}, null, null, null, null, null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          String id = cursor.getString(0);
          list.add(new TestAlarm(id));
        } while (cursor.moveToNext());
      }
    }
    list.sort(Comparator.comparing(TestAlarm::getContext));
    if (list.isEmpty()) {
      Toast.makeText(getContext(), getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
    }
    return list;
  }
}

package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TestAlarmFragment extends AbstractListFragment<TestAlarmContext> {

  private static final int MENU_CONTEXT_VIEW_ID = 1;
  private static final int MENU_CONTEXT_DELETE_ID = 2;
  private static final int MENU_CONTEXT_ACTIVATE_ID = 3;
  private static final int MENU_CONTEXT_DEACTIVATE_ID = 4;

  private final TestAlarmDao testAlarmDao;

  public TestAlarmFragment() {
    this(new TestAlarmDao());
  }

  @SuppressLint("ValidFragment")
  TestAlarmFragment(TestAlarmDao testAlarmDao) {
    super(FragmentPosition.TEST_ALARMS);
    this.testAlarmDao = testAlarmDao;
  }

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      TestAlarmContext selectedAlert = (TestAlarmContext) listView.getItemAtPosition(position);
      showTestAlarmDetails(selectedAlert);
    });
    registerForContextMenu(listView);
  }

  @Override
  protected ArrayAdapter<TestAlarmContext> createAdapter() {
    return new TestAlarmArrayAdapter(getContext(), loadTestAlarmList());
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View view, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    TestAlarmContext selectedItem = (TestAlarmContext) getListView().getItemAtPosition(info.position);
    addContextMenu(menu, MENU_CONTEXT_VIEW_ID, R.string.list_item_menu_view);
    if (ApplicationPreferences.instance().getSupervisedTestAlarms(getContext()).contains(selectedItem)) {
      addContextMenu(menu, MENU_CONTEXT_DEACTIVATE_ID, R.string.list_item_menu_deactivate);
    } else {
      addContextMenu(menu, MENU_CONTEXT_ACTIVATE_ID, R.string.list_item_menu_activate);
    }
    addContextMenu(menu, MENU_CONTEXT_DELETE_ID, R.string.list_item_menu_delete);
  }

  @Override
  public boolean onFragmentContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ListView listView = getListView();
    TestAlarmContext selectedItem = (TestAlarmContext) listView.getItemAtPosition(info.position);
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW_ID:
        showTestAlarmDetails(selectedItem);
        return true;
      case MENU_CONTEXT_DELETE_ID:
        DialogHelper.areYouSure(getContext(), (dialog, which) -> {
          deleteTestAlarm(selectedItem);
          refresh();
          Toast.makeText(getContext(), R.string.general_entry_deleted, Toast.LENGTH_SHORT).show();
        }, (dialog, which) -> {
        });
        return true;
      case MENU_CONTEXT_ACTIVATE_ID:
        Set<TestAlarmContext> supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext());
        supervisedTestAlarmContexts.add(selectedItem);
        ApplicationPreferences.instance().setSuperviseTestContexts(getContext(), supervisedTestAlarmContexts);
        refresh();
        Toast.makeText(getContext(), R.string.test_alarm_activated_toast, Toast.LENGTH_SHORT).show();
        return true;
      case MENU_CONTEXT_DEACTIVATE_ID:
        supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext());
        supervisedTestAlarmContexts.remove(selectedItem);
        ApplicationPreferences.instance().setSuperviseTestContexts(getContext(), supervisedTestAlarmContexts);
        refresh();
        Toast.makeText(getContext(), R.string.test_alarm_deactivated_toast, Toast.LENGTH_SHORT).show();
        return true;
      default:
        return false;
    }
  }

  @Override
  protected Optional<View.OnClickListener> addAction() {
    return Optional.of(view -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
      builder.setTitle(getString(R.string.test_alarm_add_title));
      EditText input = new EditText(getContext());
      input.setInputType(InputType.TYPE_CLASS_TEXT);
      input.requestFocus();
      builder.setView(input);
      builder.setPositiveButton(R.string.general_ok, (dialog, which) -> {
        dialog.dismiss();
        TestAlarmContext newTestAlarmContext = new TestAlarmContext(input.getText().toString());
        if (this.testAlarmDao.createNewContext(newTestAlarmContext, getString(R.string.test_alarm_message_empty))) {
          Set<TestAlarmContext> supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext());
          supervisedTestAlarmContexts.add(newTestAlarmContext);
          ApplicationPreferences.instance().setSuperviseTestContexts(getContext(), supervisedTestAlarmContexts);
          refresh();
          Toast.makeText(getContext(), R.string.test_alarm_toast_added_success, Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getContext(), R.string.test_alarm_toast_added_duplicate, Toast.LENGTH_LONG).show();
        }
      });
      builder.setNegativeButton(R.string.general_cancel, (dialog, which) -> dialog.cancel());
      builder.show();
    });
  }

  private void showTestAlarmDetails(TestAlarmContext selectedAlert) {
    Intent intent = new Intent(this.getContext(), TestAlarmDetailActivity.class);
    Bundle bundle = new Bundle();
    bundle.putString(TestAlarmDetailActivity.EXTRA_TEST_ALARM_CONTEXT, selectedAlert.getContext());
    intent.putExtras(bundle);
    startActivity(intent);
  }

  private void deleteTestAlarm(TestAlarmContext selectedTestAlarmContext) {
    this.testAlarmDao.delete(selectedTestAlarmContext);
    Set<TestAlarmContext> supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext());
    supervisedTestAlarmContexts.remove(selectedTestAlarmContext);
    ApplicationPreferences.instance().setSuperviseTestContexts(getContext(), supervisedTestAlarmContexts);
  }

  private List<TestAlarmContext> loadTestAlarmList() {
    List<TestAlarmContext> list = new ArrayList<>(this.testAlarmDao.loadAllContexts());
    list.sort(Comparator.comparing(TestAlarmContext::getContext));
    if (list.isEmpty()) {
      Toast.makeText(getContext(), getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
    }
    return list;
  }
}

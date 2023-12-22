package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter.Group;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static android.widget.ExpandableListView.getPackedPositionChild;
import static android.widget.ExpandableListView.getPackedPositionGroup;

public class TestAlarmFragment extends AbstractListFragment {

  private static final String TAG = "TestAlarmFragment";

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
  protected void configureListView(ExpandableListView listView) {
    listView.setClickable(true);
    listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
      Group<Boolean, TestAlarmContext> selectedStateGroup = (Group<Boolean, TestAlarmContext>) listView.getExpandableListAdapter().getGroup(groupPosition);
      TestAlarmContext selectedTestAlarmContext = selectedStateGroup.items().get(childPosition);
      showTestAlarmDetails(selectedTestAlarmContext);
      return true;
    });
    registerForContextMenu(listView);

    listView.setOnGroupExpandListener(groupPosition -> changeExpandedGroupsPreferences(listView, expandedStates -> {
      expandedStates.add(((Group<Boolean, TestAlarmContext>) listView.getExpandableListAdapter().getGroup(groupPosition)).key());
      return expandedStates;
    }));
    listView.setOnGroupCollapseListener(groupPosition -> changeExpandedGroupsPreferences(listView, expandedStates -> {
      expandedStates.remove(((Group<Boolean, TestAlarmContext>) listView.getExpandableListAdapter().getGroup(groupPosition)).key());
      return expandedStates;
    }));
  }

  private void changeExpandedGroupsPreferences(ExpandableListView listView, Function<Set<Boolean>, Set<Boolean>> transformer) {
    ApplicationPreferences applicationPreferences = ApplicationPreferences.instance();
    applicationPreferences.setExpandedTestAlertGroups(getContext(), transformer.apply(applicationPreferences.getExpandedTestAlertGroups(getContext())));
  }

  @Override
  protected ExpandableListAdapter createAdapter() {
    return new TestAlarmExpandableListAdapter(getContext(), loadTestAlarmList());
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View view, ContextMenu.ContextMenuInfo menuInfo) {
    ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
    if (getPackedPositionChild(info.packedPosition) == -1) {
      return;
    }
    TestAlarmContext selectedItem = (TestAlarmContext) getListView().getExpandableListAdapter().getChild(getPackedPositionGroup(info.packedPosition), getPackedPositionChild(info.packedPosition));
    addContextMenu(menu, MENU_CONTEXT_VIEW_ID, R.string.list_item_menu_view);
    if (ApplicationPreferences.instance().getSupervisedTestAlarms(getContext()).contains(selectedItem)) {
      addContextMenu(menu, MENU_CONTEXT_DEACTIVATE_ID, R.string.list_item_menu_deactivate);
    } else {
      addContextMenu(menu, MENU_CONTEXT_ACTIVATE_ID, R.string.list_item_menu_activate);
    }
    addContextMenu(menu, MENU_CONTEXT_DELETE_ID, R.string.list_item_menu_delete);
  }

  protected Set<Integer> getExpandedGroups(ExpandableListView listView) {
    ExpandableListAdapter adapter = listView.getExpandableListAdapter();
    Map<Boolean, Integer> stateToPosition = IntStream.range(0, adapter.getGroupCount())
        .boxed()
        .collect(Collectors.toMap(
            i -> ((Group<Boolean, TestAlarmContext>) adapter.getGroup(i)).key(),
            i -> i
        ));

    return ApplicationPreferences.instance().getExpandedTestAlertGroups(getContext()).stream()
        .filter(stateToPosition::containsKey)
        .map(stateToPosition::get)
        .collect(Collectors.toCollection(HashSet::new));
  }

  @Override
  public boolean onFragmentContextItemSelected(MenuItem item) {
    ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
    if (info == null) {
      Log.w(TAG, "No menu item was selected");
      return false;
    }
    TestAlarmContext selectedItem = (TestAlarmContext) getListView().getExpandableListAdapter().getChild(getPackedPositionGroup(info.packedPosition), getPackedPositionChild(info.packedPosition));
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
      builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
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
      builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
      builder.show();
    });
  }

  private void showTestAlarmDetails(TestAlarmContext selectedAlert) {
    Intent intent = new Intent(this.getContext(), TestAlarmDetailActivity.class);
    Bundle bundle = new Bundle();
    bundle.putString(TestAlarmDetailActivity.EXTRA_TEST_ALARM_CONTEXT, selectedAlert.context());
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
    list.sort(Comparator.comparing(TestAlarmContext::context));
    if (list.isEmpty()) {
      Toast.makeText(getContext(), getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
    }
    return list;
  }

  @Override
  protected boolean isAddButtonVisible() {
    return true;
  }
}

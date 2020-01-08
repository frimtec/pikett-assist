package com.github.frimtec.android.pikettassist.ui.alerts;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.service.ShiftService;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.Feature;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.ui.FragmentName.ALERT_LOG;

public class AlertListFragment extends AbstractListFragment<Alert> {

  private static final int MENU_CONTEXT_VIEW_ID = 1;
  private static final int MENU_CONTEXT_DELETE_ID = 2;
  private final AlertDao alertDao;

  public AlertListFragment() {
    this(new AlertDao());
  }

  @SuppressLint("ValidFragment")
  AlertListFragment(AlertDao alertDao) {
    super(ALERT_LOG);
    this.alertDao = alertDao;
  }

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
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW_ID, Menu.NONE, R.string.list_item_menu_view);
    menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.list_item_menu_delete);
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
        DialogHelper.areYouSure(getContext(), (dialog, which) -> {
          this.alertDao.delete(selectedAlert);
          refresh();
          Toast.makeText(getContext(), R.string.general_entry_deleted, Toast.LENGTH_SHORT).show();
        }, (dialog, which) -> {
        });
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  protected Optional<View.OnClickListener> addAction() {
    if (Feature.PERMISSION_CALENDAR_READ.isAllowed(getContext()) &&
        new ShiftService(getContext()).getState() == OnOffState.ON) {
      return Optional.of(view -> {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.manually_created_alarm_reason));
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(R.string.manually_created_alarm_reason_default);
        input.requestFocus();
        builder.setView(input);
        builder.setPositiveButton(R.string.general_ok, (dialog, which) -> {
          dialog.dismiss();
          String comment = input.getText().toString();
          AlertService alertService = new AlertService(getContext());
          alertService.newManuallyAlert(Instant.now(), comment);
          refresh();
        });
        builder.setNegativeButton(R.string.general_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
      });
    }
    return Optional.empty();
  }

  private void showAlertDetails(Alert selectedAlert) {
    Intent intent = new Intent(this.getContext(), AlertDetailActivity.class);
    Bundle bundle = new Bundle();
    bundle.putLong(AlertDetailActivity.EXTRA_ALERT_ID, selectedAlert.getId());
    intent.putExtras(bundle);
    startActivity(intent);
  }

  private List<Alert> loadAlertList() {
    List<Alert> alertList = this.alertDao.loadAll();
    if (alertList.isEmpty()) {
      Toast.makeText(getContext(), getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
    }
    return alertList;
  }
}

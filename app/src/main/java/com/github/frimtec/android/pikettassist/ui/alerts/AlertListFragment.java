package com.github.frimtec.android.pikettassist.ui.alerts;

import static java.time.temporal.ChronoUnit.DAYS;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.service.ShiftService;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.Feature;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class AlertListFragment extends AbstractListFragment<Alert> {

  private static final String TAG = "AlertActivity";

  private static final int MENU_CONTEXT_VIEW_ID = 1;
  private static final int MENU_CONTEXT_DELETE_ID = 2;
  private final AlertDao alertDao;

  public AlertListFragment() {
    this(new AlertDao());
  }

  private ImageButton exportButton;
  private TextView valueMonth;
  private TextView valueYear;

  private ActivityResultLauncher<Intent> newFileSelectionActivityResultLauncher;
  private ActivityResultLauncher<Intent> fileSelectionActivityResultLauncher;

  @SuppressLint("ValidFragment")
  AlertListFragment(AlertDao alertDao) {
    super(FragmentPosition.ALERT_LOG);
    this.alertDao = alertDao;
  }

  @Override
  public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.newFileSelectionActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              try {
                String fileContent = new AlertService(getContext()).exportAllAlerts();
                writeTextToUri(requireContext().getContentResolver(), data.getData(), fileContent);
                Toast.makeText(getContext(), R.string.alert_log_export_success, Toast.LENGTH_LONG).show();
              } catch (IOException e) {
                Log.e(TAG, "Cannot store export in file", e);
                Toast.makeText(getContext(), R.string.alert_log_export_failed, Toast.LENGTH_LONG).show();
              }
            }
          }
        }
    );

    this.fileSelectionActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              try {
                Context context = requireContext();
                String fileContent = readTextFromUri(context.getContentResolver(), data.getData());
                DialogHelper.yesNoDialog(context, R.string.import_alert_log_are_you_sure, (dialog, which) -> {
                  if (new AlertService(getContext()).importAllAlerts(fileContent)) {
                    Toast.makeText(getContext(), R.string.alert_log_import_success, Toast.LENGTH_LONG).show();
                  } else {
                    Toast.makeText(getContext(), R.string.alert_log_import_failed_bad_format, Toast.LENGTH_LONG).show();
                  }
                  refresh();
                }, (dialog, which) -> {
                });
              } catch (IOException e) {
                Log.e(TAG, "Cannot load import from file", e);
                Toast.makeText(getContext(), R.string.alert_log_import_failed, Toast.LENGTH_LONG).show();
              }
            }
          }
        }
    );
  }

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      Alert selectedAlert = (Alert) listView.getItemAtPosition(position);
      showAlertDetails(selectedAlert);
    });
    registerForContextMenu(listView);
    View headerView = getLayoutInflater().inflate(R.layout.alert_header, listView, false);
    this.exportButton = headerView.findViewById(R.id.alert_list_export);
    exportButton.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/json");
      intent.putExtra(Intent.EXTRA_TITLE, "passist-alert-log-export.json");
      newFileSelectionActivityResultLauncher.launch(intent);
    });
    this.valueMonth = headerView.findViewById(R.id.alert_statistic_value_month);
    this.valueYear = headerView.findViewById(R.id.alert_statistic_value_year);

    ImageButton importButton = headerView.findViewById(R.id.alert_list_import);
    importButton.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("application/json");
      fileSelectionActivityResultLauncher.launch(intent);
    });
    listView.addHeaderView(headerView);
  }

  private long countAlertsWithinLastDays(List<Alert> alertList, int days) {
    Instant lastDaysStart = Instant.now().minus(days, DAYS);
    return alertList.stream().filter(alert -> alert.getStartTime().isAfter(lastDaysStart)).count();
  }

  @Override
  protected ArrayAdapter<Alert> createAdapter() {
    return new AlertArrayAdapter(getContext(), loadAlertList());
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View view, ContextMenu.ContextMenuInfo menuInfo) {
    addContextMenu(menu, MENU_CONTEXT_VIEW_ID, R.string.list_item_menu_view);
    addContextMenu(menu, MENU_CONTEXT_DELETE_ID, R.string.list_item_menu_delete);
  }

  @Override
  public boolean onFragmentContextItemSelected(MenuItem item) {
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
        return false;
    }
  }

  @Override
  protected Optional<View.OnClickListener> addAction() {
    return Optional.of(view -> {
      AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
      builder.setTitle(getString(R.string.manually_created_alarm_reason));
      EditText input = new EditText(getContext());
      input.setInputType(InputType.TYPE_CLASS_TEXT);
      input.setText(R.string.manually_created_alarm_reason_default);
      input.requestFocus();
      builder.setView(input);
      builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        dialog.dismiss();
        String comment = input.getText().toString();
        AlertService alertService = new AlertService(getContext());
        alertService.newManuallyAlert(Instant.now(), comment);
        refresh();
      });
      builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
      builder.show();
    });
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
    if (this.exportButton != null) {
      this.exportButton.setVisibility(alertList.isEmpty() ? View.INVISIBLE : View.VISIBLE);
    }
    if (this.valueMonth != null) {
      this.valueMonth.setText(String.valueOf(countAlertsWithinLastDays(alertList, 30)));
    }
    if (this.valueYear != null) {
      this.valueYear.setText(String.valueOf(countAlertsWithinLastDays(alertList, 365)));
    }
    return alertList;
  }

  private static void writeTextToUri(ContentResolver contentResolver, Uri uri, String fileContent) throws IOException {
    try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
      if (outputStream == null) {
        throw new IOException("Null output stream for URI: " + uri);
      }
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
        writer.write(fileContent);
      }
    }
  }

  private static String readTextFromUri(ContentResolver contentResolver, Uri uri) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    try (InputStream inputStream = contentResolver.openInputStream(uri)) {
      if (inputStream == null) {
        throw new IOException("Null input stream for URI: " + uri);
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = reader.readLine()) != null) {
          stringBuilder.append(line);
        }
      }
    }
    return stringBuilder.toString();
  }

  @Override
  protected boolean isAddButtonVisible() {
    return Feature.PERMISSION_CALENDAR_READ.isAllowed(getContext()) &&
        new ShiftService(getContext()).getShiftState().isOn();
  }
}

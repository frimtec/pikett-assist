package com.github.frimtec.android.pikettassist.ui.overview;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.AlarmType.ALERT;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.service.BogusAlarmWorker;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertDetailActivity;

import java.time.Instant;
import java.util.function.Supplier;

class AlarmState extends State {

  private static final int MENU_CONTEXT_CREATE_ALARM_MANUALLY = 1;
  private static final int MENU_CONTEXT_BOGUS_ALARM = 2;
  private final StateContext stateContext;

  AlarmState(StateContext stateContext) {
    super(
        R.drawable.ic_siren,
        stateContext.getString(R.string.state_fragment_alarm_state),
        stateContext.getString(getAlertValue(stateContext.getAlarmState().first)),
        getSupplierButtons(stateContext),
        getAlertTrafficLight(stateContext.getAlarmState().first, stateContext.getShiftState().getState())
    );
    this.stateContext = stateContext;
  }

  @StringRes
  private static int getAlertValue(AlertState alertState) {
    if (alertState == AlertState.ON) {
      return R.string.alarm_state_on;
    } else if (alertState == AlertState.ON_CONFIRMED) {
      return R.string.alarm_state_on_confirmed;
    } else {
      return R.string.alarm_state_off;
    }
  }

  private static TrafficLight getAlertTrafficLight(AlertState alertState, OnOffState pikettState) {
    if (alertState == AlertState.ON) {
      return RED;
    } else if (alertState == AlertState.ON_CONFIRMED) {
      return YELLOW;
    } else {
      return pikettState == OnOffState.ON ? GREEN : OFF;
    }
  }

  private static Supplier<Button> getSupplierButtons(StateContext stateContext) {
    Supplier<Button> alarmCloseButtonSupplier = null;
    AlertState alertState = stateContext.getAlarmState().first;
    if (alertState != AlertState.OFF) {
      alarmCloseButtonSupplier = () -> {
        Button button = new Button(stateContext.getContext());
        boolean unconfirmed = alertState == AlertState.ON;
        button.setText(unconfirmed ? stateContext.getString(R.string.main_state_button_confirm_alert) : stateContext.getString(R.string.main_state_button_close_alert));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
        button.setOnClickListener(v -> {
          if (unconfirmed) {
            stateContext.confirmAlert();
          } else {
            stateContext.closeAlert();
          }
          stateContext.refreshFragment();
        });
        return button;
      };
    }
    return alarmCloseButtonSupplier;
  }


  @Override
  public void onClickAction(Context context) {
    if (stateContext.getAlarmState().second != null) {
      Intent intent = new Intent(stateContext.getContext(), AlertDetailActivity.class);
      Bundle bundle = new Bundle();
      bundle.putLong(AlertDetailActivity.EXTRA_ALERT_ID, stateContext.getAlarmState().second);
      intent.putExtras(bundle);
      stateContext.getContext().startActivity(intent);
    }
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    if (stateContext.getShiftState().isOn()) {
      stateContext.addContextMenu(menu, MENU_CONTEXT_CREATE_ALARM_MANUALLY, R.string.menu_create_manually_alarm);
    }
    stateContext.addContextMenu(menu, MENU_CONTEXT_BOGUS_ALARM, R.string.list_item_menu_bogus_alarm);
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_CREATE_ALARM_MANUALLY -> {
        AlertDialog.Builder builder = new AlertDialog.Builder(stateContext.getContext());
        builder.setTitle(stateContext.getString(R.string.manually_created_alarm_reason));
        EditText input = new EditText(stateContext.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(R.string.manually_created_alarm_reason_default);
        input.requestFocus();
        builder.setView(input);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
          dialog.dismiss();
          String comment = input.getText().toString();
          AlertService alertService = new AlertService(stateContext.getContext());
          alertService.newManuallyAlert(Instant.now(), comment);
          stateContext.refreshFragment();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
        return true;
      }
      case MENU_CONTEXT_BOGUS_ALARM -> {
        BogusAlarmWorker.scheduleBogusAlarm(context, ALERT);
        return true;
      }
      default -> {
        return false;
      }
    }
  }
}

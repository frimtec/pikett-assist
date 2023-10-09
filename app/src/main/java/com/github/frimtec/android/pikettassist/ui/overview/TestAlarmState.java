package com.github.frimtec.android.pikettassist.ui.overview;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.AlarmType.MISSING_TEST_ALARM;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Button;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.BogusAlarmWorker;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.ui.testalarm.TestAlarmDetailActivity;

import java.util.function.Supplier;

class TestAlarmState extends State {

  private static final int MENU_CONTEXT_BOGUS_ALARM = 1;

  private final StateContext stateContext;
  private final TestAlarmContext testAlarmContext;

  TestAlarmState(StateContext stateContext, TestAlarmStateContext testAlarmStateContext) {
    super(
        R.drawable.ic_test_alarm,
        testAlarmStateContext.testAlarmContext().context(),
        testAlarmStateContext.lastReceived(),
        getTestAlarmCloseButtonSupplier(testAlarmStateContext.stateContext(), testAlarmStateContext.testAlarmContext(), testAlarmStateContext.testAlarmState()),
        testAlarmStateContext.stateContext().getShiftState().isOn() ? (testAlarmStateContext.testAlarmState() == OnOffState.ON ? TrafficLight.RED : TrafficLight.GREEN) : TrafficLight.OFF);
    this.stateContext = stateContext;
    this.testAlarmContext = testAlarmStateContext.testAlarmContext();
  }

  private static Supplier<Button> getTestAlarmCloseButtonSupplier(StateContext stateContext, TestAlarmContext testAlarmContext, OnOffState testAlarmState) {
    Supplier<Button> testAlarmCloseButtonSupplier = null;
    if (testAlarmState != OnOffState.OFF) {
      testAlarmCloseButtonSupplier = () -> {
        Button button = new Button(stateContext.getContext());
        button.setText(stateContext.getString(R.string.main_state_button_close_alert));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0F);
        button.setOnClickListener(v -> {
          new TestAlarmDao().updateAlertState(testAlarmContext, OnOffState.OFF);
          stateContext.refreshFragment();
        });
        return button;
      };
    }
    return testAlarmCloseButtonSupplier;
  }

  @Override
  public void onClickAction(Context context) {
    Intent intent = new Intent(context, TestAlarmDetailActivity.class);
    Bundle bundle = new Bundle();
    bundle.putString(TestAlarmDetailActivity.EXTRA_TEST_ALARM_CONTEXT, testAlarmContext.context());
    intent.putExtras(bundle);
    context.startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    stateContext.addContextMenu(menu, MENU_CONTEXT_BOGUS_ALARM, R.string.list_item_menu_bogus_alarm);
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    if (item.getItemId() == MENU_CONTEXT_BOGUS_ALARM) {
      BogusAlarmWorker.scheduleBogusAlarm(context, MISSING_TEST_ALARM);
      return true;
    }
    return false;
  }
}

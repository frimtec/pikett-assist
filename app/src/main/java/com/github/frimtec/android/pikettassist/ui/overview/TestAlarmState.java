package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.BogusAlarmService;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.ui.testalarm.TestAlarmDetailActivity;

import java.util.function.Supplier;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmService.AlarmType.MISSING_TEST_ALARM;

class TestAlarmState extends State {

  private static final int MENU_CONTEXT_BOGUS_ALARM = 1;

  private final TestAlarmContext testAlarmContext;

  TestAlarmState(TestAlarmStateContext testAlarmStateContext) {
    super(
        R.drawable.ic_test_alarm,
        testAlarmStateContext.getTestAlarmContext().getContext(),
        testAlarmStateContext.getLastReceived(),
        getTestAlarmCloseButtonSupplier(testAlarmStateContext.getStateContext(), testAlarmStateContext.getTestAlarmContext(), testAlarmStateContext.getTestAlarmState()),
        testAlarmStateContext.getStateContext().getPikettState() == OnOffState.ON ? (testAlarmStateContext.getTestAlarmState() == OnOffState.ON ? TrafficLight.RED : TrafficLight.GREEN) : TrafficLight.OFF);
    this.testAlarmContext = testAlarmStateContext.getTestAlarmContext();
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
    bundle.putString(TestAlarmDetailActivity.EXTRA_TEST_ALARM_CONTEXT, testAlarmContext.getContext());
    intent.putExtras(bundle);
    context.startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    menu.add(Menu.NONE, MENU_CONTEXT_BOGUS_ALARM, Menu.NONE, R.string.list_item_menu_bogus_alarm);
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    if (item.getItemId() == MENU_CONTEXT_BOGUS_ALARM) {
      BogusAlarmService.scheduleBogusAlarm(context, MISSING_TEST_ALARM);
      return true;
    }
    return false;
  }
}

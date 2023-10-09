package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.LowSignalWorker;
import com.github.frimtec.android.pikettassist.service.PikettWorker;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import java.util.Calendar;

class OnCallState extends State {

  private static final String TAG = "OnCallState";

  private static final int MENU_CONTEXT_SET_MANUALLY_ON = 1;
  private static final int MENU_CONTEXT_RESET = 2;

  private final StateContext stateContext;

  OnCallState(StateContext stateContext) {
    super(
        R.drawable.ic_eye,
        stateContext.getString(R.string.state_fragment_pikett_state),
        String.format("%s %s", stateContext.getString(stateContext.getShiftState().isOn() ? (stateContext.isPikettStateManuallyOn() ? R.string.state_manually_on : (R.string.state_on)) : R.string.state_off), stateContext.getPikettStateDuration()),
        null,
        stateContext.getShiftState().isOn() ? (stateContext.isPikettStateManuallyOn() ? TrafficLight.YELLOW : TrafficLight.GREEN) : TrafficLight.OFF
    );
    this.stateContext = stateContext;
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    if (ApplicationState.instance().getPikettStateManuallyOn()) {
      stateContext.addContextMenu(menu, MENU_CONTEXT_RESET, R.string.list_item_menu_reset);
    } else {
      stateContext.addContextMenu(menu, MENU_CONTEXT_SET_MANUALLY_ON, R.string.list_item_menu_set_manually_on);
    }
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_SET_MANUALLY_ON -> {
        ApplicationState.instance().setPikettStateManuallyOn(true);
        LowSignalWorker.enqueueWork(context);
        PikettWorker.enqueueWork(context);
        stateContext.refreshFragment();
        return true;
      }
      case MENU_CONTEXT_RESET -> {
        ApplicationState.instance().setPikettStateManuallyOn(false);
        LowSignalWorker.enqueueWork(context);
        PikettWorker.enqueueWork(context);
        stateContext.refreshFragment();
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  @Override
  public void onClickAction(Context context) {
    Uri.Builder builder = CalendarContract.CONTENT_URI
        .buildUpon()
        .appendPath("time");
    ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
    try {
      stateContext.getContext().startActivity(
          new Intent(Intent.ACTION_VIEW).setData(builder.build())
      );
    } catch (ActivityNotFoundException e) {
      Log.e(TAG, "Cannot open calendar app with parameter time", e);
      Toast.makeText(context, R.string.toast_calendar_activity_not_found, Toast.LENGTH_LONG).show();
    }
  }
}

package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.LowSignalService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import java.util.Calendar;

class OnCallState extends State {

  private static final int MENU_CONTEXT_SET_MANUALLY_ON = 1;
  private static final int MENU_CONTEXT_RESET = 2;

  private final StateContext stateContext;

  OnCallState(StateContext stateContext) {
    super(
        R.drawable.ic_eye,
        stateContext.getString(R.string.state_fragment_pikett_state),
        String.format("%s %s", stateContext.getString(stateContext.getPikettState() == OnOffState.ON ? (stateContext.isPikettStateManuallyOn() ? R.string.state_manually_on : (R.string.state_on)) : R.string.state_off), stateContext.getPikettStateDuration()),
        null,
        stateContext.getPikettState() == OnOffState.ON ? (stateContext.isPikettStateManuallyOn() ? TrafficLight.YELLOW : TrafficLight.GREEN) : TrafficLight.OFF
    );
    this.stateContext = stateContext;
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    if (ApplicationState.getPikettStateManuallyOn()) {
      menu.add(Menu.NONE, MENU_CONTEXT_RESET, Menu.NONE, R.string.list_item_menu_reset);
    } else {
      menu.add(Menu.NONE, MENU_CONTEXT_SET_MANUALLY_ON, Menu.NONE, R.string.list_item_menu_set_manually_on);
    }
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_SET_MANUALLY_ON:
        ApplicationState.setPikettStateManuallyOn(true);
        LowSignalService.enqueueWork(context);
        PikettService.enqueueWork(context);
        stateContext.refreshFragment();
        return true;
      case MENU_CONTEXT_RESET:
        ApplicationState.setPikettStateManuallyOn(false);
        LowSignalService.enqueueWork(context);
        PikettService.enqueueWork(context);
        stateContext.refreshFragment();
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onClickAction(Context context) {
    Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
    builder.appendPath("time");
    ContentUris.appendId(builder, Calendar.getInstance().getTimeInMillis());
    Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
    stateContext.getContext().startActivity(intent);
  }
}

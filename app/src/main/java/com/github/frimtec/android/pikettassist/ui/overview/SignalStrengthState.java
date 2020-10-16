package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

class SignalStrengthState extends State {

  private static final int MENU_CONTEXT_DEACTIVATE = 1;
  private static final int MENU_CONTEXT_ACTIVATE = 2;

  private final StateContext stateContext;

  SignalStrengthState(StateContext stateContext) {
    super(
        R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp,
        stateContext.getNetworkOperatorName() != null ? String.format("%s %s", stateContext.getString(R.string.state_fragment_signal_level), stateContext.getNetworkOperatorName()) : stateContext.getString(R.string.state_fragment_signal_level),
        stateContext.isSuperviseSignalStrength() ? (stateContext.getPikettState() == OnOffState.ON ? getSignalStrength(stateContext) : stateContext.getString(R.string.general_enabled)) : stateContext.getString(R.string.general_disabled),
        null,
        getSignalStrengthTrafficLight(stateContext)
    );
    this.stateContext = stateContext;
  }

  private static String getSignalStrength(StateContext stateContext) {
    return stateContext.getSignalStrengthLevel().toString(stateContext.getContext());
  }

  private static TrafficLight getSignalStrengthTrafficLight(StateContext stateContext) {
    if (!stateContext.isSuperviseSignalStrength()) {
      return YELLOW;
    } else if (stateContext.getPikettState() == OnOffState.OFF) {
      return OFF;
    } else if (stateContext.getSignalStrengthLevel().ordinal() <= SignalLevel.NONE.ordinal()) {
      return RED;
    } else if (stateContext.getSignalStrengthLevel().ordinal() <= ApplicationPreferences.instance().getSuperviseSignalStrengthMinLevel(stateContext.getContext())) {
      return YELLOW;
    } else {
      return GREEN;
    }
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    if (ApplicationPreferences.instance().getSuperviseSignalStrength(context)) {
      menu.add(Menu.NONE, MENU_CONTEXT_DEACTIVATE, Menu.NONE, R.string.list_item_menu_deactivate);
    } else {
      menu.add(Menu.NONE, MENU_CONTEXT_ACTIVATE, Menu.NONE, R.string.list_item_menu_activate);
    }
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_DEACTIVATE:
        ApplicationPreferences.instance().setSuperviseSignalStrength(context, false);
        stateContext.refreshFragment();
        return true;
      case MENU_CONTEXT_ACTIVATE:
        ApplicationPreferences.instance().setSuperviseSignalStrength(context, true);
        stateContext.refreshFragment();
        return true;
      default:
        return false;
    }
  }
}

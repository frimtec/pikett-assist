package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

class DoNotDisturbState extends State {

  DoNotDisturbState(StateContext stateContext) {
    super(R.drawable.ic_do_not_disturb_on_black_24dp, stateContext.getString(R.string.state_fragment_do_not_disturb), stateContext.getString(R.string.state_on), null, TrafficLight.RED);
  }

  @Override
  public void onClickAction(Context context) {
    DialogHelper.infoDialog(context, R.string.do_not_disturb_info_title, R.string.do_not_disturb_info_text, (dialogInterface, integer) -> {});
  }
}

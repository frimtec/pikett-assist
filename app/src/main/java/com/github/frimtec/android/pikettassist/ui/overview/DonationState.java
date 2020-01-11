package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;

import com.github.frimtec.android.pikettassist.R;

class DonationState extends State {

  private final StateContext stateContext;

  DonationState(StateContext stateContext) {
    super(
        R.drawable.ic_monetization_on_black_24dp,
        stateContext.getString(R.string.state_fragment_donation),
        stateContext.getString(R.string.state_fragment_donation_value),
        null,
        TrafficLight.YELLOW
    );
    this.stateContext = stateContext;
  }

  @Override
  public void onClickAction(Context context) {
    stateContext.showDonationDialog();
  }
}

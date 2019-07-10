package com.github.frimtec.android.pikettassist.receiver;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.PikettState;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;

public class SignalStrengthListener extends PhoneStateListener {

  private static final String TAG = "SignalStrengthListener";

  private final Context context;

  public SignalStrengthListener(Context context) {
    this.context = context;
  }

  @Override
  public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    super.onSignalStrengthsChanged(signalStrength);
    Log.v(TAG, "SignalStrengthsChanged");
    context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    if (SharedState.getPikettState(context) == PikettState.ON) {
      Log.v(TAG, "Start signal strength service as pikett state is ON");
      context.startService(new Intent(context, SignalStrengthService.class));
    }
  }
}

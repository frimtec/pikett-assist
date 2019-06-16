package com.github.frimtec.android.pikettassist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE;

public class AlarmActionListener extends BroadcastReceiver {

  private static final String TAG = "ConfirmListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.v(TAG, "Action received: " + action);
    switch (action) {
      case ACTION_CLOSE:
        closeAlarm(context);
        break;
      default:
        Log.e(TAG, "Unknown action: " + action);
    }
  }

  private void closeAlarm(Context context) {
    SharedState.setAlarmState(context, AlarmState.OFF);
    NotificationHelper.cancel(context);
  }
}

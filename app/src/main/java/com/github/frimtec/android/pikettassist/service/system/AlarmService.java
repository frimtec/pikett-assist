package com.github.frimtec.android.pikettassist.service.system;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.LowSignalService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.TestAlarmService;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.function.Consumer;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;

public class AlarmService {

  private static final String TAG = "AlarmService";
  private static final String BASE_ACTION = "com.github.frimtec.android.pikettassist.ALARM.";

  private final Context context;
  private final AlarmManager alarmManager;

  public static final class ScheduleInfo {

    private final Duration scheduleDelay;
    private final Consumer<Intent> intentExtrasSetter;

    public ScheduleInfo(Duration scheduleDelay, Consumer<Intent> intentExtrasSetter) {
      this.scheduleDelay = scheduleDelay;
      this.intentExtrasSetter = intentExtrasSetter;
    }

    public ScheduleInfo(Duration scheduleDelay) {
      this.scheduleDelay = scheduleDelay;
      this.intentExtrasSetter = intent -> {
      };
    }

    public Duration getScheduleDelay() {
      return scheduleDelay;
    }

    public Consumer<Intent> getIntentExtrasSetter() {
      return intentExtrasSetter;
    }
  }

  public static class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null && action.startsWith(BASE_ACTION)) {
        String target = action.substring(BASE_ACTION.length());
        switch (JobService.valueOf(target)) {
          case LOW_SIGNAL_SERVICE:
            LowSignalService.enqueueWork(context, intent);
            break;
          case PIKETT_SERVICE:
            PikettService.enqueueWork(context, intent);
            break;
          case TEST_ALARM_SERVICE:
            TestAlarmService.enqueueWork(context, intent);
            break;
          default:
            Log.w(TAG, "Unknown target in action: " + target);
        }
      }
    }
  }

  public AlarmService(Context context) {
    this.context = context;
    this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
  }

  public void setAlarmForJob(ScheduleInfo scheduleInfo, JobService target) {
    Intent intent = new Intent();
    scheduleInfo.getIntentExtrasSetter().accept(intent);
    intent.setClass(this.context, AlarmService.Receiver.class);
    intent.setAction(BASE_ACTION + target.name());
    setAlarmForIntent(scheduleInfo.getScheduleDelay(),
        PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT)
    );
  }

  public void setAlarmForIntent(Duration scheduleDelay, PendingIntent intent) {
    this.alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        Instant.now().plus(scheduleDelay).toEpochMilli(),
        intent
    );
  }
}

package com.github.frimtec.android.pikettassist.ui.common;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.PowerService;
import com.github.frimtec.android.pikettassist.service.system.VibrateService;
import com.ncorti.slidetoact.SlideToActView;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public abstract class AbstractAlarmActivity extends AppCompatActivity {

  private static final int WAKE_LOCK_TIMEOUT = 1000 * 60 * 60;

  public static final int ONE_SECOND_MILLIS = 1000;
  public static final int VIBRATION_START_DELAY_MS = 50;

  protected enum SwipeButtonStyle {
    ALARM, NO_SIGNAL, MISSING_TEST_ALARM
  }

  @StringRes
  private final int alarmText;
  private final Pair<Integer, Integer> vibrationPattern;
  private final String tag;
  private final SwipeButtonStyle swipeButtonStyle;

  private Ringtone ringtone;
  private Timer ringtoneTimer;

  private Supplier<Boolean> endCondition;
  private Duration endConditionCheckInterval;
  private Timer endConditionTimer;

  private Runnable swipeAction = () -> {
  };

  private WakeLock wakeLock;
  private VibrateService vibrateService;

  public AbstractAlarmActivity(
      String tag,
      @StringRes int alarmText,
      Pair<Integer, Integer> vibrationPattern,
      SwipeButtonStyle swipeButtonStyle) {
    this.tag = tag;
    this.alarmText = alarmText;
    this.vibrationPattern = vibrationPattern;
    this.swipeButtonStyle = swipeButtonStyle;
  }

  protected final void setSwipeAction(Runnable swipeAction) {
    this.swipeAction = swipeAction;
  }

  protected final void setRingtone(Ringtone ringtone) {
    if(ringtone == null) {
      Log.w(tag, "Set ringtone is null");
      return;
    }
    this.ringtone = ringtone;
  }

  protected final void setEndCondition(Supplier<Boolean> endCondition, Duration checkInterval) {
    Objects.requireNonNull(endCondition);
    Objects.requireNonNull(checkInterval);
    this.endCondition = endCondition;
    this.endConditionCheckInterval = checkInterval;
    this.endConditionTimer = new Timer();
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    Log.v(tag, "onCreate");
    super.onCreate(savedInstanceState);
    this.vibrateService = new VibrateService(this);
    this.wakeLock = new PowerService(this).newWakeLock("alarmActivity");
    if (!wakeLock.isHeld()) {
      wakeLock.acquire(WAKE_LOCK_TIMEOUT);
    }

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    int flags =
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
    getWindow().setFlags(flags, flags);
    setContentView(R.layout.alarm);

    TextView textView = findViewById(R.id.alarm_text);
    textView.setText(alarmText);

    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.hide();
    }

    SlideToActView swipeButton = switch (swipeButtonStyle) {
      case ALARM -> findViewById(R.id.alarm_button_confirm_alarm);
      case NO_SIGNAL -> findViewById(R.id.alarm_button_confirm_no_signal);
      case MISSING_TEST_ALARM -> findViewById(R.id.alarm_button_confirm_test_alarm);
    };
    swipeButton.setVisibility(View.VISIBLE);
    swipeButton.setOnSlideCompleteListener(active -> {
      swipeAction.run();
      finish();
    });

    if (this.endCondition != null) {
      this.endConditionTimer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          if (AbstractAlarmActivity.this.endCondition.get()) {
            endConditionTimer.cancel();
            finish();
          }
        }
      }, endConditionCheckInterval.toMillis(), endConditionCheckInterval.toMillis());
    }
  }

  @Override
  protected void onStart() {
    Log.v(tag, "onStart");
    super.onStart();

    if (this.ringtone != null) {
      this.ringtone.play();
      this.ringtoneTimer = new Timer();
      this.ringtoneTimer.scheduleAtFixedRate(new TimerTask() {
        public void run() {
          if (!AbstractAlarmActivity.this.ringtone.isPlaying()) {
            AbstractAlarmActivity.this.ringtone.play();
          }
        }
      }, ONE_SECOND_MILLIS, ONE_SECOND_MILLIS);
    }
    startVibrate();
  }

  @Override
  protected void onPause() {
    Log.v(tag, "onPause");
    super.onPause();
  }

  @Override
  protected void onResume() {
    Log.v(tag, "onResume");
    super.onResume();
    startVibrate();
  }

  @Override
  protected void onRestart() {
    Log.v(tag, "onRestart");
    super.onRestart();
  }

  @Override
  protected void onStop() {
    Log.v(tag, "onStop");
    super.onStop();
    if (this.ringtone != null) {
      if (ringtoneTimer != null) {
        ringtoneTimer.cancel();
        ringtoneTimer = null;
      }
      ringtone.stop();
    }
    this.vibrateService.cancel();
  }

  @Override
  protected void onDestroy() {
    Log.v(tag, "onDestroy");
    super.onDestroy();
    if (wakeLock.isHeld()) {
      wakeLock.release();
    }

    this.sendBroadcast(new Intent(Action.REFRESH.getId()));
  }

  private void startVibrate() {
    Timer oneShot = new Timer();
    oneShot.schedule(new TimerTask() {
      public void run() {
        AbstractAlarmActivity.this.vibrateService.vibrate(vibrationPattern.first, vibrationPattern.second);
        oneShot.cancel();
      }
    }, VIBRATION_START_DELAY_MS, ONE_SECOND_MILLIS);
  }

  protected static void trigger(
      Class<? extends AbstractAlarmActivity> activityClass,
      Context context,
      AlarmService alarmService,
      List<Pair<String, String>> extras) {
    Intent alarmIntent = new Intent(context, activityClass);
    extras.forEach(extra -> alarmIntent.putExtra(extra.first, extra.second));
    PendingIntent pendingIntent = PendingIntent.getActivity(context,
        1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    alarmService.setAlarmForIntent(Duration.ofMillis(5), pendingIntent);
  }
}

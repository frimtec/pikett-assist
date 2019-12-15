package com.github.frimtec.android.pikettassist.ui.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.utility.VibrateHelper;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

import in.shadowfax.proswipebutton.ProSwipeButton;

public abstract class AbstractAlarmActivity extends AppCompatActivity {

  protected enum SwipeButtonStyle {
    RED, GREEN, BLUE
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

  private PowerManager.WakeLock wakeLock;
  private Vibrator vibrate;

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
    Objects.requireNonNull(ringtone);
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
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    Objects.requireNonNull(pm);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag + ":alarm");
    Objects.requireNonNull(wakeLock);
    if (!wakeLock.isHeld()) {
      wakeLock.acquire(0);
    }

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    setContentView(R.layout.alarm);

    TextView textView = findViewById(R.id.alarm_text);
    textView.setText(alarmText);

    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.hide();
    }

    ProSwipeButton swipeButton = findViewById(R.id.alarm_button_confirm);
    switch (swipeButtonStyle) {
      case RED:
        swipeButton.setBackgroundColor(getColor(R.color.confirmButtonBackRed));
        swipeButton.setTextColor(getColor(R.color.confirmButtonTextRed));
        swipeButton.setArrowColor(getColor(R.color.confirmButtonArrowRed));
        break;
      case GREEN:
        swipeButton.setBackgroundColor(getColor(R.color.confirmButtonBackGreen));
        swipeButton.setTextColor(getColor(R.color.confirmButtonTextGreen));
        swipeButton.setArrowColor(getColor(R.color.confirmButtonArrowGreen));
        break;
      case BLUE:
        swipeButton.setBackgroundColor(getColor(R.color.confirmButtonBackBlue));
        swipeButton.setTextColor(getColor(R.color.confirmButtonTextBlue));
        swipeButton.setArrowColor(getColor(R.color.confirmButtonArrowBlue));
        break;
      default:
        throw new IllegalStateException("Unsupported style: " + swipeButtonStyle);
    }
    swipeButton.setOnSwipeListener(() -> {
      swipeButton.showResultIcon(true);
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
      }, 1000, 1000);
    }
    vibrate = VibrateHelper.vibrate(this, vibrationPattern.first, vibrationPattern.second);
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
    vibrate.cancel();
  }

  @Override
  protected void onDestroy() {
    Log.v(tag, "onDestroy");
    super.onDestroy();
    if (wakeLock.isHeld()) {
      wakeLock.release();
    }

    this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
  }

  protected static void trigger(
      Class<? extends AbstractAlarmActivity> activityClass,
      Context context, AlarmManager alarmManager,
      List<Pair<String, String>> extras) {
    Intent alarmIntent = new Intent(context, activityClass);
    extras.forEach(extra -> alarmIntent.putExtra(extra.first, extra.second));
    PendingIntent pendingIntent = PendingIntent.getActivity(context,
        1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    Objects.requireNonNull(alarmManager);
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Instant.now().toEpochMilli() + 5, pendingIntent);
  }
}

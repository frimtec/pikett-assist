package com.github.frimtec.android.pikettassist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.helper.VibrateHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import in.shadowfax.proswipebutton.ProSwipeButton;

import static com.github.frimtec.android.pikettassist.service.SignalStrengthService.isLowSignal;

public class LowSignalAlarmActivity extends AppCompatActivity {

  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private PowerManager.WakeLock wakeLock;
  private Vibrator vibrate;
  private Timer timer;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    Objects.requireNonNull(pm);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LowSignalAlarmActivity:alarm");
    Objects.requireNonNull(wakeLock);
    wakeLock.acquire(0);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    setContentView(R.layout.alarm);

    TextView textView = findViewById(R.id.alarm_text);
    textView.setText(R.string.notification_low_signal_title);

    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.hide();
    }
    stopped.set(false);

    this.timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        SignalStrengthHelper.SignalLevel level = SignalStrengthHelper.getSignalStrength(LowSignalAlarmActivity.this);
        if (!isLowSignal(level) || !SharedState.getSuperviseSignalStrength(getApplicationContext())) {
          stopped.set(true);
          finish();
        }
      }
    }, 0, 1000);

    ProSwipeButton swipeButton = findViewById(R.id.alarm_button_confirm);
    swipeButton.setOnSwipeListener(() -> {
      swipeButton.showResultIcon(true);
      stopped.set(true);
      finish();
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    vibrate = VibrateHelper.vibrate(LowSignalAlarmActivity.this, 100, 500);
  }

  @Override
  protected void onStop() {
    super.onStop();
    this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    if (stopped.get()) {
      super.onStop();
      timer.cancel();
      vibrate.cancel();
      wakeLock.release();
    }
  }
}
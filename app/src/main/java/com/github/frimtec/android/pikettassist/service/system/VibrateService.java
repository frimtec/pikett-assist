package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrateService {

  private final Vibrator vibrator;

  public VibrateService(Context context) {
    this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
  }

  public void vibrate(long onPhaseMs, long pausePhaseMs) {
    if (this.vibrator != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, onPhaseMs, pausePhaseMs}, 1));
      } else {
        this.vibrator.vibrate(new long[]{0, onPhaseMs, pausePhaseMs}, 0);
      }
    }
  }

  public void cancel() {
    if (this.vibrator != null) {
      this.vibrator.cancel();
    }
  }
}
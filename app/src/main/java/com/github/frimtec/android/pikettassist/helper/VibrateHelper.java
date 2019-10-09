package com.github.frimtec.android.pikettassist.helper;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrateHelper {

  public static Vibrator vibrate(Context context, long onPhaseMs, long pausePhaseMs) {
    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    if (vibrator != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, onPhaseMs, pausePhaseMs}, 1));
      } else {
        vibrator.vibrate(new long[]{0, onPhaseMs, pausePhaseMs}, 0);
      }
    }
    return vibrator;
  }
}

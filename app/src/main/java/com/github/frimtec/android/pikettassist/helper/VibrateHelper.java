package com.github.frimtec.android.pikettassist.helper;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrateHelper {
  public static void vibrateWhileDoing(Context context, long onPhaseMs, long pausePhaseMs, Runnable executeWhileVibrating) {
    Vibrator vibrator = vibrate(context, onPhaseMs, pausePhaseMs);
    try {
      executeWhileVibrating.run();
    } finally {
      vibrator.cancel();
    }
  }

  public static Vibrator vibrate(Context context, long onPhaseMs, long pausePhaseMs) {
    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, onPhaseMs, pausePhaseMs}, 1));
    return vibrator;
  }
}

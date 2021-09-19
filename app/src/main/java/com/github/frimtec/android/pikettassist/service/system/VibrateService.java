package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.os.Build;
import android.os.CombinedVibration;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class VibrateService {

  interface VibratorApi {

    void vibrate(long onPhaseMs, long pausePhaseMs);

    void cancel();

    static VibratorApi create(Context context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return new VibratorApi() {
          private final VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);

          @Override
          public void vibrate(long onPhaseMs, long pausePhaseMs) {
            if (this.vibratorManager != null) {
              this.vibratorManager.vibrate(CombinedVibration.createParallel(VibrationEffect.createWaveform(new long[]{0, onPhaseMs, pausePhaseMs}, 1)));
            }
          }

          @Override
          public void cancel() {
            if (this.vibratorManager != null) {
              this.vibratorManager.cancel();
            }
          }
        };
      } else {
        //noinspection deprecation
        return new VibratorApi() {
          private final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

          @Override
          public void vibrate(long onPhaseMs, long pausePhaseMs) {
            if (this.vibrator != null) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, onPhaseMs, pausePhaseMs}, 1));
              } else {
                this.vibrator.vibrate(new long[]{0, onPhaseMs, pausePhaseMs}, 0);
              }
            }
          }

          @Override
          public void cancel() {
            if (this.vibrator != null) {
              this.vibrator.cancel();
            }
          }
        };
      }
    }
  }

  private final VibratorApi vibratorApi;

  public VibrateService(Context context) {
    this.vibratorApi = VibratorApi.create(context);
  }

  public void vibrate(long onPhaseMs, long pausePhaseMs) {
    this.vibratorApi.vibrate(onPhaseMs, pausePhaseMs);
  }

  public void cancel() {
    this.vibratorApi.cancel();
  }
}
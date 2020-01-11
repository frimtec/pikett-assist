package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class VolumeService {

  private static final String TAG = "VolumeService";

  private static final int MAX_LEVEL = 7;
  private static final int MIN_LEVEL = 0;

  private final AudioManager audioManager;
  private final Context context;

  public VolumeService(Context context) {
    this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    this.context = context;
  }

  public int getVolume() {
    return this.audioManager.getStreamVolume(AudioManager.STREAM_RING);
  }

  public void setVolume(int volumeLevel) {
    int desiredLevel = Math.min(Math.max(volumeLevel, MIN_LEVEL), MAX_LEVEL);
    int currentLevel = audioManager.getStreamVolume(AudioManager.STREAM_RING);
    if (currentLevel != desiredLevel) {
      this.audioManager.setStreamVolume(AudioManager.STREAM_RING, desiredLevel, 0);
      new NotificationService(context).notifyVolumeChanged(currentLevel, desiredLevel);
      Log.i(TAG, String.format("Change volume from %d to %d.", currentLevel, desiredLevel));
    }
  }
}

package com.github.frimtec.android.pikettassist.ui.billing;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class DonationReminderHelper {

  private static final String TAG = "DonationReminderHelper";

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static boolean randomizedOn(Context context, float probability) {
    long installationAgeInDays = Integer.MAX_VALUE;
    if (context != null) {
      try {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        installationAgeInDays = Duration.between(Instant.ofEpochMilli(packageInfo.firstInstallTime), Instant.now()).toDays();
      } catch (PackageManager.NameNotFoundException e) {
        Log.e(TAG, "Can not get package info", e);
      }
    }
    return RANDOM.nextFloat() <= Math.min((installationAgeInDays - 30f) * 0.01f, probability);
  }

}

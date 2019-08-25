package com.github.frimtec.android.pikettassist.helper;

import android.Manifest;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

import java.util.Arrays;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionHelper {
  public static final String[] REQUIRED_PERMISSIONS = {
      Manifest.permission.SEND_SMS,
      Manifest.permission.RECEIVE_SMS,
      Manifest.permission.READ_CONTACTS,
      Manifest.permission.READ_CALENDAR,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.RECEIVE_BOOT_COMPLETED,
      Manifest.permission.VIBRATE,
  };

  public static boolean hasMissingPermissions(Context context) {
    return Arrays.stream(REQUIRED_PERMISSIONS).anyMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

}

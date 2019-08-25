package com.github.frimtec.android.pikettassist.helper;

import android.Manifest;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionHelper {
  public static final String[] REQUIRED_LOW_RISK_PERMISSIONS = {
      Manifest.permission.READ_CONTACTS,
      Manifest.permission.READ_CALENDAR,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.RECEIVE_BOOT_COMPLETED,
      Manifest.permission.VIBRATE,
  };

  public static final String[] REQUIRED_SMS_PERMISSIONS = {
      Manifest.permission.SEND_SMS,
      Manifest.permission.RECEIVE_SMS
  };

  private static final List<String>  ALL_REQUIRED_PERMISSIONS = new ArrayList<>();

  static {
    ALL_REQUIRED_PERMISSIONS.addAll(Arrays.asList(REQUIRED_SMS_PERMISSIONS));
    ALL_REQUIRED_PERMISSIONS.addAll(Arrays.asList(REQUIRED_LOW_RISK_PERMISSIONS));
  }

  public static boolean hasMissingPermissions(Context context) {
    return ALL_REQUIRED_PERMISSIONS.stream().anyMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

  public static boolean hasMissingSmsPermissions(Context context) {
    return Stream.of(REQUIRED_SMS_PERMISSIONS).anyMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

  public static boolean hasMissingLowRiskSmsPermissions(Context context) {
    return Stream.of(REQUIRED_LOW_RISK_PERMISSIONS).anyMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

}

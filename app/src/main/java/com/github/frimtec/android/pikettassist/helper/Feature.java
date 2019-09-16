package com.github.frimtec.android.pikettassist.helper;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;

import com.github.frimtec.android.pikettassist.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.pikettassist.helper.Feature.RequestCodes.FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;
import static com.github.frimtec.android.pikettassist.helper.Feature.RequestCodes.PERMISSION_CHANGED_REQUEST_CODE;

public enum Feature {
  PERMISSION_SMS(true, true, R.string.permission_sms_title, context -> allPermissionsGranted(context, PermissionSets.SMS.getPermissions()), (context, fragment) -> {
    requestPermissionsWithExplanation(context, fragment, PermissionSets.SMS.getPermissions(), R.string.permission_sms_title, R.string.permission_sms_text);
  }),
  PERMISSION_CONTACTS_READ(true, true, R.string.permission_contacts_title, context -> allPermissionsGranted(context, PermissionSets.CONTACTS_READ.getPermissions()), (context, fragment) -> {
    requestPermissionsWithExplanation(context, fragment, PermissionSets.CONTACTS_READ.getPermissions(), R.string.permission_contacts_title, R.string.permission_contacts_text);
  }),
  PERMISSION_CALENDAR_READ(true, true, R.string.permission_calendar_title, context -> allPermissionsGranted(context, PermissionSets.CALENDAR_READ.getPermissions()), (context, fragment) -> {
    requestPermissionsWithExplanation(context, fragment, PermissionSets.CALENDAR_READ.getPermissions(), R.string.permission_calendar_title, R.string.permission_calendar_text);
  }),
  PERMISSION_COARSE_LOCATION(true, true, R.string.permission_location_title, context -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || allPermissionsGranted(context, PermissionSets.COARSE_LOCATION.getPermissions()), (context, fragment) -> {
    requestPermissionsWithExplanation(context, fragment, PermissionSets.COARSE_LOCATION.getPermissions(), R.string.permission_location_title, R.string.permission_location_text);
  }),
  PERMISSION_NON_CRITICAL(false, true, 0, context -> allPermissionsGranted(context, PermissionSets.NON_CRITICAL.getPermissions()), (context, fragment) -> {
    requestPermissions(fragment, PermissionSets.NON_CRITICAL.getPermissions());
  }),
  SETTING_DRAW_OVERLAYS(false, false, R.string.notification_draw_overlays_title, Settings::canDrawOverlays, (context, fragment) -> {
    NotificationHelper.infoDialog(context, R.string.notification_draw_overlays_title, R.string.notification_draw_overlays_text, (dialogInterface, integer) -> {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
      fragment.startActivityForResult(intent, FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    });
  }),
  SETTING_BATTERY_OPTIMIZATION_OFF(false, false, R.string.notification_battery_optimization_title, context -> {
    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    return pm.isIgnoringBatteryOptimizations(context.getPackageName());
  }, (context, fragment) -> {
    NotificationHelper.infoDialog(context, R.string.notification_battery_optimization_title, R.string.notification_battery_optimization_text, (dialogInterface, integer) -> {
    });
  });

  private final boolean sensitive;
  private final boolean permissionType;
  private final int nameResourceId;
  private final Function<Context, Boolean> allowed;
  private final BiConsumer<Context, Fragment> request;

  Feature(boolean sensitive, boolean permissionType, int nameResourceId, Function<Context, Boolean> allowed, BiConsumer<Context, Fragment> request) {
    this.sensitive = sensitive;
    this.permissionType = permissionType;
    this.nameResourceId = nameResourceId;
    this.allowed = allowed;
    this.request = request;
  }

  public final boolean isAllowed(Context context) {
    return this.allowed.apply(context);
  }

  public final void request(Context context, Fragment fragment) {
    request.accept(context, fragment);
  }

  public boolean isSensitive() {
    return sensitive;
  }

  public boolean isPermissionType() {
    return permissionType;
  }

  public int getNameResourceId() {
    return nameResourceId;
  }

  private static boolean allPermissionsGranted(Context context, String[] permissions) {
    return Stream.of(permissions)
        .noneMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

  private static void requestPermissionsWithExplanation(Context context, Fragment fragment, String[] permissions, int titleResourceId, int textResourceId) {
    NotificationHelper.requirePermissions(context, titleResourceId, textResourceId, (dialogInterface, integer) -> requestPermissions(fragment, permissions));
  }

  private static void requestPermissions(Fragment fragment, String[] permissions) {
    ActivityCompat.requestPermissions(fragment.getActivity(), permissions, PERMISSION_CHANGED_REQUEST_CODE);
  }

  public final static class RequestCodes {

    public final static int PERMISSION_CHANGED_REQUEST_CODE = 1;
    public final static int FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2;
  }

  private enum PermissionSets {
    SMS(new HashSet<>(Arrays.asList(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS))),
    CONTACTS_READ(Collections.singleton(Manifest.permission.READ_CONTACTS)),
    CALENDAR_READ(Collections.singleton(Manifest.permission.READ_CALENDAR)),
    COARSE_LOCATION(Collections.singleton(Manifest.permission.ACCESS_COARSE_LOCATION)),
    NON_CRITICAL(Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.INTERNET
    ))));

    private final String[] permissions;

    PermissionSets(Set<String> permissions) {
      this.permissions = permissions.toArray(new String[0]);
    }

    public String[] getPermissions() {
      return permissions;
    }
  }
}

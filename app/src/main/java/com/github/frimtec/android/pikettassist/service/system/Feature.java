package com.github.frimtec.android.pikettassist.service.system;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.PikettWorker;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Feature {
  PERMISSION_CONTACTS_READ(
      true,
      true,
      R.string.permission_contacts_title,
      context -> allPermissionsGranted(context, PermissionSets.CONTACTS_READ.getPermissions())
  ) {
    @Override
    public void request(Context context) {
      requestPermissionsWithExplanation(
          context,
          PermissionSets.CONTACTS_READ.getPermissions(),
          R.string.permission_contacts_title,
          R.string.permission_contacts_text
      );
    }
  },
  PERMISSION_CALENDAR_READ(
      true,
      true,
      R.string.permission_calendar_title,
      context -> allPermissionsGranted(context, PermissionSets.CALENDAR_READ.getPermissions())
  ) {
    @Override
    public void request(Context context) {
      requestPermissionsWithExplanation(
          context,
          PermissionSets.CALENDAR_READ.getPermissions(),
          R.string.permission_calendar_title,
          R.string.permission_calendar_text
      );
    }
  },
  PERMISSION_POST_NOTIFICATIONS(
      true,
      true,
      R.string.permission_post_notifications_title,
      context -> Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || allPermissionsGranted(context, PermissionSets.POST_NOTIFICATIONS.getPermissions())
  ) {
    @Override
    public void request(Context context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestPermissionsWithExplanation(
            context,
            PermissionSets.POST_NOTIFICATIONS.getPermissions(),
            R.string.permission_post_notifications_title,
            R.string.permission_post_notifications_text
        );
      }
    }
  },
  PERMISSION_WRITE_EXTERNAL_STORAGE(
      true,
      true,
      R.string.permission_write_external_storage_title,
      context -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || allPermissionsGranted(context, PermissionSets.WRITE_EXTERNAL_STORAGE.getPermissions())
  ) {
    @Override
    public void request(Context context) {
      requestPermissionsWithExplanation(
          context,
          PermissionSets.WRITE_EXTERNAL_STORAGE.getPermissions(),
          R.string.permission_write_external_storage_title,
          R.string.permission_write_external_storage_text
      );
    }
  },
  PERMISSION_COARSE_LOCATION(
      true,
      true,
      R.string.permission_location_title,
      context -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || allPermissionsGranted(context, PermissionSets.COARSE_LOCATION.getPermissions())
  ) {
    @Override
    public void request(Context context) {
      requestPermissionsWithExplanation(
          context,
          PermissionSets.COARSE_LOCATION.getPermissions(),
          R.string.permission_location_title,
          R.string.permission_location_text
      );
    }
  },
  PERMISSION_NON_CRITICAL(
      false,
      true,
      0,
      context -> allPermissionsGranted(context, PermissionSets.NON_CRITICAL.getPermissions())
  ) {
    @Override
    public void request(Context context) {
      requestPermissions(PermissionSets.NON_CRITICAL.getPermissions());
    }
  },
  SETTING_DRAW_OVERLAYS(
      false,
      false,
      R.string.notification_draw_overlays_title,
      Settings::canDrawOverlays
  ) {
    @Override
    protected ActivityResultLauncher<Intent> registerActivityLauncher(Fragment fragment) {
      return fragment.registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            Context context = fragment.getContext();
            if (context != null && SETTING_DRAW_OVERLAYS.isAllowed(context)) {
              PikettWorker.enqueueWork(context);
            }
          }
      );
    }

    @Override
    public void request(Context context) {
      DialogHelper.infoDialog(
          context,
          R.string.notification_draw_overlays_title,
          R.string.notification_draw_overlays_text,
          (dialogInterface, integer) -> getActivityLauncher().launch(
              new Intent(
                  Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:" + context.getPackageName())
              )
          )
      );
    }
  },
  SETTING_SCHEDULE_EXACT_ALARM(
      false,
      false,
      R.string.notification_schedule_exact_alarm_title,
      context -> Build.VERSION.SDK_INT < Build.VERSION_CODES.S || context.getSystemService(AlarmManager.class).canScheduleExactAlarms()
  ) {
    @Override
    protected ActivityResultLauncher<Intent> registerActivityLauncher(Fragment fragment) {
      return fragment.registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> Log.i(TAG, "Return from grant schedule exact alarm activity; result=" + result.getResultCode())
      );
    }

    @Override
    public void request(Context context) {
      DialogHelper.infoDialog(
          context,
          R.string.notification_schedule_exact_alarm_title,
          R.string.notification_schedule_exact_alarm_text,
          (dialogInterface, integer) -> {
            @SuppressLint("InlinedApi")
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            getActivityLauncher().launch(intent);
          }
      );
    }
  },
  SETTING_BATTERY_OPTIMIZATION_OFF(
      false,
      false,
      R.string.notification_battery_optimization_title,
      context -> {
        return new PowerService(context).isIgnoringBatteryOptimizations(context.getPackageName());
      }
  ) {
    @Override
    protected ActivityResultLauncher<Intent> registerActivityLauncher(Fragment fragment) {
      return fragment.registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> Log.i(TAG, "Return from grant battery optimization deactivation activity; result=" + result.getResultCode())
      );
    }

    @Override
    public void request(Context context) {
      DialogHelper.infoDialog(
          context,
          R.string.notification_battery_optimization_title,
          R.string.notification_battery_optimization_text,
          (dialogInterface, integer) -> {
            @SuppressLint("BatteryLife")
            Intent batteryIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            batteryIntent.setData(Uri.parse("package:" + context.getPackageName()));
            getActivityLauncher().launch(batteryIntent);
          }
      );
    }
  };

  private static final String TAG = "Feature";

  private final boolean sensitive;
  private final boolean permissionType;
  private final int nameResourceId;
  private final Function<Context, Boolean> allowed;

  private Fragment fragment = null;
  private ActivityResultLauncher<Intent> activityLauncher = null;

  Feature(boolean sensitive, boolean permissionType, int nameResourceId, Function<Context, Boolean> allowed) {
    this.sensitive = sensitive;
    this.permissionType = permissionType;
    this.nameResourceId = nameResourceId;
    this.allowed = allowed;
  }

  public final void registerFragment(Fragment fragment) {
    this.fragment = fragment;
    this.activityLauncher = registerActivityLauncher(fragment);
  }

  protected ActivityResultLauncher<Intent> registerActivityLauncher(Fragment fragment) {
    return null;
  }

  public final boolean isAllowed(Context context) {
    return this.allowed.apply(context);
  }

  public abstract void request(Context context);

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

  protected final void requestPermissionsWithExplanation(Context context, String[] permissions, int titleResourceId, int textResourceId) {
    DialogHelper.requirePermissions(context, titleResourceId, textResourceId, (dialogInterface, integer) -> requestPermissions(permissions));
  }

  public ActivityResultLauncher<Intent> getActivityLauncher() {
    return Objects.requireNonNull(activityLauncher);
  }

  protected final void requestPermissions(String[] permissions) {
    FragmentActivity activity = fragment.getActivity();
    if (activity != null) {
      ActivityCompat.requestPermissions(activity, permissions, 1);
    }
  }

  private enum PermissionSets {
    CONTACTS_READ(Collections.singleton(Manifest.permission.READ_CONTACTS)),
    CALENDAR_READ(Collections.singleton(Manifest.permission.READ_CALENDAR)),
    @RequiresApi(api = 33) POST_NOTIFICATIONS(Collections.singleton(Manifest.permission.POST_NOTIFICATIONS)),
    WRITE_EXTERNAL_STORAGE(Collections.singleton(Manifest.permission.WRITE_EXTERNAL_STORAGE)),
    COARSE_LOCATION(Collections.singleton(Manifest.permission.ACCESS_COARSE_LOCATION)),
    NON_CRITICAL(
        Set.of(
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.INTERNET
        )
    );

    private final String[] permissions;

    PermissionSets(Set<String> permissions) {
      this.permissions = permissions.toArray(new String[0]);
    }

    public String[] getPermissions() {
      return permissions;
    }
  }
}

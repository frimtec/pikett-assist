package com.github.frimtec.android.pikettassist.helper;

import android.Manifest;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

import com.github.frimtec.android.pikettassist.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionHelper {

  public enum PermissionSet {
    SMS(true, Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS))), R.string.permission_sms_title, R.string.permission_sms_text),
    CONTACTS(true, Collections.singleton(Manifest.permission.READ_CONTACTS), R.string.permission_contacts_title, R.string.permission_contacts_text),
    CALENDAR(true, Collections.singleton(Manifest.permission.READ_CALENDAR), R.string.permission_calendar_title, R.string.permission_calendar_text),
    LOCATION(true, Collections.singleton(Manifest.permission.ACCESS_COARSE_LOCATION), R.string.permission_location_title, R.string.permission_location_text),
    VARIOUS(false, Collections.unmodifiableSet(new HashSet<>(Arrays.asList(Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.VIBRATE))), 0, 0);

    private final boolean sensitive;
    private final Set<String> permissions;
    private final int titleResourceId;
    private final int textResourceId;

    PermissionSet(boolean sensitive, Set<String> permissions, int titleResourceId, int textResourceId) {
      this.sensitive = sensitive;
      this.permissions = permissions;
      this.titleResourceId = titleResourceId;
      this.textResourceId = textResourceId;
    }

    public boolean isSensitive() {
      return sensitive;
    }

    public Set<String> getPermissions() {
      return permissions;
    }

    public int getTitleResourceId() {
      return titleResourceId;
    }

    public int getTextResourceId() {
      return textResourceId;
    }
  }

  public static boolean hasMissingPermissions(Context context, PermissionSet permissionSet) {
    return permissionSet.getPermissions().stream()
        .anyMatch(permission -> ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED);
  }

  public static boolean hasMissingPermissions(Context context) {
    return Stream.of(PermissionSet.values())
        .anyMatch(set -> hasMissingPermissions(context, set));
  }

}

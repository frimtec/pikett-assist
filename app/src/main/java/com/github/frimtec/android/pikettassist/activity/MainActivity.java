package com.github.frimtec.android.pikettassist.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;

import java.util.Arrays;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

  private static final String[] REQUIRED_PERMISSIONS = {
      Manifest.permission.READ_CALENDAR,
      Manifest.permission.SEND_SMS,
      Manifest.permission.RECEIVE_SMS,
      Manifest.permission.RECEIVE_BOOT_COMPLETED,
      Manifest.permission.VIBRATE,
  };

  private static final int REQUEST_CODE = 1;
  private static final String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NotificationHelper.registerChannel(this);

    setContentView(R.layout.activity_main);

    if (!Settings.canDrawOverlays(this)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, 0);
    }
    if (!Settings.canDrawOverlays(this)) {
      Log.e("MainActivity", "Missing permissions.");
      // TODO display in main activity
      return;
    }

    if (Arrays.stream(REQUIRED_PERMISSIONS).anyMatch(permission -> ActivityCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED)) {
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE);
    } else {
      startService(new Intent(this, PikettService.class));
    }

  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (Arrays.stream(grantResults).anyMatch(result -> result == PERMISSION_DENIED)) {
      Log.e("MainActivity", "Missing permissions.");
      // TODO display in main activity
      return;
    }
    startService(new Intent(this, PikettService.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.shift:
        Log.v(TAG, "Menu option SHIFT selected.");
        return true;
      case R.id.calllog:
        Log.v("MainActivity", "Menu option CALLLOG selected.");
        return true;
      case R.id.settings:
        Log.v("MainActivity", "Menu option SETTINGS selected.");
        return true;
      case R.id.about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}

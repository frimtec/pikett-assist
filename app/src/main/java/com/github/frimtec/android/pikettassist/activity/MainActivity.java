package com.github.frimtec.android.pikettassist.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.PikettState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;

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
      Manifest.permission.ACCESS_COARSE_LOCATION,
  };

  private static final int REQUEST_CODE = 1;
  private static final String TAG = "MainActivity";

  private BroadcastReceiver broadcastReceiver;
  private StateFragement stateFragement;
  private ShiftListFragement shiftListFragement;
  private CallLogFragement calLogFragement;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
    switch (item.getItemId()) {
      case R.id.navigation_home: {
        Log.v("MainActivity", "Tab selected: home");
        loadStateFragment();
        return true;
      }
      case R.id.navigation_shifts: {
        Log.v("MainActivity", "Tab selected: shifts");
        loadShiftListFragment();
        return true;
      }
      case R.id.navigation_alert_log: {
        Log.v("MainActivity", "Tab selected: alert log");
        loadCallLogFragment();
        return true;
      }
    }
    return false;
  };

  private void loadStateFragment() {
    if (stateFragement == null) {
      stateFragement = new StateFragement();
    }
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, stateFragement);
    fragmentTransaction.commit(); // save the changes
  }

  private void loadShiftListFragment() {
    if (shiftListFragement == null) {
      shiftListFragement = new ShiftListFragement();
    }
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, shiftListFragement);
    fragmentTransaction.commit(); // save the changes
  }

  private void loadCallLogFragment() {
    if (calLogFragement == null) {
      calLogFragement = new CallLogFragement();
    }
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, calLogFragement);
    fragmentTransaction.commit(); // save the changes
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    registerReceiver();
    NotificationHelper.registerChannel(this);

    setContentView(R.layout.activity_main);

    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    if (!Settings.canDrawOverlays(this)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, 0);
    }
    if (!Settings.canDrawOverlays(this)) {
      finish();
      return;
    }

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    if(!pm.isIgnoringBatteryOptimizations(getPackageName())) {
      NotificationHelper.batteryOptimization(this, (dialogInterface, integer) -> Log.i("MainActivity", "Battery optimazation dialog confirmed."));
    }

    if (Arrays.stream(REQUIRED_PERMISSIONS).anyMatch(permission -> ActivityCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED)) {
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE);
      return;
    }
    onCreateAllPermissionsGranted();
  }

  private void onCreateAllPermissionsGranted() {
    loadStateFragment();
    startService(new Intent(this, PikettService.class));
  }

  private void refresh() {
    if (stateFragement != null) {
      stateFragement.refresh();
    }
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (Arrays.stream(grantResults).anyMatch(result -> result == PERMISSION_DENIED)) {
      Log.e("MainActivity", "Missing permissions.");
      finish();
      return;
    }
    onCreateAllPermissionsGranted();
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
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      case R.id.about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void registerReceiver() {
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Event received: " + intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED) &&
            SharedState.getPikettState(context) == PikettState.ON) {
          try {
            // wait for change
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interrupt");
          }
          Log.v(TAG, "Start signal strength service as pikett state is ON");
          context.startService(new Intent(context, SignalStrengthService.class));
        }
        refresh();
      }
    };
    IntentFilter filter = new IntentFilter("com.github.frimtec.android.pikettassist.refresh");
    filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    registerReceiver(broadcastReceiver, filter);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

}

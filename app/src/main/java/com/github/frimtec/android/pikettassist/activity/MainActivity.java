package com.github.frimtec.android.pikettassist.activity;

import android.Manifest;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
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

  private BroadcastReceiver broadcastReceiver;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
    switch (item.getItemId()) {
      case R.id.navigation_home:
        Log.v("MainActivity", "Tab selected: home");
        return true;
      case R.id.navigation_shifts:
        Log.v("MainActivity", "Tab selected: shifts");
        return true;
      case R.id.navigation_alert_log:
        Log.v("MainActivity", "Tab selected: alert log");
        startActivity(new Intent(this, AlertLogActivity.class));
        return true;
    }
    return false;
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NotificationHelper.registerChannel(this);

    setContentView(R.layout.activity_main);

    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    if (Arrays.stream(REQUIRED_PERMISSIONS).anyMatch(permission -> ActivityCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED)) {
      ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE);
    } else {
      startService(new Intent(this, PikettService.class));
    }

    if (!Settings.canDrawOverlays(this)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, 0);
    }
    if (!Settings.canDrawOverlays(this)) {
      Log.e("MainActivity", "Missing permissions.");
      // TODO display in main activity
      return;
    }

    TextView textView = (TextView) findViewById(R.id.main_state);
    textView.setText(Html.fromHtml("Pikett state: " + SharedState.getPikettState(this) + "<br/>" +
        "Alarm state: " + SharedState.getAlarmState(this).first, Html.FROM_HTML_MODE_COMPACT));

    Button button = (Button) findViewById(R.id.close_alert_button);
    button.setOnClickListener(v -> {
      try (SQLiteDatabase writableDatabase = PikettAssist.getWritableDatabase()) {
        Log.v(TAG, "Close alert button pressed.");
        ContentValues values = new ContentValues();
        values.put("end_time", Instant.now().toEpochMilli());
        int update = writableDatabase.update("t_alert", values, "end_time is null", null);
        if (update != 1) {
          Log.e(TAG, "One open case expected, but got " + update);
        }
      }
      NotificationHelper.cancel(this);
      refresh();
    });
    refresh();
    registerReceiver();
  }

  private void refresh() {
    TextView textView = (TextView) findViewById(R.id.main_state);
    textView.setText(Html.fromHtml("Pikett state: " + SharedState.getPikettState(this) + "<br/>" +
        "Alarm state: " + SharedState.getAlarmState(this).first, Html.FROM_HTML_MODE_COMPACT));

    textView.invalidate();
    Button button = (Button) findViewById(R.id.close_alert_button);
    button.setEnabled(SharedState.getAlarmState(this).first != AlarmState.OFF);
    button.invalidate();
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
        Log.v(TAG, "Refresh event received.");
        refresh();
      }
    };
    registerReceiver(broadcastReceiver, new IntentFilter("com.github.frimtec.android.pikettassist.refresh"));
  }

  @Override
  protected void onStop() {
    super.onStop();
    /*
     * Step 4: Ensure to unregister the receiver when the activity is destroyed so that
     * you don't face any memory leak issues in the app
     */
    if(broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
    }
  }
}

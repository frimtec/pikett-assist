package com.github.frimtec.android.pikettassist.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private BroadcastReceiver broadcastReceiver;
  private StateFragment stateFragment;
  private ShiftListFragment shiftListFragment;
  private CallLogFragment calLogFragment;
  private Fragment activeFragment = Fragment.STATE;
  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
    switch (item.getItemId()) {
      case R.id.navigation_home: {
        loadStateFragment();
        activeFragment = Fragment.STATE;
        return true;
      }
      case R.id.navigation_shifts: {
        activeFragment = Fragment.SHIFTS;
        loadShiftListFragment();
        return true;
      }
      case R.id.navigation_alert_log: {
        activeFragment = Fragment.CALL_LOG;
        loadCallLogFragment();
        return true;
      }
    }
    return false;
  };

  private void loadStateFragment() {
    if (stateFragment == null) {
      stateFragment = new StateFragment();
    }
    activeFragment = Fragment.STATE;
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, stateFragment);
    fragmentTransaction.commit();
  }

  private void loadShiftListFragment() {
    if (shiftListFragment == null) {
      shiftListFragment = new ShiftListFragment();
    }
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, shiftListFragment);
    fragmentTransaction.commit();
  }

  private void loadCallLogFragment() {
    if (calLogFragment == null) {
      calLogFragment = new CallLogFragment();
    }
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, calLogFragment);
    fragmentTransaction.commit();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    registerReceiver();
    NotificationHelper.registerChannel(this);

    setContentView(R.layout.activity_main);

    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    loadStateFragment();
    startService(new Intent(this, PikettService.class));
  }

  private void refresh() {
    if (stateFragment != null && activeFragment == Fragment.STATE) {
      stateFragment.refresh();
    }
    if (shiftListFragment != null && activeFragment == Fragment.SHIFTS) {
      shiftListFragment.refresh();
    }
    if (calLogFragment != null && activeFragment == Fragment.CALL_LOG) {
      calLogFragment.refresh();
    }
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    refresh();
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
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction()) &&
            SharedState.getPikettState(context) == OnOffState.ON) {
          try {
            // wait for change
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected interrupt");
          }
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

  private enum Fragment {
    STATE,
    SHIFTS,
    CALL_LOG
  }

}

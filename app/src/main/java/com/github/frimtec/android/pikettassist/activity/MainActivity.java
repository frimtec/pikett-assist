package com.github.frimtec.android.pikettassist.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  private BroadcastReceiver broadcastReceiver;
  private StateFragment stateFragment;
  private ShiftListFragment shiftListFragment;
  private CallLogFragment callLogFragment;
  private TestAlarmFragment testAlarmFragment;
  private AbstractListFragment activeFragment;

  private static final Map<Fragment, Integer> FRAGMENT_BUTTON_ID_MAP;
  private static final Map<Integer, Fragment> BUTTON_ID_FRAGMENT_MAP;

  static {
    FRAGMENT_BUTTON_ID_MAP = new EnumMap<>(Fragment.class);
    FRAGMENT_BUTTON_ID_MAP.put(Fragment.STATE, R.id.navigation_home);
    FRAGMENT_BUTTON_ID_MAP.put(Fragment.SHIFTS, R.id.navigation_shifts);
    FRAGMENT_BUTTON_ID_MAP.put(Fragment.CALL_LOG, R.id.navigation_alert_log);
    FRAGMENT_BUTTON_ID_MAP.put(Fragment.TEST_ALARMS, R.id.navigation_test_alarms);

    BUTTON_ID_FRAGMENT_MAP = new HashMap<>();
    FRAGMENT_BUTTON_ID_MAP.forEach((fragment, buttonId) -> BUTTON_ID_FRAGMENT_MAP.put(buttonId, fragment));
  }

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
    Fragment fragment = BUTTON_ID_FRAGMENT_MAP.get(item.getItemId());

    if (fragment != null) {
      loadFragment(fragment);
      return true;
    }
    return false;
  };

  void switchFragment(Fragment fragment) {
    loadFragment(fragment);
    BottomNavigationView navigation = findViewById(R.id.navigation);
    //noinspection ConstantConditions
    navigation.setSelectedItemId(FRAGMENT_BUTTON_ID_MAP.get(fragment));
  }

  private void loadFragment(Fragment fragment) {
    switch (fragment) {
      case STATE:
        if (stateFragment == null) {
          stateFragment = new StateFragment();
        }
        activeFragment = stateFragment;
        break;
      case SHIFTS:
        if (shiftListFragment == null) {
          shiftListFragment = new ShiftListFragment();
        }
        activeFragment = shiftListFragment;
        break;
      case CALL_LOG:
        if (callLogFragment == null) {
          callLogFragment = new CallLogFragment();
        }
        activeFragment = callLogFragment;
        break;
      case TEST_ALARMS:
        if (testAlarmFragment == null) {
          testAlarmFragment = new TestAlarmFragment();
        }
        activeFragment = testAlarmFragment;
        break;
      default:
        throw new IllegalStateException("Unknown fragment: " + fragment);
    }
    FragmentManager fm = getFragmentManager();
    FragmentTransaction fragmentTransaction = fm.beginTransaction();
    fragmentTransaction.replace(R.id.frame_layout, activeFragment);
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

    loadFragment(Fragment.STATE);
    startService(new Intent(this, PikettService.class));
  }

  private void refresh() {
    if (activeFragment != null) {
      activeFragment.refresh();
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

  enum Fragment {
    STATE,
    SHIFTS,
    CALL_LOG,
    TEST_ALARMS
  }

}

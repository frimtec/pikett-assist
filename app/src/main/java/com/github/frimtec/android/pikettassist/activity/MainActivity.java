package com.github.frimtec.android.pikettassist.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.donation.DonationFragment;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.github.frimtec.android.pikettassist.activity.StateFragment.DIALOG_TAG;

public class MainActivity extends AppCompatActivity implements BillingProvider {

  private BroadcastReceiver broadcastReceiver;
  private StateFragment stateFragment;
  private ShiftListFragment shiftListFragment;
  private CallLogFragment callLogFragment;
  private TestAlarmFragment testAlarmFragment;
  private AbstractListFragment activeFragment;
  private SecureSmsProxyFacade s2smp;

  private BillingManager billingManager;
  private DonationFragment donationFragment;

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

  private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
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
          stateFragment.setParent(this);
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
    this.s2smp = SecureSmsProxyFacade.instance(this);
    registerReceiver();
    NotificationHelper.registerChannel(this);

    setContentView(R.layout.activity_main);

    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    loadFragment(Fragment.STATE);

    // Create and initialize BillingManager which talks to BillingLibrary
    billingManager = new BillingManager(this, stateFragment);

    startService(new Intent(this, PikettService.class));
  }

  private void refresh() {
    if (activeFragment != null) {
      activeFragment.refresh();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (activeFragment == stateFragment && billingManager != null
        && billingManager.getBillingClientResponseCode() == BillingResponseCode.OK) {
      billingManager.queryPurchases();
    }
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    refresh();
    startService(new Intent(this, PikettService.class));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1000) {
      RegistrationResult result = s2smp.getRegistrationResult(resultCode, data);
      result.getSecret().ifPresent(secret -> SharedState.setSmsAdapterSecret(this, secret));

      String[] registrationErrors = getResources().getStringArray(R.array.registration_errors);
      String registrationText = getString(R.string.sms_adapter_registration) + ": " +
          registrationErrors[result.getReturnCode().ordinal()];
      Toast.makeText(this, registrationText, Toast.LENGTH_LONG).show();
      if (result.getReturnCode().isSuccess()) {
        refresh();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    if(isDeveloperMode()) {
      menu.findItem(R.id.logcat).setVisible(true);
    }
    return super.onCreateOptionsMenu(menu);
  }

  private boolean isDeveloperMode() {
    return Settings.Secure.getInt(getApplicationContext().getContentResolver(),
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      case R.id.donate:
        showDonationDialog();
        return true;
      case R.id.logcat:
        startActivity(new Intent(this, LogcatActivity.class));
        return true;
      case R.id.about:
        Intent intent = new Intent(this, AboutActivity.class);

        List<Integer> sponsorMedals = new ArrayList<>();
        if (getBronzeSponsor() == BillingState.PURCHASED) {
          sponsorMedals.add(R.drawable.bronze_icon);
        }
        if (getSilverSponsor() == BillingState.PURCHASED) {
          sponsorMedals.add(R.drawable.silver_icon);
        }
        if (getGoldSponsor() == BillingState.PURCHASED) {
          sponsorMedals.add(R.drawable.gold_icon);
        }

        intent.putExtra(AboutActivity.EXTRA_SPONSOR_ICONS, sponsorMedals.stream().mapToInt(value -> value).toArray());
        startActivity(intent);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  void showDonationDialog() {
    if (donationFragment == null) {
      donationFragment = new DonationFragment();
    }
    if (!isAcquireFragmentShown()) {
      donationFragment.show(getSupportFragmentManager(), DIALOG_TAG);
      if (getBillingManager() != null &&
          getBillingManager().getBillingClientResponseCode() > BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
        donationFragment.onManagerReady(this);
      }
    }
  }

  public boolean isAcquireFragmentShown() {
    return donationFragment != null && donationFragment.isVisible();
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
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
    if (billingManager != null) {
      billingManager.destroy();
    }
    super.onDestroy();
  }

  enum Fragment {
    STATE,
    SHIFTS,
    CALL_LOG,
    TEST_ALARMS
  }

  @Override
  public BillingManager getBillingManager() {
    return billingManager;
  }

  @Override
  public BillingState getBronzeSponsor() {
    return stateFragment.getBronzeSponsor();
  }

  @Override
  public BillingState getSilverSponsor() {
    return stateFragment.getSilverSponsor();
  }

  @Override
  public BillingState getGoldSponsor() {
    return stateFragment.getGoldSponsor();
  }
}

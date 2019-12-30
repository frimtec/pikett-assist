package com.github.frimtec.android.pikettassist.ui;

import android.annotation.SuppressLint;
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
import com.github.frimtec.android.pikettassist.domain.Action;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.donation.DonationFragment;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.ui.about.AboutActivity;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertListFragment;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.ui.overview.StateFragment;
import com.github.frimtec.android.pikettassist.ui.preferences.PreferencesActivity;
import com.github.frimtec.android.pikettassist.ui.shifts.ShiftListFragment;
import com.github.frimtec.android.pikettassist.ui.support.LogcatActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.TestAlarmFragment;
import com.github.frimtec.android.pikettassist.utility.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.utility.NotificationHelper;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.ui.BillingAdapter.BILLING_DIALOG_TAG;

public class MainActivity extends AppCompatActivity {

  public static final String ACTIVE_FRAGMENT_STATE = "ACTIVE_FRAGMENT";
  private BroadcastReceiver broadcastReceiver;
  private StateFragment stateFragment;
  private ShiftListFragment shiftListFragment;
  private AlertListFragment alertListFragment;
  private TestAlarmFragment testAlarmFragment;
  private AbstractListFragment activeFragment;
  private SecureSmsProxyFacade s2msp;

  private DonationFragment donationFragment;
  private BillingAdapter billingAdapter;

  private static final Map<FragmentName, Integer> FRAGMENT_BUTTON_ID_MAP;

  @SuppressLint("UseSparseArrays")
  private static final Map<Integer, FragmentName> BUTTON_ID_FRAGMENT_MAP = new HashMap<>();

  static {
    FRAGMENT_BUTTON_ID_MAP = new EnumMap<>(FragmentName.class);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentName.STATE, R.id.navigation_home);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentName.SHIFTS, R.id.navigation_shifts);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentName.ALERT_LOG, R.id.navigation_alert_log);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentName.TEST_ALARMS, R.id.navigation_test_alarms);

    FRAGMENT_BUTTON_ID_MAP.forEach((fragment, buttonId) -> BUTTON_ID_FRAGMENT_MAP.put(buttonId, fragment));
  }

  private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
    FragmentName fragment = BUTTON_ID_FRAGMENT_MAP.get(item.getItemId());

    if (fragment != null) {
      loadFragment(fragment);
      return true;
    }
    return false;
  };

  public void switchFragment(FragmentName fragment) {
    loadFragment(fragment);
    BottomNavigationView navigation = findViewById(R.id.navigation);
    //noinspection ConstantConditions
    navigation.setSelectedItemId(FRAGMENT_BUTTON_ID_MAP.get(fragment));
  }

  private void loadFragment(FragmentName fragment) {
    switch (fragment) {
      case STATE:
        if (stateFragment == null) {
          stateFragment = new StateFragment();
          stateFragment.setActivityFacade(this, new StateFragment.BillingAccess() {
            @Override
            public List<BillingProvider.BillingState> getProducts() {
              return MainActivity.this.billingAdapter.getAllProducts();
            }

            @Override
            public void showDonationDialog() {
              MainActivity.this.showDonationDialog();
            }
          });
        }
        activeFragment = stateFragment;
        break;
      case SHIFTS:
        if (shiftListFragment == null) {
          shiftListFragment = new ShiftListFragment();
        }
        activeFragment = shiftListFragment;
        break;
      case ALERT_LOG:
        if (alertListFragment == null) {
          alertListFragment = new AlertListFragment();
        }
        activeFragment = alertListFragment;
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
    this.s2msp = SecureSmsProxyFacade.instance(this);
    registerReceiver();
    NotificationHelper.registerChannel(this);

    setContentView(R.layout.activity_main);

    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    this.billingAdapter = new BillingAdapter(this);

    FragmentName savedFragmentName = FragmentName.STATE;
    if (savedInstanceState != null) {
      savedFragmentName = FragmentName.valueOf(savedInstanceState.getString(ACTIVE_FRAGMENT_STATE, savedFragmentName.name()));
    }
    loadFragment(savedFragmentName);

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
    BillingManager billingManager = this.billingAdapter.getBillingManager();
    if (billingManager != null && billingManager.getBillingClientResponseCode() == BillingResponseCode.OK) {
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
      RegistrationResult result = s2msp.getRegistrationResult(resultCode, data);
      result.getSecret().ifPresent(secret -> SharedState.setSmsAdapterSecret(this, secret));

      String[] registrationErrors = getResources().getStringArray(R.array.registration_errors);
      String registrationText = getString(R.string.sms_adapter_registration) + ": " +
          registrationErrors[result.getReturnCode().ordinal()];
      Toast.makeText(this, registrationText, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    if (isDeveloperMode()) {
      menu.findItem(R.id.logcat).setVisible(true);
    }
    return super.onCreateOptionsMenu(menu);
  }

  private boolean isDeveloperMode() {
    return Settings.Global.getInt(getApplicationContext().getContentResolver(),
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.settings:
        startActivity(new Intent(this, PreferencesActivity.class));
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
        if (billingAdapter.getBronzeSponsor() == PURCHASED) {
          sponsorMedals.add(R.drawable.bronze_icon);
        }
        if (billingAdapter.getSilverSponsor() == PURCHASED) {
          sponsorMedals.add(R.drawable.silver_icon);
        }
        if (billingAdapter.getGoldSponsor() == PURCHASED) {
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
      donationFragment.show(getSupportFragmentManager(), BILLING_DIALOG_TAG);

      if (billingAdapter != null &&
          this.billingAdapter.getBillingManager().getBillingClientResponseCode() > BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
        donationFragment.onManagerReady(this.billingAdapter);
      }
    }
  }

  private boolean isAcquireFragmentShown() {
    return donationFragment != null && donationFragment.isVisible();
  }

  private void registerReceiver() {
    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction()) &&
            CalendarEventHelper.getPikettState(context) == OnOffState.ON) {
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
    IntentFilter filter = new IntentFilter(Action.REFRESH.getId());
    filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    registerReceiver(broadcastReceiver, filter);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ACTIVE_FRAGMENT_STATE, this.activeFragment.getFragmentName().name());
  }

  @Override
  protected void onDestroy() {
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
    if (billingAdapter != null) {
      billingAdapter.destroy();
    }
    super.onDestroy();
  }
}

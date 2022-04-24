package com.github.frimtec.android.pikettassist.ui;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.service.system.Feature.RequestCodes.FROM_BATTERY_OPTIMIZATION_REQUEST_CODE;
import static com.github.frimtec.android.pikettassist.ui.BillingAdapter.BILLING_DIALOG_TAG;
import static com.github.frimtec.android.pikettassist.ui.overview.StateFragment.REGISTER_SMS_ADAPTER_REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.BillingClient;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.donation.DonationFragment;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;
import com.github.frimtec.android.pikettassist.service.LowSignalService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.ShiftService;
import com.github.frimtec.android.pikettassist.service.SmsListener;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.pikettassist.ui.about.AboutActivity;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertListFragment;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.pikettassist.ui.common.ViewPager2Helper;
import com.github.frimtec.android.pikettassist.ui.overview.StateFragment;
import com.github.frimtec.android.pikettassist.ui.settings.SettingsActivity;
import com.github.frimtec.android.pikettassist.ui.shifts.ShiftListFragment;
import com.github.frimtec.android.pikettassist.ui.support.LogcatActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.TestAlarmFragment;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.RegistrationResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final String ACTIVE_FRAGMENT_STATE = "ACTIVE_FRAGMENT";

  class SwipeFragmentStateAdapter extends FragmentStateAdapter {

    private final FragmentActivity fragmentActivity;

    public SwipeFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
      super(fragmentActivity);
      this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      FragmentPosition fragmentPosition = ensureValidFragmentPosition(position);
      switch (fragmentPosition) {
        case STATE:
          StateFragment stateFragment = new StateFragment();
          stateFragment.setActivityFacade(MainActivity.this, new StateFragment.BillingAccess() {
            @Override
            public List<BillingProvider.BillingState> getProducts() {
              return MainActivity.this.billingAdapter.getAllProducts();
            }

            @Override
            public void showDonationDialog() {
              MainActivity.this.showDonationDialog();
            }
          });
          return stateFragment;
        case SHIFTS:
          return new ShiftListFragment();
        case ALERT_LOG:
          return new AlertListFragment();
        case TEST_ALARMS:
          return new TestAlarmFragment();
      }
      throw new IllegalStateException("Unknown fragment: " + fragmentPosition);
    }

    @Override
    public int getItemCount() {
      return ApplicationPreferences.instance().getTestAlarmEnabled(fragmentActivity.getApplicationContext()) ? 4 : 3;
    }
  }

  private ViewPager2 viewPager;

  private BroadcastReceiver broadcastReceiver;
  private ViewPager2.OnPageChangeCallback pageChangeCallback;
  private SecureSmsProxyFacade s2msp;

  private DonationFragment donationFragment;
  private BillingAdapter billingAdapter;

  private static final Map<FragmentPosition, Integer> FRAGMENT_BUTTON_ID_MAP;

  @SuppressLint("UseSparseArrays")
  private static final Map<Integer, FragmentPosition> BUTTON_ID_FRAGMENT_MAP = new HashMap<>();

  static {
    FRAGMENT_BUTTON_ID_MAP = new EnumMap<>(FragmentPosition.class);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentPosition.STATE, R.id.navigation_home);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentPosition.SHIFTS, R.id.navigation_shifts);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentPosition.ALERT_LOG, R.id.navigation_alert_log);
    FRAGMENT_BUTTON_ID_MAP.put(FragmentPosition.TEST_ALARMS, R.id.navigation_test_alarms);

    FRAGMENT_BUTTON_ID_MAP.forEach((fragment, buttonId) -> BUTTON_ID_FRAGMENT_MAP.put(buttonId, fragment));
  }

  private void loadFragment(FragmentPosition fragmentPosition) {
    viewPager.setCurrentItem(fragmentPosition.ordinal(), false);
  }

  @NonNull
  private static FragmentPosition ensureValidFragmentPosition(int fragmentPosition) {
    if (fragmentPosition >= FragmentPosition.values().length) {
      throw new IllegalStateException("Unknown fragment position: " + fragmentPosition);
    }
    return FragmentPosition.values()[fragmentPosition];
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.s2msp = SecureSmsProxyFacade.instance(this);
    registerBroadcastReceiver();

    new NotificationService(this).registerChannel();

    setContentView(R.layout.activity_main);

    viewPager = findViewById(R.id.view_pager);
    FragmentStateAdapter pagerAdapter = new SwipeFragmentStateAdapter(this);
    viewPager.setAdapter(pagerAdapter);
    ViewPager2Helper.reduceDragSensitivity(viewPager, 8);
    registerPageChangeCallback(viewPager);
    BottomNavigationView navigation = findViewById(R.id.navigation);
    navigation.setOnItemSelectedListener(item -> {
      FragmentPosition fragment = BUTTON_ID_FRAGMENT_MAP.get(item.getItemId());
      if (fragment != null) {
        loadFragment(fragment);
        return true;
      }
      return false;
    });

    this.billingAdapter = new BillingAdapter(this);

    FragmentPosition savedFragmentPosition = FragmentPosition.STATE;
    if (savedInstanceState != null) {
      savedFragmentPosition = ensureValidFragmentPosition(savedInstanceState.getInt(ACTIVE_FRAGMENT_STATE, savedFragmentPosition.ordinal()));
    } else {
      // register on new app start only, not on orientation change
      registerOnSmsAdapter();
    }
    loadFragment(savedFragmentPosition);
    updateBottomNavigation();
    PikettService.enqueueWork(this);
  }

  private void registerOnSmsAdapter() {
    Installation installation = this.s2msp.getInstallation();
    if (installation.getAppVersion().orElse("0").compareTo("1.3.5") >= 0) {
      this.s2msp.register(this, REGISTER_SMS_ADAPTER_REQUEST_CODE, SmsListener.class);
    } else {
      Log.w(TAG, "Silent registration not supported by S2MSP Version: " + installation.getAppVersion().orElse("N/A"));
    }
  }

  private void refresh() {
    FragmentManager fm = getSupportFragmentManager();
    var activeFragment = (AbstractListFragment<?>) fm.findFragmentByTag("f" + viewPager.getCurrentItem());
    if (activeFragment != null) {
      activeFragment.refresh();
    }
  }

  private void updateBottomNavigation() {
    BottomNavigationView navigation = findViewById(R.id.navigation);
    MenuItem item = navigation.getMenu().findItem(R.id.navigation_test_alarms);
    item.setVisible(ApplicationPreferences.instance().getTestAlarmEnabled(this));
  }

  @Override
  protected void onPause() {
    unregisterBroadcastReceiver();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerBroadcastReceiver();
    updateBottomNavigation();
    BillingManager billingManager = this.billingAdapter.getBillingManager();
    if (billingManager != null && billingManager.getBillingClientResponseCode() == BillingResponseCode.OK) {
      billingManager.queryPurchases();
    }
    refresh();
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    refresh();
    PikettService.enqueueWork(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REGISTER_SMS_ADAPTER_REQUEST_CODE) {
      Log.d(TAG, "SMS adapter register result received.");
      RegistrationResult result = s2msp.getRegistrationResult(resultCode, data);
      result.getSecret().ifPresent(secret -> {
        if (!secret.equals(ApplicationState.instance().getSmsAdapterSecret())) {
          Log.i(TAG, "SMS adapter secret changed.");
          ApplicationState.instance().setSmsAdapterSecret(secret);
        }
      });
      if (!result.getReturnCode().isSuccess()) {
        String[] registrationErrors = getResources().getStringArray(R.array.registration_errors);
        String registrationText = getString(R.string.sms_adapter_registration) + ": " +
            registrationErrors[result.getReturnCode().ordinal()];
        Toast.makeText(this, registrationText, Toast.LENGTH_LONG).show();
      }
    } else if (requestCode == FROM_BATTERY_OPTIMIZATION_REQUEST_CODE) {
      Log.i(TAG, "Return from grant battery optimization deactivation activity; result=" + resultCode);
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
    int itemId = item.getItemId();
    if (itemId == R.id.settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    } else if (itemId == R.id.donate) {
      showDonationDialog();
      return true;
    } else if (itemId == R.id.logcat) {
      startActivity(new Intent(this, LogcatActivity.class));
      return true;
    } else if (itemId == R.id.about) {
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

  private void showDonationDialog() {
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

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(ACTIVE_FRAGMENT_STATE, this.viewPager.getCurrentItem());
  }

  @Override
  protected void onDestroy() {
    unregisterBroadcastReceiver();
    unregisterPageChangeCallback(this.viewPager);
    if (billingAdapter != null) {
      billingAdapter.destroy();
    }
    super.onDestroy();
  }

  private void registerBroadcastReceiver() {
    if (broadcastReceiver == null) {
      broadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
          if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction()) &&
              new ShiftService(context).getState() == OnOffState.ON) {
            LowSignalService.enqueueWork(context);
          }
          refresh();
        }
      };
      IntentFilter filter = new IntentFilter(Action.REFRESH.getId());
      filter.addAction(Intent.ACTION_BATTERY_CHANGED);
      filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
      filter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
      registerReceiver(broadcastReceiver, filter);
    }
  }

  private void unregisterBroadcastReceiver() {
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

  private void registerPageChangeCallback(ViewPager2 viewPager) {
    if (pageChangeCallback == null) {
      pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
          super.onPageSelected(position);
          BottomNavigationView navigation = findViewById(R.id.navigation);
          Integer itemId = FRAGMENT_BUTTON_ID_MAP.get(ensureValidFragmentPosition(position));
          if (itemId != null) {
            navigation.setSelectedItemId(itemId);
          }
        }
      };
      viewPager.registerOnPageChangeCallback(pageChangeCallback);
    }
  }

  private void unregisterPageChangeCallback(ViewPager2 viewPager) {
    if (pageChangeCallback != null) {
      viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
      pageChangeCallback = null;
    }
  }
}

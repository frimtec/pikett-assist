package com.github.frimtec.android.pikettassist.ui;

import static com.android.billingclient.api.BillingClient.BillingResponseCode;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.ui.BillingAdapter.BILLING_DIALOG_TAG;
import static com.github.frimtec.android.pikettassist.ui.overview.StateFragment.REGISTER_SMS_ADAPTER_REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.donation.DonationFragment;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;
import com.github.frimtec.android.pikettassist.service.LowSignalWorker;
import com.github.frimtec.android.pikettassist.service.PikettWorker;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private static final String ACTIVE_FRAGMENT_STATE = "ACTIVE_FRAGMENT";
  private static final int[] TAB_ICONS = new int[]{
      R.drawable.ic_home_24dp,
      R.drawable.ic_date_range_24dp,
      R.drawable.ic_siren,
      R.drawable.ic_test_alarm
  };

  private static final int[] TAB_DESCRIPTION = new int[]{
      R.string.title_home,
      R.string.title_shift_overview,
      R.string.title_alert_log,
      R.string.title_test_alarms
  };

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
        case STATE -> {
          StateFragment stateFragment = new StateFragment();
          stateFragment.setActivityFacade(new StateFragment.BillingAccess() {
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
        }
        case SHIFTS -> {
          return new ShiftListFragment();
        }
        case ALERT_LOG -> {
          return new AlertListFragment();
        }
        case TEST_ALARMS -> {
          return new TestAlarmFragment();
        }
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
  private SecureSmsProxyFacade s2msp;

  private DonationFragment donationFragment;
  private BillingAdapter billingAdapter;

  private TabLayoutMediator tabLayoutMediator;

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
    AppCompatDelegate.setDefaultNightMode(ApplicationPreferences.instance().getAppTheme(this));

    this.s2msp = SecureSmsProxyFacade.instance(this);
    registerBroadcastReceiver();

    new NotificationService(this).registerChannel();

    setContentView(R.layout.activity_main);

    viewPager = findViewById(R.id.view_pager);
    viewPager.setAdapter(new SwipeFragmentStateAdapter(this));
    ViewPager2Helper.reduceDragSensitivity(viewPager, 8);

    TabLayout tabLayout = findViewById(R.id.tab_layout);
    tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
      tab.setIcon(TAB_ICONS[position]);
      tab.setContentDescription(TAB_DESCRIPTION[position]);
    });
    tabLayoutMediator.attach();

    this.billingAdapter = new BillingAdapter(this);

    FragmentPosition savedFragmentPosition = Arrays.stream(FragmentPosition.values())
        .map(Enum::name)
        .filter(value -> value.equals(getIntent().getAction()))
        .findFirst()
        .map(FragmentPosition::valueOf)
        .orElse(FragmentPosition.STATE);

    if (savedInstanceState != null) {
      savedFragmentPosition = ensureValidFragmentPosition(savedInstanceState.getInt(ACTIVE_FRAGMENT_STATE, savedFragmentPosition.ordinal()));
    } else {
      // register on new app start only, not on orientation change
      registerOnSmsAdapter();
    }
    loadFragment(savedFragmentPosition);
    PikettWorker.enqueueWork(this);
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
    refreshTabLabels();
    FragmentManager fm = getSupportFragmentManager();
    var activeFragment = (AbstractListFragment<?>) fm.findFragmentByTag("f" + viewPager.getCurrentItem());
    if (activeFragment != null) {
      activeFragment.refresh();
    }
  }

  private void refreshTabLabels() {
    if (tabLayoutMediator != null) {
      tabLayoutMediator.detach();
      tabLayoutMediator.attach();
    }
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
    BillingManager billingManager = this.billingAdapter.getBillingManager();
    if (billingManager != null && billingManager.getBillingClientResponseCode() == BillingResponseCode.OK) {
      billingManager.queryPurchases();
    }
    refresh();
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    refresh();
    PikettWorker.enqueueWork(this);
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

      if (billingAdapter != null) {
        int billingClientResponseCode = this.billingAdapter.getBillingManager().getBillingClientResponseCode();
        switch (billingClientResponseCode) {
          case BillingResponseCode.BILLING_UNAVAILABLE -> donationFragment.onManagerReady(null);
          case BillingResponseCode.OK -> donationFragment.onManagerReady(this.billingAdapter);
          default ->
              Log.w(TAG, "Unhandled billing client response code: " + billingClientResponseCode);
        }
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
              new ShiftService(context).getShiftState().isOn()) {
            LowSignalWorker.enqueueWork(context);
          }
          refresh();
        }
      };
      IntentFilter filter = new IntentFilter(Action.REFRESH.getId());
      filter.addAction(Intent.ACTION_BATTERY_CHANGED);
      filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
      filter.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);
      } else {
        registerReceiver(broadcastReceiver, filter);
      }
    }
  }

  private void unregisterBroadcastReceiver() {
    if (broadcastReceiver != null) {
      unregisterReceiver(broadcastReceiver);
      broadcastReceiver = null;
    }
  }

}

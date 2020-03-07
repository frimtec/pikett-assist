package com.github.frimtec.android.pikettassist.ui.overview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.service.OperationsCenterContactService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.service.ShiftService;
import com.github.frimtec.android.pikettassist.service.SmsListener;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.Feature;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.pikettassist.ui.FragmentName;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_LOADED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.service.system.Feature.RequestCodes.FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE;
import static com.github.frimtec.android.pikettassist.service.system.Feature.SETTING_BATTERY_OPTIMIZATION_OFF;
import static com.github.frimtec.android.pikettassist.service.system.Feature.SETTING_DRAW_OVERLAYS;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.siFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

public class StateFragment extends AbstractListFragment<State> {

  public static final int REGISTER_SMS_ADAPTER_REQUEST_CODE = 1000;

  public interface BillingAccess {

    List<BillingState> getProducts();

    void showDonationDialog();
  }

  private static final String DATE_TIME_FORMAT = "dd.MM.yy\nHH:mm:ss";
  private static final String TAG = "StateFragment";

  static final int REQUEST_CODE_SELECT_PHONE_NUMBER = 111;

  private final Random random = new Random(System.currentTimeMillis());

  private AlertService alertService;
  private SecureSmsProxyFacade s2msp;
  private Activity activity;
  private BillingAccess billingAccess;

  private SignalStrengthService signalStrengthService;
  private final AlertDao alertDao;
  private final TestAlarmDao testAlarmDao;
  private OperationsCenterContactService operationsCenterContactService;

  public StateFragment() {
    this(new AlertDao(), new TestAlarmDao());
  }

  @SuppressLint("ValidFragment")
  StateFragment(AlertDao alertDao, TestAlarmDao testAlarmDao) {
    super(FragmentName.STATE);
    this.alertDao = alertDao;
    this.testAlarmDao = testAlarmDao;
  }

  public void setActivityFacade(Activity parent, BillingAccess billingAccess) {
    this.activity = parent;
    this.billingAccess = billingAccess;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.alertService = new AlertService(getContext());
    this.s2msp = SecureSmsProxyFacade.instance(getContext());
    this.signalStrengthService = new SignalStrengthService(getContext());
    this.operationsCenterContactService = new OperationsCenterContactService(getContext());
  }

  @Override
  public void onResume() {
    // the configured subscription may have been changed
    this.signalStrengthService = new SignalStrengthService(this.getContext());
    super.onResume();
  }

  @Override
  protected void configureListView(ListView listView) {
    listView.setClickable(true);
    listView.setOnItemClickListener((parent, view1, position, id) -> {
      State selectedState = (State) listView.getItemAtPosition(position);
      selectedState.onClickAction(getContext());
    });
    registerForContextMenu(listView);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == FROM_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
      if (SETTING_DRAW_OVERLAYS.isAllowed(getContext())) {
        getContext().startService(new Intent(getContext(), PikettService.class));
      }
    } else if (requestCode == REQUEST_CODE_SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
      Contact contact = this.operationsCenterContactService.getContactFromUri(data.getData());
      ApplicationPreferences.setOperationsCenterContactReference(getContext(), contact.getReference());
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  protected ArrayAdapter<State> createAdapter() {
    List<State> states = new LinkedList<>();
    Optional<Feature> missingPermission = Arrays.stream(Feature.values())
        .filter(Feature::isPermissionType)
        .filter(set -> !set.isAllowed(getContext()))
        .findFirst();

    boolean missingPermissions = missingPermission.isPresent();
    if (missingPermissions) {
      Feature permission = missingPermission.get();
      if (permission.isSensitive()) {
        states.add(new State(R.drawable.ic_warning_black_24dp, getString(R.string.state_fragment_permissions), getString(permission.getNameResourceId()), null, RED) {
          @Override
          public void onClickAction(Context context) {
            permission.request(context, StateFragment.this);
          }
        });
      } else {
        permission.request(getContext(), this);
      }
    }

    boolean canDrawOverlays = SETTING_DRAW_OVERLAYS.isAllowed(getContext());
    if (!canDrawOverlays) {
      states.add(new State(R.drawable.ic_settings_black_24dp, getString(R.string.state_fragment_draw_overlays), getString(R.string.state_off), null, RED) {
        @Override
        public void onClickAction(Context context) {
          SETTING_DRAW_OVERLAYS.request(context, StateFragment.this);
        }
      });
    }

    if (!SETTING_BATTERY_OPTIMIZATION_OFF.isAllowed(getContext())) {
      states.add(new State(R.drawable.ic_battery_alert_black_24dp, getString(R.string.state_fragment_battery_optimization), getString(R.string.state_on), null, YELLOW) {
        @Override
        public void onClickAction(Context context) {
          SETTING_BATTERY_OPTIMIZATION_OFF.request(context, StateFragment.this);
        }
      });
    }

    if (!missingPermissions && canDrawOverlays) {
      regularStates(states);
    }
    return new StateArrayAdapter(getContext(), new ArrayList<>(states));
  }

  private void regularStates(List<State> states) {
    Installation smsAdapterInstallation = this.s2msp.getInstallation();
    Contact operationsCenterContact = this.operationsCenterContactService.getOperationsCenterContact();
    Set<String> operationsCenterPhoneNumbers = this.operationsCenterContactService.getPhoneNumbers(operationsCenterContact);
    ShiftService shiftService = new ShiftService(getContext());
    boolean pikettStateManuallyOn = ApplicationState.getPikettStateManuallyOn();
    OnOffState pikettState = shiftService.getState();
    Instant now = Shift.now();
    Duration prePostRunTime = ApplicationPreferences.getPrePostRunTime(getContext());
    StateContext stateContext = new StateContext(
        getContext(),
        this::startActivityForResult,
        this::refresh,
        () -> this.s2msp.register(activity, REGISTER_SMS_ADAPTER_REQUEST_CODE, operationsCenterPhoneNumbers, SmsListener.class),
        () -> this.s2msp.sendSms(new Sms(SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK, ":-)"), ApplicationState.getSmsAdapterSecret()),
        () -> this.alertService.confirmAlert(),
        () -> this.alertService.closeAlert(),
        () -> this.billingAccess.showDonationDialog(),
        smsAdapterInstallation,
        !smsAdapterInstallation.getAppVersion().isPresent(),
        smsAdapterInstallation.getAppVersion().isPresent() && smsAdapterInstallation.getApiVersion().compareTo(smsAdapterInstallation.getAppVersion().get()) > 0,
        s2msp.areSmsPermissionsGranted(),
        pikettState,
        shiftService.findCurrentOrNextShift(now).map(shift -> toDuration(pikettStateManuallyOn, pikettState, shift, now, prePostRunTime)).orElse(""),
        this.alertDao.getAlertState(),
        pikettStateManuallyOn,
        !(operationsCenterPhoneNumbers.isEmpty() || s2msp.isAllowed(operationsCenterPhoneNumbers)),
        this.signalStrengthService.getSignalStrength(),
        ApplicationPreferences.getSuperviseSignalStrength(getContext()),
        this.signalStrengthService.getNetworkOperatorName(),
        operationsCenterContact,
        operationsCenterPhoneNumbers
    );
    states.add(new SmsAdapterState(stateContext));
    if(pikettState == OnOffState.ON && new NotificationService(getContext()).isDoNotDisturbEnabled()) {
      states.add(new DoNotDisturbState(stateContext));
    }
    states.addAll(Arrays.asList(
        new OperationsCenterState(stateContext),
        new OnCallState(stateContext),
        new AlarmState(stateContext),
        new SignalStrengthState(stateContext)
    ));
    if (ApplicationPreferences.getTestAlarmEnabled(getContext())) {
      ApplicationPreferences.getSupervisedTestAlarms(getContext())
          .forEach(testAlarmContext -> states.add(new TestAlarmState(
              this.testAlarmDao.loadDetails(testAlarmContext)
                  .map(details -> new TestAlarmStateContext(stateContext, testAlarmContext, formatDateTime(details.getReceivedTime()), details.getAlertState()))
                  .orElse(new TestAlarmStateContext(stateContext, testAlarmContext, getString(R.string.state_fragment_test_alarm_never_received), OnOffState.OFF))
          )));
    }

    if (billingAccess != null) {
      List<BillingState> products = billingAccess.getProducts();
      if (products.stream().allMatch(billing -> billing != NOT_LOADED) &&
          products.stream().noneMatch(billing -> billing == PURCHASED) &&
          randomizedOn()) {
        DonationState donationState = new DonationState(stateContext);
        states.add(this.random.nextInt(states.size() + 1), donationState);
      }
    }
  }

  private String toDuration(boolean pikettStateManuallyOn, OnOffState pikettState, Shift currentOrNextShift, Instant now, Duration prePostRunTime) {
    return pikettStateManuallyOn ? "" : String.format("(%s)",
        toDurationString(Duration.between(now, pikettState == OnOffState.ON ? currentOrNextShift.getEndTime(prePostRunTime) : currentOrNextShift.getStartTime(prePostRunTime)), siFormatter()));
  }

  private boolean randomizedOn() {
    long installationAgeInDays = Integer.MAX_VALUE;
    try {
      PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
      installationAgeInDays = Duration.between(Instant.ofEpochMilli(packageInfo.firstInstallTime), Instant.now()).toDays();
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Can not get package info", e);
    }
    return this.random.nextFloat() <= Math.min((installationAgeInDays - 30f) * 0.01f, 0.3f);
  }

  private String formatDateTime(Instant time) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(StateFragment.DATE_TIME_FORMAT, Locale.getDefault())) : "";
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    State selectedItem = (State) getListView().getItemAtPosition(info.position);
    selectedItem.onCreateContextMenu(getContext(), menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ListView listView = getListView();
    State selectedItem = (State) listView.getItemAtPosition(info.position);
    boolean selected = selectedItem.onContextItemSelected(getContext(), item);
    if (selected) {
      return true;
    } else {
      return super.onContextItemSelected(item);
    }
  }

}

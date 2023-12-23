package com.github.frimtec.android.pikettassist.ui.overview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.*;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState;
import com.github.frimtec.android.pikettassist.service.*;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.BatteryService;
import com.github.frimtec.android.pikettassist.service.system.Feature;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.pikettassist.ui.FragmentPosition;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter;
import com.github.frimtec.android.pikettassist.ui.common.AbstractListFragment;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static android.app.Activity.RESULT_OK;
import static android.widget.ExpandableListView.getPackedPositionGroup;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_LOADED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.service.system.Feature.*;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.UnitNameProvider.siFormatter;
import static com.github.frimtec.android.pikettassist.ui.common.DurationFormatter.toDurationString;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

public class StateFragment extends AbstractListFragment<State, State> {

  public static final int REGISTER_SMS_ADAPTER_REQUEST_CODE = 1000;
  private static final int NOT_FOUND = -1;

  public interface BillingAccess {

    List<BillingState> getProducts();

    void showDonationDialog();
  }

  private static final String DATE_TIME_FORMAT = "dd.MM.yy\nHH:mm:ss";
  private static final String TAG = "StateFragment";

  private final Random random = new Random(System.currentTimeMillis());

  private AlertService alertService;
  private SecureSmsProxyFacade s2msp;
  private BillingAccess billingAccess;

  private SignalStrengthService signalStrengthService;
  private final AlertDao alertDao;
  private final TestAlarmDao testAlarmDao;
  private OperationsCenterContactService operationsCenterContactService;
  private ContactPersonService contactPersonService;

  ActivityResultLauncher<Intent> phoneNumberSelectionLauncher;

  public StateFragment() {
    this(new AlertDao(), new TestAlarmDao());
  }

  @SuppressLint("ValidFragment")
  StateFragment(AlertDao alertDao, TestAlarmDao testAlarmDao) {
    super(FragmentPosition.STATE);
    this.alertDao = alertDao;
    this.testAlarmDao = testAlarmDao;
  }

  public void setActivityFacade(BillingAccess billingAccess) {
    this.billingAccess = billingAccess;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Context context = requireContext();
    this.alertService = new AlertService(context);
    this.s2msp = SecureSmsProxyFacade.instance(context);
    this.signalStrengthService = new SignalStrengthService(context);
    this.operationsCenterContactService = new OperationsCenterContactService(context);
    this.contactPersonService = new ContactPersonService(context);

    Arrays.stream(Feature.values()).forEach(feature -> feature.registerFragment(this));
    phoneNumberSelectionLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              Contact contact = this.operationsCenterContactService.getContactFromUri(data.getData());
              ApplicationPreferences.instance().setOperationsCenterContactReference(context, contact.reference());
            }
          }
        }
    );
  }

  @Override
  public void onResume() {
    // the configured subscription may have been changed
    Context context = requireContext();
    this.signalStrengthService = new SignalStrengthService(context);
    super.onResume();
  }

  @Override
  protected void configureListView(ExpandableListView listView) {
    listView.setClickable(true);
    listView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
      State selectedState = getGroup(groupPosition).key();
      if (!selectedState.getChildStates().isEmpty()) {
        return false;
      }
      selectedState.onClickAction(getContext());
      return true;
    });
    listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
      State selectedState = getGroup(groupPosition).items().get(childPosition);
      selectedState.onClickAction(getContext());
      return true;
    });
    registerForContextMenu(listView);
    listView.setGroupIndicator(null);
  }

  @Override
  protected void changeExpandedGroupsPreferences(Function<Set<State>, Set<State>> transformer) {
    Set<State> states = new HashSet<>();
    transformer.apply(states);
    ApplicationPreferences.instance().setTestAlarmStatesExpanded(getContext(), !states.isEmpty());
  }

  @Override
  protected Set<Integer> getExpandedGroups() {
    if (!ApplicationPreferences.instance().isTestAlarmStatesExpanded(getContext())) {
      return Collections.emptySet();
    }

    int pos = IntStream.range(0, getGroupCount())
        .boxed()
        .filter(i -> {
          State group = getGroup(i).key();
          return group.getClass().equals(TestAlarmState.class) && !group.getChildStates().isEmpty();
        }).findFirst().orElse(NOT_FOUND);
    if (pos == NOT_FOUND) {
      return Collections.emptySet();
    }
    return Set.of(pos);
  }

  protected AbstractExpandableListAdapter<State, State> createAdapter() {
    List<State> states = new LinkedList<>();
    Optional<Feature> missingPermission = Arrays.stream(Feature.values())
        .filter(Feature::isPermissionType)
        .filter(set -> !set.isAllowed(getContext()))
        .findFirst();

    boolean missingPermissions = missingPermission.isPresent();
    if (missingPermissions) {
      Feature permission = missingPermission.get();
      if (permission.isSensitive()) {
        states.add(new State(R.drawable.ic_warning_24dp, getString(R.string.state_fragment_permissions), getString(permission.getNameResourceId()), null, RED) {
          @Override
          public void onClickAction(Context context) {
            permission.request(context);
          }
        });
      } else {
        permission.request(getContext());
      }
    }

    boolean canDrawOverlays = SETTING_DRAW_OVERLAYS.isAllowed(getContext());
    if (!canDrawOverlays) {
      states.add(new State(R.drawable.ic_settings_24dp, getString(R.string.state_fragment_draw_overlays), getString(R.string.state_off), null, RED) {
        @Override
        public void onClickAction(Context context) {
          SETTING_DRAW_OVERLAYS.request(context);
        }
      });
    }

    boolean canScheduleExactAlarms = SETTING_SCHEDULE_EXACT_ALARM.isAllowed(getContext());
    if (!canScheduleExactAlarms) {
      states.add(new State(R.drawable.ic_baseline_access_alarm_24, getString(R.string.state_fragment_schedule_exact_alarm), getString(R.string.state_off), null, RED) {
        @Override
        public void onClickAction(Context context) {
          SETTING_SCHEDULE_EXACT_ALARM.request(context);
        }
      });
    }

    if (!SETTING_BATTERY_OPTIMIZATION_OFF.isAllowed(getContext())) {
      states.add(new State(R.drawable.ic_battery_alert_black_24dp, getString(R.string.state_fragment_battery_optimization), getString(R.string.state_on), null, YELLOW) {
        @Override
        public void onClickAction(Context context) {
          SETTING_BATTERY_OPTIMIZATION_OFF.request(context);
        }
      });
    }

    if (!missingPermissions && canDrawOverlays && canScheduleExactAlarms) {
      regularStates(states);
    }
    return new StateExpandableListAdapter(getContext(), new ArrayList<>(states));
  }

  private void regularStates(List<State> states) {
    Installation smsAdapterInstallation = this.s2msp.getInstallation();
    Contact operationsCenterContact = this.operationsCenterContactService.getOperationsCenterContact();
    Set<String> operationsCenterPhoneNumbers = this.operationsCenterContactService.getPhoneNumbers(operationsCenterContact);
    ShiftService shiftService = new ShiftService(getContext());
    BatteryService batteryService = new BatteryService(getContext());
    NotificationService notificationService = new NotificationService(getContext());
    boolean pikettStateManuallyOn = ApplicationState.instance().getPikettStateManuallyOn();
    ShiftState shiftState = shiftService.getShiftState();
    Instant now = Shift.now();
    Duration prePostRunTime = ApplicationPreferences.instance().getPrePostRunTime(getContext());
    Optional<Shift> currentOrNextShift = shiftService.findCurrentOrNextShift(now);
    StateContext stateContext = new StateContext(
        this,
        getContext(),
        this::refresh,
        () -> this.s2msp.register(getActivity(), REGISTER_SMS_ADAPTER_REQUEST_CODE, operationsCenterPhoneNumbers, SmsListener.class),
        () -> this.s2msp.sendSms(new Sms(SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK, ":-)"), ApplicationState.instance().getSmsAdapterSecret()),
        () -> this.alertService.confirmAlert(),
        () -> this.alertService.closeAlert(),
        () -> this.billingAccess.showDonationDialog(),
        smsAdapterInstallation,
        s2msp.areSmsPermissionsGranted(),
        shiftState,
        currentOrNextShift.map(shift -> toDuration(pikettStateManuallyOn, shiftState, shift, now, prePostRunTime)).orElse(""),
        this.alertDao.getAlertState(),
        pikettStateManuallyOn,
        !(operationsCenterPhoneNumbers.isEmpty() || s2msp.isAllowed(operationsCenterPhoneNumbers)),
        this.signalStrengthService.getSignalStrength(),
        ApplicationPreferences.instance().getSuperviseSignalStrength(getContext()),
        this.signalStrengthService.getNetworkOperatorName(),
        operationsCenterContact,
        operationsCenterPhoneNumbers,
        batteryService.batteryStatus()
    );
    states.add(new SmsAdapterState(stateContext));
    if (notificationService.isDoNotDisturbEnabled()) {
      states.add(new DoNotDisturbState(stateContext));
    }
    states.add(new OperationsCenterState(stateContext, phoneNumberSelectionLauncher));
    Optional<List<String>> partners = currentOrNextShift.map(Shift::getPartners);
    if (shiftState.isOn() && partners.isPresent()) {
      List<String> pairAliases = partners.get();
      Map<String, ContactPerson> contactPersonsByAliases = this.contactPersonService.findContactPersonsByAliases(new HashSet<>(pairAliases));
      pairAliases.forEach(pair -> states.add(new PartnerState(stateContext, Objects.requireNonNull(contactPersonsByAliases.getOrDefault(pair, new ContactPerson(pair))))));
    }
    states.addAll(Arrays.asList(
        new OnCallState(stateContext),
        new AlarmState(stateContext),
        new SignalStrengthState(stateContext),
        new BatteryState(stateContext)
    ));
    if (ApplicationPreferences.instance().getTestAlarmEnabled(getContext())) {
      Map<TestAlarmContext, TestAlarm> allTestAlarms = this.testAlarmDao.loadAll().stream().collect(Collectors.toMap(
          TestAlarm::context,
          testAlarm -> testAlarm
      ));
      List<TestAlarmState> testAlarmStates = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext()).stream()
          .sorted(Comparator.comparing(TestAlarmContext::context))
          .map(testAlarmContext -> {
            TestAlarm testAlarm = Objects.requireNonNull(
                allTestAlarms.getOrDefault(
                    testAlarmContext,
                    new TestAlarm(testAlarmContext, null, OnOffState.OFF, "", ""))
            );
            return new TestAlarmState(
                stateContext,
                new TestAlarmStateContext(stateContext, testAlarm, formatDateTime(testAlarm.receivedTime()))
            );
          }).collect(Collectors.toList());

      if (testAlarmStates.size() == 1) {
        states.addAll(testAlarmStates);
      } else if (testAlarmStates.size() > 1) {
        states.add(new TestAlarmState(
            stateContext,
            new TestAlarmStateContext(
                stateContext,
                new TestAlarm(
                    new TestAlarmContext(String.format(Locale.getDefault(), getString(R.string.title_test_alarms) + " (%d)", testAlarmStates.size())),
                    null,
                    OnOffState.ON,
                    "",
                    null
                ),
                null
            ),
            testAlarmStates
        ));
      }
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

  private String toDuration(boolean pikettStateManuallyOn, ShiftState shiftState, Shift currentOrNextShift, Instant now, Duration prePostRunTime) {
    return pikettStateManuallyOn ? "" : String.format("(%s)%s",
        toDurationString(Duration.between(now, shiftState.isOn() ? currentOrNextShift.getEndTime(prePostRunTime) : currentOrNextShift.getStartTime(prePostRunTime)), siFormatter()),
        shiftState.getShift().map(shift -> "\n" + shift.getTitle()).orElse("")
    );
  }

  private boolean randomizedOn() {
    long installationAgeInDays = Integer.MAX_VALUE;
    Context context = getContext();
    if (context != null) {
      try {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        installationAgeInDays = Duration.between(Instant.ofEpochMilli(packageInfo.firstInstallTime), Instant.now()).toDays();
      } catch (PackageManager.NameNotFoundException e) {
        Log.e(TAG, "Can not get package info", e);
      }
    }
    return this.random.nextFloat() <= Math.min((installationAgeInDays - 30f) * 0.01f, 0.3f);
  }

  private String formatDateTime(Instant time) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(StateFragment.DATE_TIME_FORMAT, Locale.getDefault())) : "";
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View view, ContextMenu.ContextMenuInfo menuInfo) {
    ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
    State selectedItem = getGroup(getPackedPositionGroup(info.packedPosition)).key();
    selectedItem.onCreateContextMenu(getContext(), menu);
  }

  @Override
  public boolean onFragmentContextItemSelected(MenuItem item) {
    ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
    if (info == null) {
      Log.w(TAG, "No menu item was selected");
      return false;
    }
    State selectedItem = getGroup(getPackedPositionGroup(info.packedPosition)).key();
    return selectedItem.onContextItemSelected(getContext(), item);
  }

}

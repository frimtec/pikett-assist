package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;

import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ShiftState;
import com.github.frimtec.android.pikettassist.service.system.InternetAvailabilityService.InternetAvailability;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class StateContext {

  private final StateFragment stateFragment;
  private final Context context;

  private final Runnable fragmentRefreshAction;
  private final Runnable registerPhoneNumberOnSmsAdapterAction;
  private final Runnable sendLoopbackSmsAction;
  private final Runnable confirmAlertAction;
  private final Runnable closeAlertAction;
  private final Runnable showDonationDialogAction;

  private final ShiftState shiftState;
  private final String pikettStateDuration;
  private final Pair<AlertState, Long> alarmState;
  private final boolean pikettStateManuallyOn;
  private final boolean operationsCenterPhoneNumbersBlocked;

  private final Installation smsAdapterInstallation;
  private final boolean smsAdapterPermissionsGranted;

  private final SignalLevel signalStrengthLevel;
  private final boolean superviseSignalStrength;
  private final String networkOperatorName;

  private final Contact operationCenter;
  private final Set<String> operationsCenterPhoneNumbers;
  private final BatteryStatus batteryStatus;
  private final InternetAvailability internetAvailability;

  StateContext(
      StateFragment stateFragment,
      Context context,
      Runnable fragmentRefreshAction,
      Runnable registerPhoneNumberOnSmsAdapterAction,
      Runnable sendLoopbackSmsAction,
      Runnable confirmAlertAction,
      Runnable closeAlertAction,
      Runnable showDonationDialogAction,
      Installation smsAdapterInstallation,
      boolean smsAdapterPermissionsGranted,
      ShiftState shiftState,
      String pikettStateDuration,
      Pair<AlertState, Long> alarmState,
      boolean pikettStateManuallyOn,
      boolean operationsCenterPhoneNumbersBlocked,
      SignalLevel signalStrengthLevel,
      boolean superviseSignalStrength,
      String networkOperatorName,
      Contact operationCenter,
      Set<String> operationsCenterPhoneNumbers,
      BatteryStatus batteryStatus,
      InternetAvailability internetAvailability
  ) {
    this.stateFragment = stateFragment;
    this.context = context;
    this.fragmentRefreshAction = fragmentRefreshAction;
    this.registerPhoneNumberOnSmsAdapterAction = registerPhoneNumberOnSmsAdapterAction;
    this.sendLoopbackSmsAction = sendLoopbackSmsAction;
    this.confirmAlertAction = confirmAlertAction;
    this.closeAlertAction = closeAlertAction;
    this.showDonationDialogAction = showDonationDialogAction;
    this.smsAdapterInstallation = smsAdapterInstallation;
    this.smsAdapterPermissionsGranted = smsAdapterPermissionsGranted;
    this.shiftState = shiftState;
    this.pikettStateDuration = pikettStateDuration;
    this.alarmState = alarmState;
    this.pikettStateManuallyOn = pikettStateManuallyOn;
    this.operationsCenterPhoneNumbersBlocked = operationsCenterPhoneNumbersBlocked;
    this.signalStrengthLevel = signalStrengthLevel;
    this.superviseSignalStrength = superviseSignalStrength;
    this.networkOperatorName = networkOperatorName;
    this.operationCenter = operationCenter;
    this.operationsCenterPhoneNumbers = new HashSet<>(operationsCenterPhoneNumbers);
    this.batteryStatus = batteryStatus;
    this.internetAvailability = internetAvailability;
  }

  Context getContext() {
    return context;
  }

  void refreshFragment() {
    fragmentRefreshAction.run();
  }

  void registerPhoneNumberOnSmsAdapter() {
    registerPhoneNumberOnSmsAdapterAction.run();
  }

  void sendLoopbackSms() {
    sendLoopbackSmsAction.run();
  }

  void confirmAlert() {
    confirmAlertAction.run();
  }

  void closeAlert() {
    closeAlertAction.run();
  }

  void showDonationDialog() {
    showDonationDialogAction.run();
  }

  Installation getSmsAdapterInstallation() {
    return smsAdapterInstallation;
  }

  boolean isSmsAdapterPermissionsGranted() {
    return smsAdapterPermissionsGranted;
  }

  ShiftState getShiftState() {
    return shiftState;
  }

  public String getPikettStateDuration() {
    return pikettStateDuration;
  }

  Pair<AlertState, Long> getAlarmState() {
    return alarmState;
  }

  boolean isPikettStateManuallyOn() {
    return pikettStateManuallyOn;
  }

  boolean isOperationsCenterPhoneNumbersBlocked() {
    return operationsCenterPhoneNumbersBlocked;
  }

  public String getString(@StringRes int stringRes) {
    return context.getString(stringRes);
  }

  SignalLevel getSignalStrengthLevel() {
    return signalStrengthLevel;
  }

  boolean isSuperviseSignalStrength() {
    return superviseSignalStrength;
  }

  String getNetworkOperatorName() {
    return networkOperatorName;
  }

  Contact getOperationCenter() {
    return operationCenter;
  }

  public Set<String> getOperationsCenterPhoneNumbers() {
    return Collections.unmodifiableSet(operationsCenterPhoneNumbers);
  }

  public BatteryStatus getBatteryStatus() {
    return batteryStatus;
  }

  public InternetAvailability getInternetAvailability() {
    return internetAvailability;
  }

  public MenuItem addContextMenu(ContextMenu menu, int id, @StringRes int text) {
    return this.stateFragment.addContextMenu(menu, id, text);
  }
}

package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

class StateContext {

  private final Context context;

  private final BiConsumer<Intent, Integer> startActivityForResultAction;
  private final Runnable fragmentRefreshAction;
  private final Runnable registerPhoneNumberOnSmsAdapterAction;
  private final Runnable sendLoopbackSmsAction;
  private final Runnable confirmAlertAction;
  private final Runnable closeAlertAction;
  private final Runnable showDonationDialogAction;

  private final OnOffState pikettState;
  private final String pikettStateDuration;
  private final Pair<AlertState, Long> alarmState;
  private final boolean pikettStateManuallyOn;
  private final boolean operationsCenterPhoneNumbersBlocked;

  private final Installation smsAdapterInstallation;
  private final boolean smsAdapterMissing;
  private final boolean smsAdapterVersionOutdated;
  private final boolean smsAdapterPermissionsGranted;

  private final SignalLevel signalStrengthLevel;
  private final boolean superviseSignalStrength;
  private final String networkOperatorName;

  private final Contact operationCenter;
  private final Set<String> operationsCenterPhoneNumbers;

  StateContext(
      Context context,
      BiConsumer<Intent, Integer> startActivityForResultAction,
      Runnable fragmentRefreshAction,
      Runnable registerPhoneNumberOnSmsAdapterAction,
      Runnable sendLoopbackSmsAction,
      Runnable confirmAlertAction,
      Runnable closeAlertAction,
      Runnable showDonationDialogAction,
      Installation smsAdapterInstallation,
      boolean smsAdapterMissing,
      boolean smsAdapterVersionOutdated,
      boolean smsAdapterPermissionsGranted,
      OnOffState pikettState,
      String pikettStateDuration,
      Pair<AlertState, Long> alarmState,
      boolean pikettStateManuallyOn,
      boolean operationsCenterPhoneNumbersBlocked,
      SignalLevel signalStrengthLevel,
      boolean superviseSignalStrength,
      String networkOperatorName,
      Contact operationCenter,
      Set<String> operationsCenterPhoneNumbers) {
    this.context = context;
    this.startActivityForResultAction = startActivityForResultAction;
    this.fragmentRefreshAction = fragmentRefreshAction;
    this.registerPhoneNumberOnSmsAdapterAction = registerPhoneNumberOnSmsAdapterAction;
    this.sendLoopbackSmsAction = sendLoopbackSmsAction;
    this.confirmAlertAction = confirmAlertAction;
    this.closeAlertAction = closeAlertAction;
    this.showDonationDialogAction = showDonationDialogAction;
    this.smsAdapterInstallation = smsAdapterInstallation;
    this.smsAdapterMissing = smsAdapterMissing;
    this.smsAdapterVersionOutdated = smsAdapterVersionOutdated;
    this.smsAdapterPermissionsGranted = smsAdapterPermissionsGranted;
    this.pikettState = pikettState;
    this.pikettStateDuration = pikettStateDuration;
    this.alarmState = alarmState;
    this.pikettStateManuallyOn = pikettStateManuallyOn;
    this.operationsCenterPhoneNumbersBlocked = operationsCenterPhoneNumbersBlocked;
    this.signalStrengthLevel = signalStrengthLevel;
    this.superviseSignalStrength = superviseSignalStrength;
    this.networkOperatorName = networkOperatorName;
    this.operationCenter = operationCenter;
    this.operationsCenterPhoneNumbers = new HashSet<>(operationsCenterPhoneNumbers);
  }

  Context getContext() {
    return context;
  }

  @SuppressWarnings("SameParameterValue")
  void startActivityForResultAction(Intent intent, int requestCode) {
    startActivityForResultAction.accept(intent, requestCode);
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

  boolean isSmsAdapterMissing() {
    return smsAdapterMissing;
  }

  boolean isSmsAdapterVersionOutdated() {
    return smsAdapterVersionOutdated;
  }

  boolean isSmsAdapterPermissionsGranted() {
    return smsAdapterPermissionsGranted;
  }

  OnOffState getPikettState() {
    return pikettState;
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
}

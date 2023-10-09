package com.github.frimtec.android.pikettassist.state;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;

public interface ApplicationPreferences {

  String PREF_KEY_LOW_SIGNAL_FILTER = "low_signal_filter_nl";
  int PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR = 15;
  String CALENDAR_FILTER_ALL = "-1";
  String PREF_KEY_BATTERY_SAFER_AT_NIGHT = "battery_safer_at_night";
  String PREF_KEY_BATTERY_WARN_LEVEL = "battery_warn_level";

  @SuppressWarnings("PointlessArithmeticExpression")
  NonLinearNumericSeries LOW_SIGNAL_FILTER_PREFERENCE = new NonLinearNumericSeries(new int[]{
      0 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      1 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      2 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      3 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      4 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      6 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      8 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      10 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      12 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      16 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      20 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      24 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      28 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR,
      40 * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR
  });

  /**
   * @noinspection SameReturnValue
   */
  static ApplicationPreferences instance() {
    return SharedPreferencesApplicationPreferences.INSTANCE;
  }

  String getCalendarEventPikettTitlePattern(Context context);

  boolean getPartnerExtractionEnabled(Context context);

  String getPartnerSearchExtractPattern(Context context);

  String getCalendarSelection(Context context);

  Duration getPrePostRunTime(Context context);

  ContactReference getOperationsCenterContactReference(Context context);

  void setOperationsCenterContactReference(Context context, ContactReference contactReference);

  String getSmsTestMessagePattern(Context context);

  String getMetaSmsMessagePattern(Context context);

  boolean getSendConfirmSms(Context context);

  String getSmsConfirmText(Context context);

  boolean getSuperviseSignalStrength(Context context);

  int getSuperviseSignalStrengthMinLevel(Context context);

  int getSuperviseSignalStrengthSubscription(Context context);

  void setSuperviseSignalStrengthSubscription(Context context, int subscriptionId);

  void setSuperviseSignalStrength(Context context, boolean supervise);

  boolean getNotifyLowSignal(Context context);

  int getLowSignalFilterSeconds(Context context);

  int convertLowSignalFilerToSeconds(int filterValue);

  boolean getSuperviseBatteryLevel(Context context);

  void setSuperviseBatteryLevel(Context context, boolean supervise);

  int getBatteryWarnLevel(Context context);

  boolean getTestAlarmEnabled(Context context);

  String getAlarmRingTone(Context context);

  String getTestAlarmRingTone(Context context);

  Set<TestAlarmContext> getSupervisedTestAlarms(Context context);

  String getTestAlarmCheckTime(Context context);

  Set<String> getTestAlarmCheckWeekdays(Context context);

  int getTestAlarmAcceptTimeWindowMinutes(Context context);

  int getAppTheme(Context context);

  void setSuperviseTestContexts(Context context, Set<TestAlarmContext> values);

  boolean getManageVolumeEnabled(Context context);

  boolean getBatterySaferAtNightEnabled(Context context);

  int getOnCallVolume(Context context, LocalTime currentTime);

  boolean isDayProfile(Context context, LocalTime currentTime);

}

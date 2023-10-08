package com.github.frimtec.android.pikettassist.service.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings.Global;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.util.List;
import java.util.Objects;

public class SignalStrengthService {

  private static final String TAG = "SignalStrengthService";
  private final TelephonyManager telephonyManager;
  private final Context context;

  public static boolean isLowSignal(SignalStrengthService.SignalLevel level, int minLevel) {
    return level.ordinal() <= minLevel;
  }

  public SignalStrengthService(Context context) {
    this(context, ApplicationPreferences.instance().getSuperviseSignalStrengthSubscription(context));
  }

  public SignalStrengthService(Context context, int subscriptionId) {
    TelephonyManager mainTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    Objects.requireNonNull(mainTelephonyManager);
    TelephonyManager subscriptionTelephonyManager = mainTelephonyManager.createForSubscriptionId(subscriptionId);
    this.telephonyManager = subscriptionTelephonyManager != null ? subscriptionTelephonyManager : mainTelephonyManager;
    this.context = context;
  }

  public SignalLevel getSignalStrength() {
    if (isAirplaneModeOn(this.context)) {
      return SignalLevel.OFF;
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      @SuppressLint("MissingPermission") List<CellInfo> cellInfos = this.telephonyManager.getAllCellInfo();
      CellSignalStrength signalStrength = null;
      if (cellInfos.size() != 0) {
        CellInfo cellInfo = cellInfos.get(0);
        if (cellInfo instanceof CellInfoGsm) {
          signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();
        } else if (cellInfo instanceof CellInfoLte) {
          signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
        } else if (cellInfo instanceof CellInfoWcdma) {
          signalStrength = ((CellInfoWcdma) cellInfo).getCellSignalStrength();
        } else {
          Log.e(TAG, "Unknown cell info type: " + cellInfo.getClass().getName());
        }
      }
      Integer level = signalStrength != null ? signalStrength.getLevel() : null;
      return SignalLevel.from(level);
    } else {
      SignalStrength signalStrength = this.telephonyManager.getSignalStrength();
      return SignalLevel.from(signalStrength != null ? signalStrength.getLevel() : null);
    }
  }

  private static boolean isAirplaneModeOn(Context context) {
    return Global.getInt(context.getContentResolver(), Global.AIRPLANE_MODE_ON, 0) != 0;
  }

  /**
   * Gets the network operator name if available.
   *
   * @return the current network operator name or null.
   */
  public String getNetworkOperatorName() {
    String name = telephonyManager.getNetworkOperatorName();
    if (name == null || name.trim().isEmpty()) {
      return null;
    }
    return name;
  }

  public enum SignalLevel {
    OFF,
    NONE,
    POOR,
    MODERATE,
    GOOD,
    GREAT;

    private static SignalLevel from(Integer level) {
      if (level == null) {
        return OFF;
      }
      return switch (level) {
        case 0 -> NONE;
        case 1 -> POOR;
        case 2 -> MODERATE;
        case 3 -> GOOD;
        case 4 -> GREAT;
        default ->
          // Workaround for Huawei CLT-L29
            level > 4 ? GREAT : OFF;
      };
    }

    public String toString(Context context) {
      String[] signalLevels = context.getResources().getStringArray(R.array.signal_levels);
      return signalLevels[ordinal()];
    }
  }
}

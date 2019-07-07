package com.github.frimtec.android.pikettassist.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.*;
import android.util.Log;

import java.util.List;
import java.util.Optional;

public class SignalStremgthHelper {
  private static final String TAG = "SignalStremgthHelper";

  public enum SignalLevel {
    OFF(-1),
    NONE(0),
    POOR(1),
    MODERATE(2),
    GOOD(3),
    GREAT(4);
    private final int level;

    SignalLevel(int level) {
      this.level = level;
    }

    private static SignalLevel from(Integer level) {
      if (level == null) {
        return OFF;
      }
      switch (level) {
        case 0:
          return NONE;
        case 1:
          return POOR;
        case 2:
          return MODERATE;
        case 3:
          return GOOD;
        case 4:
          return GREAT;
        default:
          throw new IllegalArgumentException("Unknown level: " + level);
      }
    }
  }

  public static SignalLevel getSignalStrength(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    @SuppressLint("MissingPermission") List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

    CellSignalStrength signalStrength = null;
    if (cellInfos.size() == 0) {
      Log.w(TAG, "No signal");
    } else {
      CellInfo cellInfo = cellInfos.get(0);
      if (cellInfo instanceof CellInfoGsm) {
        signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();
      } else if (cellInfo instanceof CellInfoLte) {
        signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
      } else {
        Log.e(TAG, "Unknown cell info type: " + cellInfos.getClass().getName());
      }
      if (signalStrength != null) {
        Log.v(TAG, String.format("Signal strength dbm: %d; level: %d; asu: %d", signalStrength.getDbm(), signalStrength.getLevel(), signalStrength.getAsuLevel()));
      }
    }
    Integer level = signalStrength != null ? signalStrength.getLevel() : null;
    return SignalLevel.from(level);
  }
}

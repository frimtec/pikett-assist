package com.github.frimtec.android.pikettassist.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.R;

import java.util.List;

public class SignalStrengthHelper {

  private static final String TAG = "SignalStrengthHelper";

  public static SignalLevel getSignalStrength(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    @SuppressLint("MissingPermission") List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

    CellSignalStrength signalStrength = null;
    if (cellInfos.size() != 0) {
      CellInfo cellInfo = cellInfos.get(0);
      if (cellInfo instanceof CellInfoGsm) {
        signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();
      } else if (cellInfo instanceof CellInfoLte) {
        signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
      } else {
        Log.e(TAG, "Unknown cell info type: " + cellInfos.getClass().getName());
      }
    }
    Integer level = signalStrength != null ? signalStrength.getLevel() : null;
    return SignalLevel.from(level);
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

    public String toString(Context context) {
      String[] signalLevels = context.getResources().getStringArray(R.array.signal_levels);
      return signalLevels[ordinal()];
    }
  }
}

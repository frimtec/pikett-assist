package com.github.frimtec.android.pikettassist.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.*;
import android.util.Log;

import java.util.List;
import java.util.Optional;

public class SignalStremgthHelper {
  private static final String TAG = "SignalStremgthHelper";


  public static Optional<Integer> getSignalStrength(Context context) {
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
    return signalStrength != null ? Optional.of(signalStrength.getLevel()) : Optional.empty();
  }
}

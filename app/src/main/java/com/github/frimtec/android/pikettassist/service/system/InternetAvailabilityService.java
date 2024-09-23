package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import com.github.frimtec.android.pikettassist.R;

public class InternetAvailabilityService {

  private final ConnectivityManager connectivityManager;

  public enum InternetAvailability {
    WIFI(true, R.drawable.ic_baseline_network_wifi_24),
    MOBILE(true, R.drawable.ic_baseline_network_cell_24),
    OTHER(true, R.drawable.ic_baseline_network_cell_24),
    NONE(false, R.drawable.ic_baseline_signal_cellular_connected_no_internet_24);
    private final boolean available;
    private final int iconResource;

    InternetAvailability(boolean available, int iconResource) {
      this.available = available;
      this.iconResource = iconResource;
    }

    public boolean isAvailable() {
      return available;
    }

    public int getIconResource() {
      return iconResource;
    }

    public String toString(Context context) {
      String[] internetAvailability = context.getResources().getStringArray(R.array.internet_availability);
      return internetAvailability[ordinal()];
    }
  }

  public InternetAvailabilityService(Context context) {
    this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public InternetAvailability getInternetAvailability() {
    Network activeNetwork = connectivityManager.getActiveNetwork();
    if (activeNetwork == null) {
      return InternetAvailability.NONE;
    }
    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
    if (networkCapabilities == null) {
      return InternetAvailability.NONE;
    }

    if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
      return InternetAvailability.NONE;
    }

    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    if (activeNetworkInfo != null) {
      return switch (activeNetworkInfo.getType()) {
        case ConnectivityManager.TYPE_WIFI -> InternetAvailability.WIFI;
        case ConnectivityManager.TYPE_MOBILE -> InternetAvailability.MOBILE;
        default -> InternetAvailability.OTHER;
      };
    }
    return InternetAvailability.OTHER;
  }
}

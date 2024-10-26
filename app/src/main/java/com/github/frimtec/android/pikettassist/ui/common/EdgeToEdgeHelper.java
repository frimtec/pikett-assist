package com.github.frimtec.android.pikettassist.ui.common;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class EdgeToEdgeHelper {

  public static void handleInsets(View rootView) {
    ViewCompat.setOnApplyWindowInsetsListener(
        rootView,
        (view, windowInsets) -> {
          Insets insets = windowInsets.getInsets(
              WindowInsetsCompat.Type.systemBars() |
                  WindowInsetsCompat.Type.displayCutout()
          );
          view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
          return WindowInsetsCompat.CONSUMED;
        }
    );

  }
}

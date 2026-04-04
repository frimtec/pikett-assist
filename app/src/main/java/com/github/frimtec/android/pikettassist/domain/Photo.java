package com.github.frimtec.android.pikettassist.domain;

import android.net.Uri;

import androidx.annotation.Nullable;

public record Photo(@Nullable Uri uri, @Nullable Uri thumbnailUri) {

  public Photo() {
    this(null, (Uri) null);
  }

  public Photo(@Nullable String uri, @Nullable String thumbnailUri) {
    this(toUri(uri), toUri(thumbnailUri));
  }

  private static @Nullable Uri toUri(@Nullable String uri) {
    return uri != null ? Uri.parse(uri) : null;
  }
}

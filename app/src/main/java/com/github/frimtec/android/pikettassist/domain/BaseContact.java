package com.github.frimtec.android.pikettassist.domain;

import android.net.Uri;

import androidx.annotation.Nullable;

public abstract class BaseContact {

  @Nullable
  private final Uri photoThumbnailUri;

  public BaseContact(@Nullable String photoThumbnailUri) {
    this.photoThumbnailUri = photoThumbnailUri != null ? Uri.parse(photoThumbnailUri) : null;
  }

  @Nullable
  public Uri photoThumbnailUri() {
    return photoThumbnailUri;
  }
}

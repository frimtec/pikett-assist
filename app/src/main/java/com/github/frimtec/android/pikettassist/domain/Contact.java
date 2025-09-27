package com.github.frimtec.android.pikettassist.domain;

import androidx.annotation.Nullable;

public class Contact extends BaseContact {

  private final ContactReference reference;
  private final boolean valid;
  private final String name;

  public Contact(ContactReference reference, boolean valid, String name, @Nullable String photoThumbnailUri) {
    super(photoThumbnailUri);
    this.reference = reference;
    this.valid = valid;
    this.name = name;
  }

  public ContactReference reference() {
    return reference;
  }

  public boolean valid() {
    return valid;
  }

  public String name() {
    return name;
  }
}

package com.github.frimtec.android.pikettassist.domain;

public class Contact extends BaseContact {

  private final ContactReference reference;
  private final boolean valid;
  private final String name;

  public Contact(
      ContactReference reference,
      boolean valid,
      String name,
      Photo photo
  ) {
    super(photo);
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

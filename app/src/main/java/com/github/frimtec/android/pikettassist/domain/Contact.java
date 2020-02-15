package com.github.frimtec.android.pikettassist.domain;

public class Contact {

  private final ContactReference reference;
  private final boolean valid;
  private final String name;

  public Contact(ContactReference reference, boolean valid, String name) {
    this.reference = reference;
    this.valid = valid;
    this.name = name;
  }

  public ContactReference getReference() {
    return this.reference;
  }

  public boolean isValid() {
    return valid;
  }

  public String getName() {
    return name;
  }

}

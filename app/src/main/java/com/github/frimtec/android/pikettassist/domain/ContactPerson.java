package com.github.frimtec.android.pikettassist.domain;

public class ContactPerson {

  private static final long INVALID_CONTACT_ID = -1;

  private final long id;
  private final String fullName;

  public ContactPerson(long id, String fullName) {
    this.id = id;
    this.fullName = fullName;
  }

  public ContactPerson(String unknownAlias) {
    this.id = INVALID_CONTACT_ID;
    this.fullName = unknownAlias;
  }

  public long getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public boolean isValid() {
    return id != INVALID_CONTACT_ID;
  }
}

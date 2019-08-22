package com.github.frimtec.android.pikettassist.domain;

public class Contact {
  private final long id;
  private final boolean valid;
  private final String name;

  public Contact(long id, boolean valid, String name) {
    this.id = id;
    this.valid = valid;
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public boolean isValid() {
    return valid;
  }

  public String getName() {
    return name;
  }
}

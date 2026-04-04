package com.github.frimtec.android.pikettassist.domain;

public abstract class BaseContact {

  private final Photo photo;

  public BaseContact(Photo photo) {
    this.photo = photo;
  }

  public Photo photo() {
    return this.photo;
  }
}

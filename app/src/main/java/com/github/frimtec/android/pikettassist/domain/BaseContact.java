package com.github.frimtec.android.pikettassist.domain;

public abstract class BaseContact {

  private final Photo photo;
  private final boolean live;

  public BaseContact(Photo photo, boolean live) {
    this.photo = photo;
    this.live = live;
  }

  public Photo photo() {
    return this.photo;
  }

  public boolean live() {
    return this.live;
  }
}

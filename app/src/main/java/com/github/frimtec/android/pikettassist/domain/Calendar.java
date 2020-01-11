package com.github.frimtec.android.pikettassist.domain;

public class Calendar {

  private final int id;
  private final String name;

  public Calendar(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

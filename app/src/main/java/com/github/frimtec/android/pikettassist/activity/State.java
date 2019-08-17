package com.github.frimtec.android.pikettassist.activity;

import android.widget.Button;

public class State {

  public enum TrafficLight {
      OFF, RED, YELLOW, GREEN
  }

  private final int iconResource;
  private final String title;
  private final String value;
  private final Button button;
  private final TrafficLight state;

  public State(int iconResource, String title, String value, Button button, TrafficLight state) {
    this.iconResource = iconResource;
    this.title = title;
    this.value = value;
    this.button = button;
    this.state = state;
  }

  public int getIconResource() {
    return iconResource;
  }

  public String getTitle() {
    return title;
  }

  public String getValue() {
    return value;
  }

  public Button getButton() {
    return button;
  }

  public TrafficLight getState() {
    return state;
  }
}

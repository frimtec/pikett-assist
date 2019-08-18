package com.github.frimtec.android.pikettassist.activity;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.function.Consumer;

public class State {

  public enum TrafficLight {
      OFF, RED, YELLOW, GREEN
  }

  private final int iconResource;
  private final String title;
  private final String value;
  private final Button button;
  private final TrafficLight state;
  private final Consumer<Context> onClickAction;

  public State(int iconResource, String title, String value, Button button, TrafficLight state, Consumer<Context> onClickAction) {
    this.iconResource = iconResource;
    this.title = title;
    this.value = value;
    this.button = button;
    this.state = state;
    this.onClickAction = onClickAction;
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

  public void onClick(Context context) {
      onClickAction.accept(context);
  }
}

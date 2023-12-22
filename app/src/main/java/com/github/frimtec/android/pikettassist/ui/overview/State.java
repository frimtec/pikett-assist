package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class State implements Comparable<State> {

  private final int iconResource;
  private final String title;
  private final String value;
  private final Supplier<Button> buttonSupplier;
  private final TrafficLight state;

  private final List<? extends State> childStates;

  public enum TrafficLight {
    OFF, RED, YELLOW, GREEN
  }

  public State(int iconResource, String title, String value, Supplier<Button> buttonSupplier, TrafficLight state) {
    this.iconResource = iconResource;
    this.title = title;
    this.value = value;
    this.buttonSupplier = buttonSupplier;
    this.state = state;
    this.childStates = Collections.emptyList();
  }

  public State(int iconResource, String title, String value, Supplier<Button> buttonSupplier, TrafficLight state, List<? extends State> childStates) {
    this.iconResource = iconResource;
    this.title = title;
    this.value = value;
    this.buttonSupplier = buttonSupplier;
    this.state = state;
    this.childStates = childStates;
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
    return buttonSupplier != null ? buttonSupplier.get() : null;
  }

  public TrafficLight getState() {
    return state;
  }

  public void onClickAction(Context context) {
  }

  public void onCreateContextMenu(Context context, ContextMenu menu) {
  }

  public boolean onContextItemSelected(Context context, MenuItem item) {
    return false;
  }

  public List<? extends State> getChildStates() {
    return childStates;
  }

  @Override
  public int compareTo(State o) {
    return 0;
  }
}

package com.github.frimtec.android.pikettassist.ui.overview;

import android.net.Uri;
import android.widget.Button;

import com.github.frimtec.android.pikettassist.domain.BaseContact;

import java.util.function.Supplier;

abstract class AbstractContactState extends State {

  private final BaseContact contact;

  public AbstractContactState(
      int iconResource,
      String title,
      String value,
      Supplier<Button> buttonSupplier,
      TrafficLight state,
      BaseContact contact
  ) {
    super(iconResource, title, value, buttonSupplier, state);
    this.contact = contact;
  }

  @Override
  public final Uri getValueImage() {
    return contact.photoThumbnailUri();
  }
}

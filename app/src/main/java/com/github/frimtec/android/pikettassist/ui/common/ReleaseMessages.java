package com.github.frimtec.android.pikettassist.ui.common;

import static com.github.frimtec.android.pikettassist.domain.ContactReference.NO_SELECTION;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.github.frimtec.android.pikettassist.BuildConfig;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.Feature;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum ReleaseMessages {
  CONTACT_READ_PERMISSION_PRE_INFO(
      1,
      R.string.release_message_01_title,
      R.string.release_message_01_text,
      context -> isPlayStoreFlavor() && Feature.PERMISSION_CONTACTS_READ.isPermissionDeclared(context),
      ReleaseMessages::isNewSetup
  ),
  CONTACT_READ_PERMISSION_ANDROID_17(
      2,
      R.string.release_message_01_title,
      R.string.release_message_01_text,
      context -> isPlayStoreFlavor() && !Feature.PERMISSION_CONTACTS_READ.isPermissionDeclared(context),
      context -> ReleaseMessages.isNewSetup(context) && !Feature.PERMISSION_CONTACTS_READ.isPermissionDeclared(context)
  );

  private static boolean isNewSetup(Context context) {
    return NO_SELECTION.equals(ApplicationPreferences.instance().getOperationsCenterContactReference(context));
  }

  @SuppressWarnings("ConstantValue")
  private static boolean isPlayStoreFlavor() {
    return "playstore".equals(BuildConfig.FLAVOR);
  }

  private final int id;
  private final int titleResourceId;
  private final int textResourceId;
  private final Predicate<Context> relevanceFilter;
  private final Predicate<Context> ignoredForeverFilter;

  ReleaseMessages(
      int id,
      @StringRes int titleResourceId,
      @StringRes int textResourceId,
      Predicate<Context> relevanceFilter, Predicate<Context> ignoredForeverFilter
  ) {
    this.id = id;
    this.titleResourceId = titleResourceId;
    this.textResourceId = textResourceId;
    this.relevanceFilter = relevanceFilter;
    this.ignoredForeverFilter = ignoredForeverFilter;
  }

  public int id() {
    return id;
  }

  public int titleResourceId() {
    return titleResourceId;
  }

  public int textResourceId() {
    return textResourceId;
  }

  public static Stream<ReleaseMessages> relevantValues(Context context, @Nullable Consumer<ReleaseMessages> checkIgnoredCall) {
    if (checkIgnoredCall != null) {
      Arrays.stream(values())
          .filter(rm -> rm.ignoredForever(context))
          .forEach(checkIgnoredCall);
    }
    return Arrays.stream(values())
        .filter(rm -> rm.relevant(context));
  }

  private boolean relevant(Context context) {
    return relevanceFilter == null || relevanceFilter.test(context);
  }

  private boolean ignoredForever(Context context) {
    return ignoredForeverFilter == null || ignoredForeverFilter.test(context);
  }

}

package com.github.frimtec.android.pikettassist.service.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.junit.jupiter.api.Test;

class ContactRepositoryTest {
  @Test
  void testCreateForDeclaredPermission() {
    // arrange
    Context context = createContext(
        "android.permission.READ_CONTACTS",
        "android.permission.READ_CALENDAR"
    );

    // act
    ContactRepository contactRepository = ContactRepository.create(context);

    // assert
    assertThat(contactRepository).isInstanceOf(ContactRepositoryContactRead.class);
  }

  @Test
  void testCreateForNonDeclaredPermission() {
    // arrange
    Context context = createContext(
        "android.permission.READ_CALENDAR"
    );

    // act
    ContactRepository contactRepository = ContactRepository.create(context);

    // assert
    assertThat(contactRepository).isInstanceOf(ContactRepositoryNoContactRead.class);
  }

  private static Context createContext(String ... declaredPermissions) {
    Context context = mock(Context.class);
    PackageManager packageManager = mock(PackageManager.class);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.requestedPermissions = declaredPermissions;

    when(context.getPackageManager()).thenReturn(packageManager);
    String packageName = "com.example.app.any";
    when(context.getPackageName()).thenReturn(packageName);

    try {
      when(packageManager.getPackageInfo(eq(packageName), eq(PackageManager.GET_PERMISSIONS)))
          .thenReturn(packageInfo);
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException(e);
    }

    return context;
  }
}
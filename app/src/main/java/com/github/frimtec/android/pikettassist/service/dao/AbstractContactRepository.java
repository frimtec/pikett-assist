package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType.fromNumber;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

abstract class AbstractContactRepository implements ContactRepository {

  private final Context context;

  protected AbstractContactRepository(Context context) {
    this.context = context;
  }

  protected final @Nullable Set<String> parseCompanyShortCodes(@Nullable String company) {
    if (!TextUtils.isEmpty(company)) {
      return Arrays.stream(company.split(","))
          .filter(Objects::nonNull)
          .map(String::trim)
          .filter(number -> fromNumber(number, this.context).isShortCode())
          .collect(Collectors.toSet());
    }
    return null;
  }


}

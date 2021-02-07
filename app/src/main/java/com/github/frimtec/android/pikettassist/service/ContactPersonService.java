package com.github.frimtec.android.pikettassist.service;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.ContactPerson;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ContactPersonService extends AbstractContactService {

  public ContactPersonService(Context context) {
    super(context);
  }

  public Map<String, ContactPerson> findContactPersonsByAliases(Set<String> aliases) {
    return hasReadContactPermission() ? getContactDao().findContactPersonsByAliases(aliases) : Collections.emptyMap();
  }
}

package com.github.frimtec.android.pikettassist.service;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.service.dao.ContactDao;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;

abstract class AbstractContactService {

  private final Context context;
  private final ContactDao contactDao;

  public AbstractContactService(Context context) {
    this.context = context;
    this.contactDao = new ContactDao(context);
  }

  public Map<String, ContactPerson> findContactPersonsByAliases(Set<String> aliases) {
    return hasReadContactPermission() ? this.contactDao.findContactPersonsByAliases(aliases) : Collections.emptyMap();
  }

  protected final boolean hasReadContactPermission() {
    return PERMISSION_CONTACTS_READ.isAllowed(this.context);
  }

  protected final ContactDao getContactDao() {
    return this.contactDao;
  }
}

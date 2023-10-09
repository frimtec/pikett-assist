package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;

import android.content.Context;

import com.github.frimtec.android.pikettassist.service.dao.ContactDao;

abstract class AbstractContactService {

  private final Context context;
  private final ContactDao contactDao;

  public AbstractContactService(Context context) {
    this.context = context;
    this.contactDao = new ContactDao(context);
  }

  protected final boolean hasReadContactPermission() {
    return PERMISSION_CONTACTS_READ.isAllowed(this.context);
  }

  protected final ContactDao getContactDao() {
    return this.contactDao;
  }
}

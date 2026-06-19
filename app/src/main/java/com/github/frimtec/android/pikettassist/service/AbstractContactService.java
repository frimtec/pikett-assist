package com.github.frimtec.android.pikettassist.service;


import android.content.Context;

import com.github.frimtec.android.pikettassist.service.dao.ContactRepository;

abstract class AbstractContactService {

  private final ContactRepository contactRepository;

  public AbstractContactService(Context context) {
    this.contactRepository = ContactRepository.create(context);
  }

  protected final ContactRepository getContactRepository() {
    return this.contactRepository;
  }
}

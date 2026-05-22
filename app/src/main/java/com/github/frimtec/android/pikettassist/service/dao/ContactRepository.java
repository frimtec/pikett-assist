package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.domain.ContactReference.NO_SELECTION;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.StringRes;

import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.domain.Photo;
import com.github.frimtec.android.pikettassist.service.system.Feature;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ContactRepository {
  Optional<Contact> getContact(long id);

  Optional<Contact> getContact(Uri contactUri);

  Map<String, ContactPerson> findContactPersonsByAliases(Set<String> aliases);

  Set<String> getPhoneNumbers(Contact contact);
   Set<String> getShortCodesFromContact(Contact contact);

   static  ContactRepository create(Context context) {
     return Feature.PERMISSION_CONTACTS_READ.isPermissionDeclared(context) ?
         new ContactRepositoryContactRead(context) : new ContactRepositoryNoContactRead(context);
   }

  boolean isContactsPhoneNumber(Contact contact, String number);


  static Contact invalidContact(Context context, @StringRes int contactName) {
    return new Contact(NO_SELECTION, false, context.getString(contactName), new Photo(), true);
  }

}

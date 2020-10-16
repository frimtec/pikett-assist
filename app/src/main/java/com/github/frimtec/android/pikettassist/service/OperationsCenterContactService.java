package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.StringRes;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.service.dao.ContactDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.util.Set;

import static com.github.frimtec.android.pikettassist.domain.ContactReference.NO_SELECTION;
import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;

public class OperationsCenterContactService {

  private static final String TAG = "OperationsCenterContactService";

  private final ContactDao contactDao;
  private final Context context;

  public OperationsCenterContactService(Context context) {
    this.context = context;
    this.contactDao = new ContactDao(context);
  }

  public Contact getOperationsCenterContact() {
    if (!hasReadContactPermission()) {
      return invalidContact(R.string.contact_name_no_access);
    }
    ContactReference contactReference = ApplicationPreferences.instance().getOperationsCenterContactReference(this.context);
    if (!contactReference.isComplete()) {
      contactReference = migrateToFullReference(contactReference);
    }
    if (NO_SELECTION.equals(contactReference)) {
      return invalidContact(R.string.contact_preference_empty_selection);
    }
    Uri lookupUri = ContactsContract.Contacts.getLookupUri(contactReference.getId(), contactReference.getLookupKey());
    Contact contact = this.contactDao.getContact(lookupUri)
        .orElseGet(() -> invalidContact(R.string.contact_helper_unknown_contact));
    if (contact.isValid()) {
      if (!contact.getReference().equals(contactReference)) {
        ApplicationPreferences.instance().setOperationsCenterContactReference(context, contact.getReference());
        Log.w(TAG, "Alarm operations center contact reference changed and updated.");
      }
    } else {
      Log.e(TAG, "Alarm operations center contact reference no more valid.");
    }
    return contact;
  }

  public Contact getContactFromUri(Uri uri) {
    return this.contactDao.getContact(uri)
        .orElse(invalidContact(R.string.contact_helper_unknown_contact));
  }

  public boolean isContactsPhoneNumber(Contact contact, String number) {
    if (!contact.isValid()) {
      Log.e(TAG, "SMS received but no valid operations center defined.");
      return false;
    }
    Set<Long> contactIds = this.contactDao.lookupContactIdsByPhoneNumber(number);
    return contactIds.contains(contact.getReference().getId());
  }

  public Set<String> getPhoneNumbers(Contact contact) {
    return this.contactDao.getPhoneNumbers(contact);
  }

  private ContactReference migrateToFullReference(ContactReference contactReference) {
    ContactReference migratedReference = this.contactDao.getContact(contactReference.getId())
        .map(Contact::getReference)
        .orElse(NO_SELECTION);
    ApplicationPreferences.instance().setOperationsCenterContactReference(this.context, migratedReference);
    if (migratedReference != NO_SELECTION) {
      Log.i(TAG, "Operations center contact migrated from id to reference.");
    } else {
      Log.e(TAG, "Operations center contact migration failed.");
    }
    return migratedReference;
  }

  private boolean hasReadContactPermission() {
    return PERMISSION_CONTACTS_READ.isAllowed(this.context);
  }

  private Contact invalidContact(@StringRes int contactName) {
    return new Contact(NO_SELECTION, false, this.context.getString(contactName));
  }
}

package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.domain.ContactReference.NO_SELECTION;
import static com.github.frimtec.android.pikettassist.service.dao.ContactRepository.invalidContact;
import static com.github.frimtec.android.pikettassist.service.system.NotificationService.INVALID_OPERATIONAL_CENTER_ID;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.service.dao.ContactRepository;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.util.Set;

public class OperationsCenterContactService extends AbstractContactService {

  private static final String TAG = "OperationsCenterContactService";

  private final Context context;

  public OperationsCenterContactService(Context context) {
    super(context);
    this.context = context;
  }

  public Contact getOperationsCenterContact() {
    ContactReference contactReference = ApplicationPreferences.instance().getOperationsCenterContactReference(this.context);
    if (NO_SELECTION.equals(contactReference)) {
      return invalidContact(this.context, R.string.contact_preference_empty_selection);
    }
    if (!contactReference.isComplete()) {
      contactReference = migrateToFullReference(contactReference);
    }
    Contact contact = getContactRepository().getContact(contactReference.id())
        .orElseGet(() -> invalidContact(this.context, R.string.contact_helper_unknown_contact));
    if (contact.valid()) {
      new NotificationService(context).cancelNotification(INVALID_OPERATIONAL_CENTER_ID);
      if (!contact.reference().equals(contactReference)) {
        ApplicationPreferences.instance().setOperationsCenterContactReference(context, contact.reference());
        Log.w(TAG, "Alarm operations center contact reference changed and updated.");
      }
    } else {
      Log.e(TAG, "Alarm operations center contact reference no more valid.");
    }
    return contact;
  }

  public Contact getContactFromUri(Uri uri) {
    return getContactRepository().getContact(uri)
        .orElse(invalidContact(this.context, R.string.contact_helper_unknown_contact));
  }

  public boolean isContactsPhoneNumber(Contact contact, String number) {
    if (!contact.valid()) {
      Log.e(TAG, "SMS received but no valid operations center defined.");
      return false;
    }
    ContactRepository contactRepository = getContactRepository();
    return contactRepository.isContactsPhoneNumber(contact, number);
  }

  public Set<String> getPhoneNumbers(Contact contact) {
    return getContactRepository().getPhoneNumbers(contact);
  }

  private ContactReference migrateToFullReference(ContactReference contactReference) {
    ContactReference migratedReference = getContactRepository().getContact(contactReference.id())
        .map(Contact::reference)
        .orElse(NO_SELECTION);
    ApplicationPreferences.instance().setOperationsCenterContactReference(this.context, migratedReference);
    if (migratedReference != NO_SELECTION) {
      Log.i(TAG, "Operations center contact migrated from id to reference.");
    } else {
      Log.e(TAG, "Operations center contact migration failed.");
    }
    return migratedReference;
  }
}

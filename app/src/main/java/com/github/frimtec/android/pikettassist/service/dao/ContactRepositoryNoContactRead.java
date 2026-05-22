package com.github.frimtec.android.pikettassist.service.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.domain.Photo;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ContactRepositoryNoContactRead extends AbstractContactRepository {

  private static final String TAG = "ContactRepositoryNoAccess";

  static final String[] PROJECTION_URI = new String[]{
      ContactsContract.Contacts._ID,
      ContactsContract.Contacts.LOOKUP_KEY,
      ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
      ContactsContract.Contacts.PHOTO_URI,
      ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
      ContactsContract.Data.MIMETYPE,
      ContactsContract.Data.DATA1,
      ContactsContract.CommonDataKinds.Photo.PHOTO
  };

  private final Context context;
  private final ContentResolver contentResolver;
  private final ApplicationState applicationState;

  public ContactRepositoryNoContactRead(Context context) {
    super(context);
    this.context = context;
    this.contentResolver = context.getContentResolver();
    this.applicationState = ApplicationState.instance();
  }

  public Optional<Contact> getContact(long id) {
    return this.applicationState.loadContact(id)
        .map(contactCopy -> new Contact(
            contactCopy.reference(),
            true,
            contactCopy.fullName(),
            contactCopy.photo(),
            false
        ));
  }

  public Optional<Contact> getContact(Uri contactUri) {
    try (Cursor cursor = this.contentResolver.query(contactUri, PROJECTION_URI, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        ContactReference reference = new ContactReference(cursor.getLong(0), cursor.getString(1));
        String fullName = cursor.getString(2);
        String nickname = null;
        Uri photoUri = null;
        Set<String> phoneNumbers = new HashSet<>();
        Set<String> shortCodes = new HashSet<>();
        while (cursor.moveToNext()) {
          String data1 = cursor.getString(6);
          switch (cursor.getString(5)) {
            case ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE:
              nickname = data1;
              break;
            case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
              phoneNumbers.add(PhoneNumberUtils.normalizeNumber(data1));
              break;
            case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
              shortCodes = parseCompanyShortCodes(data1);
              break;
            case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
              byte[] blob = cursor.getBlob(7);
              photoUri = persistPhoto(reference.id(), blob);
              break;
          }
        }

        ContactCopy contactCopy = new ContactCopy(
            Instant.now(),
            reference,
            fullName,
            nickname,
            new Photo(
                photoUri,
                photoUri
            ),
            phoneNumbers,
            shortCodes
        );

        persist(contactCopy);
        return Optional.of(new Contact(
            contactCopy.reference(),
            true,
            contactCopy.fullName(),
            contactCopy.photo(),
            false
        ));
      }
    }
    return Optional.empty();
  }

  private void persist(ContactCopy contactCopy) {
    this.applicationState.saveContact(contactCopy);
  }

  private Uri persistPhoto(long id, byte[] blob) {
    if (blob == null) {
      return null;
    }
    File file = new File(context.getFilesDir(), "contact_" + id + "_photo.png");
    try (OutputStream os = new FileOutputStream(file)) {
      os.write(blob);
      return Uri.fromFile(file);
    } catch (IOException e) {
      Log.e(TAG, "Cannot persist photo for contact " + id, e);
      return null;
    }
  }

  public Map<String, ContactPerson> findContactPersonsByAliases(Set<String> aliases) {
    Map<String, ContactPerson> result = new HashMap<>();
    for (String alias : aliases) {
      this.applicationState.loadContact(alias).ifPresent(contact -> result.put(alias, new ContactPerson(
              contact.nickname(),
              contact.reference().id(),
              contact.fullName(),
              contact.photo(),
              false
          )
      ));
    }
    return result;
  }

  public Set<String> getPhoneNumbers(Contact contact) {
    return this.applicationState.loadContact(contact.reference().id())
        .map(contactCopy -> {
          Set<String> allNumbers = new HashSet<>(contactCopy.phoneNumbers());
          allNumbers.addAll(contactCopy.shortCodes());
          return allNumbers;
        })
        .orElse(Collections.emptySet());
  }

  public Set<String> getShortCodesFromContact(Contact contact) {
    return this.applicationState.loadContact(contact.reference().id())
        .map(ContactCopy::shortCodes)
        .orElse(Collections.emptySet());
  }

  @Override
  public boolean isContactsPhoneNumber(Contact contact, String number) {
    return ApplicationState.instance().loadContact(contact.reference().id()).map(contactCopy ->
        contactCopy.phoneNumbers().contains(number) || contactCopy.shortCodes().contains(number)
    ).orElse(false);
  }
}

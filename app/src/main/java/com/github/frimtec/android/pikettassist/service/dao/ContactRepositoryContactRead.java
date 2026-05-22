package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.service.dao.ContactRepository.invalidContact;
import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;
import static com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType.NUMERIC_SHORT_CODE;
import static java.util.stream.Collectors.joining;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.domain.ContactReference;
import com.github.frimtec.android.pikettassist.domain.Photo;
import com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class ContactRepositoryContactRead extends AbstractContactRepository {

  private static final String TAG = "ContactRepositoryWithReadAccess";

  static final String[] PROJECTION_URI = new String[]{
      ContactsContract.Contacts._ID,
      ContactsContract.Contacts.LOOKUP_KEY,
      ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
      ContactsContract.Contacts.PHOTO_URI,
      ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
  };

  private final Context context;
  private final ContentResolver contentResolver;

  public ContactRepositoryContactRead(Context context) {
    super(context);
    this.context = context;
    this.contentResolver = context.getContentResolver();
  }

  public Optional<Contact> getContact(long id) {
    return guard(
        () -> {
          try (Cursor cursor = this.contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
              new String[]{
                  ContactsContract.Contacts.LOOKUP_KEY,
                  ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                  ContactsContract.Contacts.PHOTO_URI,
                  ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
              },
              ContactsContract.Contacts._ID + " = ?",
              new String[]{String.valueOf(id)}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
              ContactReference reference = new ContactReference(id, cursor.getString(0));
              return Optional.of(new Contact(
                  reference,
                  true,
                  cursor.getString(1),
                  new Photo(
                      cursor.getString(2),
                      cursor.getString(3)
                  ),
                  true
              ));
            }
          }
          return Optional.empty();
        },
        Optional.of(
            invalidContact(this.context, R.string.contact_name_no_access)
        )
    );
  }

  public Optional<Contact> getContact(Uri contactUri) {
    return guard(
        () -> {
          try (Cursor cursor = this.contentResolver.query(contactUri, PROJECTION_URI, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
              ContactReference reference = new ContactReference(cursor.getLong(0), cursor.getString(1));
              return Optional.of(new Contact(
                  reference,
                  true,
                  cursor.getString(2),
                  new Photo(
                      cursor.getString(3),
                      cursor.getString(4)
                  ),
                  true
              ));
            }
          }
          return Optional.empty();
        },
        Optional.of(
            invalidContact(this.context, R.string.contact_name_no_access)
        )
    );
  }

  public Map<String, ContactPerson> findContactPersonsByAliases(Set<String> aliases) {
    return guard(
        () -> {
          try (Cursor cursor = this.contentResolver.query(ContactsContract.Data.CONTENT_URI,
              new String[]{
                  ContactsContract.CommonDataKinds.Nickname.DATA1,
                  ContactsContract.Data.CONTACT_ID,
                  ContactsContract.Data.DISPLAY_NAME,
                  ContactsContract.Contacts.PHOTO_URI,
                  ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
              },
              ContactsContract.CommonDataKinds.Nickname.DATA1 + " IN (" + in(aliases) + ")",
              null,
              null)) {
            if (cursor != null && cursor.moveToFirst()) {
              Map<String, ContactPerson> contactPeople = new HashMap<>();
              do {
                String nickname = cursor.getString(0);
                contactPeople.put(nickname, new ContactPerson(
                    nickname,
                    cursor.getLong(1),
                    cursor.getString(2),
                    new Photo(
                        cursor.getString(3),
                        cursor.getString(4)
                    ),
                    true
                ));
              } while (cursor.moveToNext());
              return contactPeople;
            }
            return Collections.emptyMap();
          }
        },
        Collections.emptyMap()
    );
  }

  private String in(Set<String> elements) {
    return elements.stream().map(element -> String.format("'%s'", element)).collect(joining(","));
  }

  Set<Long> lookupContactIdsByPhoneNumber(String phoneNumber, boolean normalized) {
    return guard(
        () -> {
          try (Cursor cursor = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
              new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
              (normalized ? ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER : ContactsContract.CommonDataKinds.Phone.NUMBER) + " = ?",
              new String[]{phoneNumber}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
              Set<Long> contactIds = new HashSet<>();
              do {
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                if (columnIndex >= 0) {
                  contactIds.add(cursor.getLong(columnIndex));
                } else {
                  Log.e(TAG, "Column CONTACT_ID not found in cursor");
                }
              } while (cursor.moveToNext());
              return contactIds;
            }
            return Collections.emptySet();
          }
        },
        Collections.emptySet()
    );
  }

  public Set<String> getPhoneNumbers(Contact contact) {
    return guard(
        () -> {
          Set<String> phoneNumbers = new HashSet<>();

          // add real phone numbers
          try (Cursor cursor = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
              new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.NUMBER},
              ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
              new String[]{String.valueOf(contact.reference().id())}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
              do {
                int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                String normalizedNumber = null;
                if (columnIndex >= 0) {
                  normalizedNumber = cursor.getString(columnIndex);
                } else {
                  Log.e(TAG, "Column NORMALIZED_NUMBER not found in cursor");
                }
                if (normalizedNumber != null) {
                  phoneNumbers.add(normalizedNumber);
                } else {
                  int columnIndexNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                  String number = columnIndexNumber >= 0 ? cursor.getString(columnIndexNumber) : "NA";
                  if (number != null && number.matches("\\d+") && PhoneNumberType.fromNumber(number, context) == NUMERIC_SHORT_CODE) {
                    phoneNumbers.add(number);
                  } else {
                    Log.e(TAG, "Skipping phone number as normalized number is null for number: " + number);
                  }
                }
              } while (cursor.moveToNext());
            }
          }

          // add short codes from contact organization field as comma separated list
          phoneNumbers.addAll(getShortCodesFromContact(contact));
          return phoneNumbers;
        },
        Collections.emptySet()
    );
  }

  public Set<String> getShortCodesFromContact(Contact contact) {
    return guard(
        () -> {
          try (Cursor cursor = this.contentResolver.query(ContactsContract.Data.CONTENT_URI,
              null, ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{String.valueOf(contact.reference().id()),
                  ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}, null)) {
            if (cursor == null) return Collections.emptySet();
            if (cursor.moveToFirst()) {
              int columnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY);
              if (columnIndex >= 0) {
                String company = cursor.getString(columnIndex);
                Set<String> company1 = parseCompanyShortCodes(company);
                if (company1 != null) return company1;
              } else {
                Log.e(TAG, "Column COMPANY not found in cursor");
              }
            }
          }
          return Collections.emptySet();
        },
        Collections.emptySet()
    );
  }

  @Override
  public boolean isContactsPhoneNumber(Contact contact, String number) {
    return lookupContactIdsByPhoneNumber(number, true).contains(contact.reference().id()) ||
        lookupContactIdsByPhoneNumber(number, false).contains(contact.reference().id()) ||
        getShortCodesFromContact(contact).contains(number);
  }

  private <T> T guard(Supplier<T> blockToGuard, T fallbackResult) {
    if (!PERMISSION_CONTACTS_READ.isAllowed(this.context)) {
      Log.w(TAG, "guardContactQuery: Blocked execution due to missing permissions");
      return fallbackResult;
    }
    return blockToGuard.get();
  }
}

package com.github.frimtec.android.pikettassist.service.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.domain.ContactReference;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;

public class ContactDao {

  private static final String TAG = "ContactDao";

  static final String[] PROJECTION_URI = new String[]{
      ContactsContract.Contacts._ID,
      ContactsContract.Contacts.LOOKUP_KEY,
      ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
  };

  private final ContentResolver contentResolver;

  public ContactDao(Context context) {
    this.contentResolver = context.getContentResolver();
  }

  public Optional<Contact> getContact(long id) {
    try (Cursor cursor = this.contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(id)}, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        ContactReference reference = new ContactReference(id, cursor.getString(0));
        return Optional.of(new Contact(reference, true, cursor.getString(1)));
      }
    }
    return Optional.empty();
  }

  public Optional<Contact> getContact(Uri contactUri) {
    try (Cursor cursor = this.contentResolver.query(contactUri, PROJECTION_URI, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        ContactReference reference = new ContactReference(cursor.getLong(0), cursor.getString(1));
        return Optional.of(new Contact(reference, true, cursor.getString(2)));
      }
    }
    return Optional.empty();
  }

  public Map<String, ContactPerson> findContactPersonsByAliases(Set<String> aliases) {
    try (Cursor cursor = this.contentResolver.query(ContactsContract.Data.CONTENT_URI,
        new String[]{
            ContactsContract.CommonDataKinds.Nickname.DATA1,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME
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
              cursor.getString(2)
          ));
        } while (cursor.moveToNext());
        return contactPeople;
      }
      return Collections.emptyMap();
    }
  }

  private String in(Set<String> elements) {
    return elements.stream().map(element -> String.format("'%s'", element)).collect(joining(","));
  }

  public Set<Long> lookupContactIdsByPhoneNumber(String phoneNumber) {
    try (Cursor cursor = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
        new String[]{phoneNumber}, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        Set<Long> contactIds = new HashSet<>();
        do {
          contactIds.add(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
        } while (cursor.moveToNext());
        return contactIds;
      }
      return Collections.emptySet();
    }
  }

  public Set<String> getPhoneNumbers(Contact contact) {
    try (Cursor cursor = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.NUMBER},
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        new String[]{String.valueOf(contact.getReference().getId())}, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        Set<String> phoneNumbers = new HashSet<>();
        do {
          String normalizedNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
          if (normalizedNumber != null) {
            phoneNumbers.add(normalizedNumber);
          } else {
            Log.w(TAG, "Skipping phone number as normalized number is null for number: " + cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
          }
        } while (cursor.moveToNext());
        return phoneNumbers;
      }
      return Collections.emptySet();
    }
  }
}

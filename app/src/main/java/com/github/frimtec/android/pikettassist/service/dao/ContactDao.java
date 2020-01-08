package com.github.frimtec.android.pikettassist.service.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.domain.Contact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ContactDao {

  private final ContentResolver contentResolver;

  public ContactDao(Context context) {
    this.contentResolver = context.getContentResolver();
  }

  public Optional<Contact> getContact(long id) {
    try (Cursor cursor = this.contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(id)}, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        return Optional.of(new Contact(id, true, cursor.getString(0)));
      }
    }
    return Optional.empty();
  }

  public Optional<Contact> getContact(Uri contactUri) {
    String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
    try (Cursor cursor = this.contentResolver.query(contactUri, projection, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        return Optional.of(new Contact(cursor.getLong(0), true, cursor.getString(1)));
      }
    }
    return Optional.empty();
  }

  public Set<Long> lookupContactIdByPhoneNumber(String phoneNumber) {
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

  public Set<String> getPhoneNumbers(long contactId) {
    try (Cursor cursor = this.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER},
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        new String[]{String.valueOf(contactId)}, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        Set<String> phoneNumbers = new HashSet<>();
        do {
          phoneNumbers.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)));
        } while (cursor.moveToNext());
        return phoneNumbers;
      }
      return Collections.emptySet();
    }
  }
}

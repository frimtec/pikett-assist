package com.github.frimtec.android.pikettassist.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ContactHelper {

  private static final long UNKNOWN_ID = -1;

  public static Contact getContact(Context context, long id) {
    ContentResolver cr = context.getContentResolver();
    try (Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(id)}, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        return new Contact(id, true, cursor.getString(0));
      }
    }
    return notFound(context);
  }

  public static Contact getContact(Context context, Uri contactUri) {
    String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
    try (Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        return new Contact(cursor.getLong(0), true, cursor.getString(1));
      }
    }
    return notFound(context);
  }

  public static Contact notFound(Context context) {
    return new Contact(UNKNOWN_ID, false, context.getString(R.string.contact_helper_unknown_contact));
  }

  public static Set<Long> lookupContactIdByPhoneNumber(Context context, String phoneNumber) {
    ContentResolver cr = context.getContentResolver();
    try (Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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

  public static Set<String> getPhoneNumbers(Context context, long contactId) {
    ContentResolver cr = context.getContentResolver();
    try (Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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

package com.github.frimtec.android.pikettassist.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;

import java.util.Optional;

public class ContactHelper {

  public static Contact getContact(Context context, long id) {
    ContentResolver cr = context.getContentResolver();
    try (Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(id)}, null)) {
      if (cursor.moveToFirst()) {
        return new Contact(id, true, cursor.getString(0));
      }
    }
    return notFound(context, id);
  }

  public static Contact getContact(Context context, Uri contactUri) {
    String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};
    try (Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        return new Contact(cursor.getLong(0), true, cursor.getString(1));
      }
    }
    return notFound(context, -1);
  }

  private static Contact notFound(Context context, long id) {
    return new Contact(id, false, context.getString(R.string.contact_helper_unknown_contact));
  }

  public static Optional<Long> lookupContactIdByPhoneNumber(Context context, String phoneNumber) {
    ContentResolver cr = context.getContentResolver();
    try (Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
        new String[]{phoneNumber}, null)) {
      if (cursor.moveToFirst()) {
        return Optional.of(cursor.getLong(
            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
      }
      return Optional.empty();
    }
  }
}

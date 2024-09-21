package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.service.dao.ContactDao.PROJECTION_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.domain.ContactReference;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class ContactDaoTest {

  @Test
  void getContactForIdReturnsContact() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getString(0)).thenReturn("lookupKey");
    when(cursor.getString(1)).thenReturn("name");
    when(resolver.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(12L)}, null))
        .thenReturn(cursor);

    Optional<Contact> contact = dao.getContact(12L);
    assertThat(contact).isNotEmpty();
    assertThat(contact.get().name()).isEqualTo("name");
    assertThat(contact.get().reference()).isEqualTo(new ContactReference(12L, "lookupKey"));
  }

  @Test
  void getContactForIdWithNullCursorReturnsEmpty() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    when(resolver.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(12L)}, null))
        .thenReturn(null);

    Optional<Contact> contact = dao.getContact(12L);
    assertThat(contact).isEmpty();
  }

  @Test
  void getContactForIdWithEmptyCursorReturnsEmpty() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    when(resolver.query(ContactsContract.Contacts.CONTENT_URI,
        new String[]{ContactsContract.Contacts.LOOKUP_KEY, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
        ContactsContract.Contacts._ID + " = ?",
        new String[]{String.valueOf(12L)}, null))
        .thenReturn(cursor);

    Optional<Contact> contact = dao.getContact(12L);
    assertThat(contact).isEmpty();
  }

  @Test
  void getContactForUriReturnsContact() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(0)).thenReturn(12L);
    when(cursor.getString(1)).thenReturn("lookupKey");
    when(cursor.getString(2)).thenReturn("name");
    Uri uri = mock(Uri.class);
    when(resolver.query(eq(uri), eq(PROJECTION_URI), isNull(), isNull(), isNull()))
        .thenReturn(cursor);

    Optional<Contact> contact = dao.getContact(uri);
    assertThat(contact).isNotEmpty();
    assertThat(contact.get().name()).isEqualTo("name");
    assertThat(contact.get().reference()).isEqualTo(new ContactReference(12L, "lookupKey"));
  }

  @Test
  void getContactForUriWithNullCursorReturnsEmpty() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Uri uri = mock(Uri.class);
    when(resolver.query(eq(uri), eq(PROJECTION_URI), isNull(), isNull(), isNull()))
        .thenReturn(null);

    Optional<Contact> contact = dao.getContact(uri);
    assertThat(contact).isEmpty();
  }

  @Test
  void getContactForUriWithEmptyCursorReturnsEmpty() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    Uri uri = mock(Uri.class);
    when(resolver.query(eq(uri), eq(PROJECTION_URI), isNull(), isNull(), isNull()))
        .thenReturn(cursor);

    Optional<Contact> contact = dao.getContact(uri);
    assertThat(contact).isEmpty();
  }

  @Test
  void lookupContactIdsByPhoneNumber() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.moveToNext()).thenReturn(true, false);
    when(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)).thenReturn(4);
    when(cursor.getLong(4)).thenReturn(12L, 15L);
    when(resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
        new String[]{"number"}, null))
        .thenReturn(cursor);

    Set<Long> contactIds = dao.lookupContactIdsByPhoneNumber("number", true);
    assertThat(contactIds).isEqualTo(new HashSet<>(Arrays.asList(
        12L,
        15L
    )));
  }

  @Test
  void lookupContactIdsByPhoneNumberForEmptyCursorReturnsEmpty() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    when(resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
        new String[]{"number"}, null))
        .thenReturn(cursor);

    Set<Long> contactIds = dao.lookupContactIdsByPhoneNumber("number", true);
    assertThat(contactIds).isEmpty();
  }

  @Test
  void lookupContactIdsByPhoneNumberForNullCursorReturnsEmpty() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    when(resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID},
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " = ?",
        new String[]{"number"}, null))
        .thenReturn(null);

    Set<Long> contactIds = dao.lookupContactIdsByPhoneNumber("number", true);
    assertThat(contactIds).isEmpty();
  }

  @Test
  void getPhoneNumbers() {
    Contact contact = new Contact(new ContactReference(12L, "lookupKey"), true, "name");
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.moveToNext()).thenReturn(true, true, false);
    when(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)).thenReturn(2);
    when(cursor.getString(2)).thenReturn("number1", null, "number2");
    when(resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.NUMBER},
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        new String[]{String.valueOf(contact.reference().id())}, null))
        .thenReturn(cursor);

    Set<String> contactIds = dao.getPhoneNumbers(contact);
    assertThat(contactIds).isEqualTo(new HashSet<>(Arrays.asList(
        "number1",
        "number2"
    )));
  }

  @Test
  void getPhoneNumbersForNullCursorReturnsEmpty() {
    Contact contact = new Contact(new ContactReference(12L, "lookupKey"), true, "name");
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    when(resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.NUMBER},
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        new String[]{String.valueOf(contact.reference().id())}, null))
        .thenReturn(null);

    Set<String> contactIds = dao.getPhoneNumbers(contact);
    assertThat(contactIds).isEmpty();
  }

  @Test
  void getPhoneNumbersForEmptyCursorReturnsEmpty() {
    Contact contact = new Contact(new ContactReference(12L, "lookupKey"), true, "name");
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    ContactDao dao = new ContactDao(context);
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    when(resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.NUMBER},
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        new String[]{String.valueOf(contact.reference().id())}, null))
        .thenReturn(cursor);

    Set<String> contactIds = dao.getPhoneNumbers(contact);
    assertThat(contactIds).isEmpty();
  }

}
package com.github.frimtec.android.pikettassist.ui.common;

import static android.provider.ContactsPickerSessionContract.ACTION_PICK_CONTACTS;
import static android.provider.ContactsPickerSessionContract.EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.service.system.Feature;

import java.util.ArrayList;

public final class ContactPicker {

  private ContactPicker() {
  }

  public static Intent createContactPickerIntent(Context context) {
    Intent intent;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN && !Feature.PERMISSION_CONTACTS_READ.isPermissionDeclared(context)) {
      intent = new Intent(ACTION_PICK_CONTACTS);
      ArrayList<String> requestedFields = new ArrayList<>();
      requestedFields.add(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
      requestedFields.add(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
      requestedFields.add(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
      requestedFields.add(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
      requestedFields.add(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
      intent.putStringArrayListExtra(EXTRA_PICK_CONTACTS_REQUESTED_DATA_FIELDS, requestedFields);
    } else {
      intent = new Intent(Intent.ACTION_PICK);
      intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    }
    return intent;
  }

}

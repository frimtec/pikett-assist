package com.github.frimtec.android.pikettassist.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.service.OperationsCenterContactService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.takisoft.preferencex.PreferenceActivityResultListener;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class ContactPreference extends Preference implements PreferenceActivityResultListener {

  private static final int CONTACT_SELECTED = 42;

  private final OperationsCenterContactService operationsCenterContactService;

  public ContactPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.operationsCenterContactService = new OperationsCenterContactService(context);
  }

  public ContactPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.operationsCenterContactService = new OperationsCenterContactService(context);
  }

  public ContactPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.operationsCenterContactService = new OperationsCenterContactService(context);
  }

  public ContactPreference(Context context) {
    super(context);
    this.operationsCenterContactService = new OperationsCenterContactService(context);
  }

  @Override
  public void onPreferenceClick(@NonNull PreferenceFragmentCompat fragment, @NonNull Preference preference) {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    fragment.startActivityForResult(intent, CONTACT_SELECTED);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == CONTACT_SELECTED && data != null) {
      Uri contactUri = data.getData();
      Contact contact = this.operationsCenterContactService.getContactFromUri(contactUri);
      ApplicationPreferences.setOperationsCenterContactReference(getContext(), contact.getReference());
      notifyChanged();
    }
  }

  @Override
  public CharSequence getSummary() {
    Contact contact = this.operationsCenterContactService.getOperationsCenterContact();
    return contact.getName();
  }
}

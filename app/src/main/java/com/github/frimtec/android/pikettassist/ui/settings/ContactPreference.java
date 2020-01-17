package com.github.frimtec.android.pikettassist.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.service.dao.ContactDao;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.takisoft.preferencex.PreferenceActivityResultListener;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import static com.github.frimtec.android.pikettassist.domain.Contact.unknown;
import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;

public class ContactPreference extends Preference implements PreferenceActivityResultListener {

  private static final int CONTACT_SELECTED = 42;

  private final ContactDao contactDao;

  public ContactPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    this.contactDao = new ContactDao(context);
  }

  public ContactPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.contactDao = new ContactDao(context);
  }

  public ContactPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.contactDao = new ContactDao(context);
  }

  public ContactPreference(Context context) {
    super(context);
    this.contactDao = new ContactDao(context);
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
      Contact contact = this.contactDao.getContact(contactUri).orElse(unknownContact());
      SharedState.setAlarmOperationsCenterContact(getContext(), contact);
      notifyChanged();
    }
  }

  @Override
  public CharSequence getSummary() {
    long contactId = SharedState.getAlarmOperationsCenterContact(getContext());
    if (contactId != SharedState.EMPTY_CONTACT) {
      if (PERMISSION_CONTACTS_READ.isAllowed(getContext())) {
        return this.contactDao.getContact(contactId).orElse(unknownContact()).getName();
      }
      return String.valueOf(contactId);
    }
    return getContext().getString(R.string.contact_preference_empty_selection);
  }

  private Contact unknownContact() {
    return unknown(getContext().getString(R.string.contact_helper_unknown_contact));
  }

}

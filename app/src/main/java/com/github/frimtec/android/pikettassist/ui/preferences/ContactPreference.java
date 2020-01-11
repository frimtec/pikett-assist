package com.github.frimtec.android.pikettassist.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.service.dao.ContactDao;
import com.github.frimtec.android.pikettassist.state.SharedState;

import static com.github.frimtec.android.pikettassist.domain.Contact.unknown;
import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;

class ContactPreference extends RingtonePreference {


  private final ContactDao contactDao;

  public ContactPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.contactDao = new ContactDao(context);
  }

  @Override
  protected void onPrepareRingtonePickerIntent(Intent intent) {
    intent.setAction(Intent.ACTION_PICK);
    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (super.onActivityResult(requestCode, resultCode, data)) {
      if (data != null) {
        Uri contactUri = data.getData();
        Contact contact = this.contactDao.getContact(contactUri).orElse(unknownContact());
        persistString(String.valueOf(contact.getId()));
        setSummary(contact.getName());
        return true;
      }
    }
    return false;
  }

  @Override
  public CharSequence getSummary() {
    String summary = super.getSummary().toString();
    if (summary.contains("%s")) {
      summary = getValue(getPersistedString(String.valueOf(SharedState.EMPTY_CONTACT)));
    }
    return summary;
  }

  private String getValue(String preferenceValue) {
    if (!preferenceValue.isEmpty()) {
      if(PERMISSION_CONTACTS_READ.isAllowed(getContext())) {
        return this.contactDao.getContact(Long.parseLong(preferenceValue)).orElse(unknownContact()).getName();
      }
      return preferenceValue;
    }
    return getContext().getString(R.string.contact_preference_empty_selection);
  }

  private Contact unknownContact() {
    return unknown(getContext().getString(R.string.contact_helper_unknown_contact));
  }
}

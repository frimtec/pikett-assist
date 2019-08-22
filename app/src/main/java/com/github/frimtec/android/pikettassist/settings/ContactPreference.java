package com.github.frimtec.android.pikettassist.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

public class ContactPreference extends RingtonePreference {

  public ContactPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
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
        Contact contact = ContactHelper.getContact(getContext(), contactUri);
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
      return ContactHelper.getContact(getContext(), Long.parseLong(preferenceValue)).getName();
    }
    return getContext().getString(R.string.contact_preference_empty_selection);
  }


}

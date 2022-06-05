package com.github.frimtec.android.pikettassist.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.AttributeSet;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.service.OperationsCenterContactService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.takisoft.preferencex.PreferenceActivityResultListener;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class ContactPreference extends Preference implements PreferenceActivityResultListener {

  private final OperationsCenterContactService operationsCenterContactService;

  private ActivityResultLauncher<Intent> contactSelectionLauncher;

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

  public void initLauncher(Fragment fragment) {
    this.contactSelectionLauncher = fragment.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getData() != null) {
            Uri contactUri = result.getData().getData();
            Contact contact = this.operationsCenterContactService.getContactFromUri(contactUri);
            ApplicationPreferences.instance().setOperationsCenterContactReference(getContext(), contact.getReference());
            notifyChanged();
          }
        }
    );
  }

  @Override
  public void onPreferenceClick(@NonNull PreferenceFragmentCompat fragment, @NonNull Preference preference) {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    contactSelectionLauncher.launch(intent);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
    throw new IllegalStateException("Must not be called");
  }

  @Override
  public CharSequence getSummary() {
    Contact contact = this.operationsCenterContactService.getOperationsCenterContact();
    return contact.getName();
  }
}

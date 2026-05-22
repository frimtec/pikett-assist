package com.github.frimtec.android.pikettassist.ui.overview;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CONTACTS_READ;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.service.dao.ContactRepository;
import com.github.frimtec.android.pikettassist.ui.common.ContactPicker;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import java.util.concurrent.atomic.AtomicReference;

class PartnerState extends AbstractContactState {

  private static final int MENU_CONTEXT_VIEW_PARTNER = 1;
  private static final int MENU_CONTEXT_SELECT_PARTNER = 2;

  private final StateContext stateContext;
  private final String linkAlias;
  private final ContactPerson partner;
  private final Pair<ActivityResultLauncher<Intent>, AtomicReference<String>> phoneNumberSelectionLauncher;


  PartnerState(StateContext stateContext, String linkAlias, ContactPerson partner, Pair<ActivityResultLauncher<Intent>, AtomicReference<String>> phoneNumberSelectionLauncher) {
    super(
        R.drawable.ic_baseline_people_24,
        stateContext.getString(R.string.state_fragment_partner),
        partner.getFullName(),
        null,
        partner.getContactId() > 0 ? TrafficLight.GREEN : TrafficLight.YELLOW,
        partner
    );
    this.linkAlias = linkAlias;
    this.stateContext = stateContext;
    this.partner = partner;
    this.phoneNumberSelectionLauncher = phoneNumberSelectionLauncher;
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    if (!PERMISSION_CONTACTS_READ.isPermissionDeclared(context)) {
      if (partner.isValid()) {
        stateContext.addContextMenu(menu, MENU_CONTEXT_VIEW_PARTNER, R.string.list_item_menu_view);
      }
      stateContext.addContextMenu(menu, MENU_CONTEXT_SELECT_PARTNER, R.string.list_item_menu_select);
    }
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW_PARTNER -> {
        actionViewContact();
        return true;
      }
      case MENU_CONTEXT_SELECT_PARTNER -> {
        phoneNumberSelectionLauncher.second.set(linkAlias);
        phoneNumberSelectionLauncher.first.launch(ContactPicker.createContactPickerIntent(context));
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  @Override
  public void onClickAction(Context context) {
    if (partner.isValid()) {
      actionViewContact();
    } else {
      if (!PERMISSION_CONTACTS_READ.isPermissionDeclared(context)) {
        phoneNumberSelectionLauncher.second.set(linkAlias);
        phoneNumberSelectionLauncher.first.launch(ContactPicker.createContactPickerIntent(context));
      } else {
        ClipboardManager clipboard = (ClipboardManager) this.stateContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(stateContext.getString(R.string.state_fragment_partner_not_found_clipboard_label), partner.getNickname()));
        DialogHelper.infoDialog(
            context,
            R.string.partner_contact_unknown_info_title,
            R.string.partner_contact_unknown_info_text,
            (dialogInterface, integer) -> {
              Intent intent = new Intent(Intent.ACTION_VIEW);
              intent.setData(ContactsContract.Contacts.CONTENT_URI);
              stateContext.getContext().startActivity(intent);
            });
      }
    }
  }

  private void actionViewContact() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
      ContactRepository.create(stateContext.getContext()).getContact(partner.getContactId()).ifPresent(contact -> {
        Uri uri = ContactsContract.Contacts.getLookupUri(contact.reference().id(), contact.reference().lookupKey());
        intent.setData(uri);
        stateContext.getContext().startActivity(intent);
      });
    } else {
      Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(this.partner.getContactId()));
      intent.setData(uri);
      stateContext.getContext().startActivity(intent);
    }
  }

}

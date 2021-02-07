package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

public class PartnerState extends State {

  private final StateContext stateContext;
  private final ContactPerson partner;


  public PartnerState(StateContext stateContext, ContactPerson partner) {
    super(
        R.drawable.ic_baseline_people_24,
        stateContext.getString(R.string.state_fragment_partner),
        partner.getFullName(),
        null,
        partner.getContactId() > 0 ? TrafficLight.GREEN : TrafficLight.YELLOW
    );
    this.stateContext = stateContext;
    this.partner = partner;
  }

  @Override
  public void onClickAction(Context context) {
    if (partner.isValid()) {
      actionViewContact();
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

  private void actionViewContact() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(this.partner.getContactId()));
    intent.setData(uri);
    stateContext.getContext().startActivity(intent);
  }

}

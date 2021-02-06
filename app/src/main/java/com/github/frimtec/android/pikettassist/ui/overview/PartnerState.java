package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.ContactPerson;

public class PartnerState extends State {

  private final StateContext stateContext;
  private final ContactPerson partner;


  public PartnerState(StateContext stateContext, ContactPerson partner) {
    super(
        R.drawable.ic_baseline_people_24,
        stateContext.getString(R.string.state_fragment_partner),
        partner.getFullName(),
        null,
        partner.getId() > 0 ? TrafficLight.GREEN : TrafficLight.YELLOW
    );
    this.stateContext = stateContext;
    this.partner = partner;
  }

  @Override
  public void onClickAction(Context context) {
    if (partner.isValid()) {
      actionViewContact();
    } else {
      // TODO: 06.02.2021 Show instruction
      ClipboardManager clipboard = (ClipboardManager) this.stateContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip = ClipData.newPlainText(stateContext.getString(R.string.state_fragment_partner_not_found_clipboard_label), partner.getFullName());
      clipboard.setPrimaryClip(clip);
    }
  }

  private void actionViewContact() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(this.partner.getId()));
    intent.setData(uri);
    stateContext.getContext().startActivity(intent);
  }

}

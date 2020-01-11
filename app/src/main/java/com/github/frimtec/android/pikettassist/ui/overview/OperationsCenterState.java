package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Contact;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.ui.common.DialogHelper;

import static com.github.frimtec.android.pikettassist.ui.overview.StateFragment.REQUEST_CODE_SELECT_PHONE_NUMBER;

class OperationsCenterState extends State {

  private static final int MENU_CONTEXT_VIEW_OPERATIONS_CENTER_ID = 1;
  private static final int MENU_CONTEXT_SELECT_OPERATIONS_CENTER_ID = 2;
  private static final int MENU_CONTEXT_CLEAR_OPERATIONS_CENTER_ID = 3;

  private final StateContext stateContext;
  private final Contact operationCenter;

  OperationsCenterState(StateContext stateContext) {
    super(R.drawable.ic_phone_black_24dp, stateContext.getString(R.string.state_fragment_operations_center), stateContext.getOperationCenter().getName(), null, stateContext.getOperationCenter().isValid() ? TrafficLight.GREEN : TrafficLight.RED);
    this.stateContext = stateContext;
    this.operationCenter = stateContext.getOperationCenter();
  }

  @Override
  public void onClickAction(Context context) {
    long alarmOperationsCenterContact = SharedState.getAlarmOperationsCenterContact(context);
    if (operationCenter.isValid()) {
      actionViewContact(alarmOperationsCenterContact);
    } else {
      actionSelectContact();
    }
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    if (operationCenter.isValid()) {
      menu.add(Menu.NONE, MENU_CONTEXT_VIEW_OPERATIONS_CENTER_ID, Menu.NONE, R.string.list_item_menu_view);
    }
    menu.add(Menu.NONE, MENU_CONTEXT_SELECT_OPERATIONS_CENTER_ID, Menu.NONE, R.string.list_item_menu_select);
    menu.add(Menu.NONE, MENU_CONTEXT_CLEAR_OPERATIONS_CENTER_ID, Menu.NONE, R.string.list_item_menu_clear);
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW_OPERATIONS_CENTER_ID:
        actionViewContact(operationCenter.getId());
        return true;
      case MENU_CONTEXT_SELECT_OPERATIONS_CENTER_ID:
        actionSelectContact();
        return true;
      case MENU_CONTEXT_CLEAR_OPERATIONS_CENTER_ID:
        DialogHelper.areYouSure(stateContext.getContext(), (dialog, which) -> {
          SharedState.setAlarmOperationsCenterContact(context, Contact.unknown(context.getString(R.string.contact_helper_unknown_contact)));
          stateContext.refreshFragment();
        }, (dialog, which) -> {
        });
        return true;
      default:
        return false;
    }
  }

  private void actionSelectContact() {
    Intent intent = new Intent(Intent.ACTION_PICK);
    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
    stateContext.startActivityForResultAction(intent, REQUEST_CODE_SELECT_PHONE_NUMBER);
  }

  private void actionViewContact(long alarmOperationsCenterContact) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(alarmOperationsCenterContact));
    intent.setData(uri);
    stateContext.getContext().startActivity(intent);
  }
}

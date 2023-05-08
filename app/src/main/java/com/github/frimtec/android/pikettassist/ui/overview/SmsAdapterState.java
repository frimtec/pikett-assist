package com.github.frimtec.android.pikettassist.ui.overview;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.S2MSP_PACKAGE_NAME;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation.AppCompatibility;

class SmsAdapterState extends State {

  private static final String F_DROID_PACKAGE_NAME = "org.fdroid.fdroid";

  private static final int MENU_CONTEXT_VIEW = 1;
  private static final int SEND_TEST_SMS = 2;

  private final StateContext stateContext;
  private final boolean smsAdapterSmsPermission;
  private final PackageManager packageManager;

  SmsAdapterState(StateContext stateContext) {
    super(
        R.drawable.ic_message_24dp,
        stateContext.getString(R.string.state_fragment_sms_adapter),
        getSmsAdapterValue(stateContext, stateContext.isSmsAdapterPermissionsGranted()),
        null,
        getSmsAdapterState(stateContext, stateContext.isSmsAdapterPermissionsGranted()));
    this.stateContext = stateContext;
    this.smsAdapterSmsPermission = stateContext.isSmsAdapterPermissionsGranted();
    this.packageManager = stateContext.getContext().getPackageManager();
  }

  private static State.TrafficLight getSmsAdapterState(StateContext stateContext, boolean smsAdapterSmsPermission) {
    AppCompatibility compatibility = getAppCompatibility(stateContext);
    if (!compatibility.isSupported() || !smsAdapterSmsPermission || stateContext.isOperationsCenterPhoneNumbersBlocked()) {
      return RED;
    } else if (compatibility == AppCompatibility.UPDATE_RECOMMENDED) {
      return YELLOW;
    } else {
      return GREEN;
    }
  }

  private static AppCompatibility getAppCompatibility(StateContext stateContext) {
    return stateContext.getSmsAdapterInstallation().getAppCompatibility();
  }

  private static String getSmsAdapterValue(StateContext stateContext, boolean smsAdapterSmsPermission) {
    AppCompatibility compatibility = getAppCompatibility(stateContext);
    if (compatibility == AppCompatibility.NOT_INSTALLED) {
      // RED
      return stateContext.getString(R.string.state_fragment_sms_adapter_not_installed);
    } else if (!smsAdapterSmsPermission) {
      // RED
      return stateContext.getString(R.string.state_fragment_sms_adapter_no_sms_permissions);
    } else if (stateContext.isOperationsCenterPhoneNumbersBlocked()) {
      // RED
      return stateContext.getString(R.string.state_fragment_phone_numbers_blocked);
    } else {
      switch (compatibility) {
        case NOT_YET_SUPPORTED:
          // RED
          return stateContext.getString(R.string.state_fragment_sms_adapter_not_yet_supported)
              + "\n" + getVersionUpdate(stateContext);
        case NO_MORE_SUPPORTED:
          // RED
          return stateContext.getString(R.string.state_fragment_s2msp_requires_update)
              + "\n" + getVersionUpdate(stateContext);
        case UPDATE_RECOMMENDED:
          // YELLOW
          return stateContext.getString(R.string.state_fragment_sms_adapter_update_recommended)
              + "\n" + getVersionUpdate(stateContext);
      }
      // GREEN
      return "S2MSP V" + getAppVersion(stateContext);
    }
  }

  private static String getAppVersion(StateContext stateContext) {
    return stateContext.getSmsAdapterInstallation().getAppVersion().orElse("?.?");
  }

  private static String getVersionUpdate(StateContext stateContext) {
    return String.format("Version %s -> %s", getAppVersion(stateContext), stateContext.getSmsAdapterInstallation().getApiVersion());
  }

  @Override
  public void onClickAction(Context context) {
    AppCompatibility appCompatibility = getAppCompatibility(stateContext);
    if (appCompatibility == AppCompatibility.NOT_INSTALLED) {
      openDownloadDialog(context, R.string.permission_sms_text, R.string.permission_sms_title, stateContext.getSmsAdapterInstallation());
      return;
    }
    if (smsAdapterSmsPermission) {
      if (stateContext.isOperationsCenterPhoneNumbersBlocked()) {
        stateContext.registerPhoneNumberOnSmsAdapter();
        return;
      }
      if (appCompatibility == AppCompatibility.UPDATE_RECOMMENDED || appCompatibility == AppCompatibility.NO_MORE_SUPPORTED) {
        openDownloadDialog(context, R.string.permission_sms_update_text, R.string.permission_sms_update_title, stateContext.getSmsAdapterInstallation());
        return;
      }
    }
    Intent launchIntent = packageManager.getLaunchIntentForPackage(S2MSP_PACKAGE_NAME);
    if (launchIntent != null) {
      stateContext.getContext().startActivity(launchIntent);
    }
  }

  private void openDownloadDialog(Context context, @StringRes int message, @StringRes int title, Installation installation) {
    if (isFDroidAvailable(context)) {
      SpannableString htmlMessage = new SpannableString(Html.fromHtml(context.getString(message) + "<br><br>" + context.getString(R.string.install_with_fdroid), Html.FROM_HTML_MODE_COMPACT));
      AlertDialog alertDialog = new FDroidSmsAdapterInstallDialog(context, htmlMessage, title);
      alertDialog.show();
      enableLinks(alertDialog);
    } else {
      // let the browser handle the stuff
      SpannableString htmlMessage = new SpannableString(Html.fromHtml(context.getString(message) + context.getString(R.string.install_with_browser), Html.FROM_HTML_MODE_COMPACT));
      AlertDialog alertDialog = new AlertDialog.Builder(context)
          // set dialog message
          .setTitle(title)
          .setMessage(htmlMessage)
          .setCancelable(true)
          .setPositiveButton(R.string.general_download, (dialog, which) -> {
            Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, installation.getDownloadLink());
            openBrowserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openBrowserIntent);
          })
          .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
          }).create();
      alertDialog.show();
      enableLinks(alertDialog);
    }
  }

  private static class FDroidSmsAdapterInstallDialog extends AlertDialog {

    public FDroidSmsAdapterInstallDialog(Context context, SpannableString htmlMessage, @StringRes int title) {
      super(context);
      setTitle(title);
      setMessage(htmlMessage);
      setCancelable(true);
      setButton(AlertDialog.BUTTON_POSITIVE, getContext().getString(R.string.general_install), (dialog, which) -> context.startActivity(
          Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.github.frimtec.android.securesmsproxy")), "Select F-Droid"))
      );
      setButton(AlertDialog.BUTTON_NEGATIVE, getContext().getString(android.R.string.cancel), (dialog, which) -> {
      });
    }

  }

  private boolean isFDroidAvailable(Context context) {
    try {
      context.getPackageManager().getPackageInfo(F_DROID_PACKAGE_NAME, 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    super.onCreateContextMenu(context, menu);
    boolean present = stateContext.getSmsAdapterInstallation().getAppVersion().isPresent();
    stateContext.addContextMenu(menu, MENU_CONTEXT_VIEW, R.string.list_item_menu_view)
        .setEnabled(present);
    stateContext.addContextMenu(menu, SEND_TEST_SMS, R.string.list_item_menu_send_test_sms)
        .setEnabled(present && !TextUtils.isEmpty(ApplicationState.instance().getSmsAdapterSecret()));
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW:
        Intent launchIntent = packageManager.getLaunchIntentForPackage(S2MSP_PACKAGE_NAME);
        if (launchIntent != null) {
          stateContext.getContext().startActivity(launchIntent);
        }
        return true;
      case SEND_TEST_SMS:
        stateContext.sendLoopbackSms();
        Toast.makeText(context, R.string.state_fragment_loopback_sms_sent, Toast.LENGTH_SHORT).show();
        return true;
      default:
        return false;
    }
  }

  private void enableLinks(AlertDialog alertDialog) {
    TextView textView = alertDialog.findViewById(android.R.id.message);
    if (textView != null) {
      textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
  }
}

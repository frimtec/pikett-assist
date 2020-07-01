package com.github.frimtec.android.pikettassist.ui.overview;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.ApplicationState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.Installation;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;
import static com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade.S2MSP_PACKAGE_NAME;

class SmsAdapterState extends State {

  private static final String F_DROID_PACKAGE_NAME = "org.fdroid.fdroid";

  private static final int MENU_CONTEXT_VIEW = 1;
  private static final int SEND_TEST_SMS = 2;

  private final StateContext stateContext;
  private final boolean smsAdapterSmsPermission;
  private final PackageManager packageManager;

  SmsAdapterState(StateContext stateContext) {
    super(
        R.drawable.ic_message_black_24dp,
        stateContext.getString(R.string.state_fragment_sms_adapter),
        getSmsAdapterValue(stateContext, stateContext.isSmsAdapterPermissionsGranted()),
        null,
        getSmsAdapterState(stateContext, stateContext.isSmsAdapterPermissionsGranted()));
    this.stateContext = stateContext;
    this.smsAdapterSmsPermission = stateContext.isSmsAdapterPermissionsGranted();
    this.packageManager = stateContext.getContext().getPackageManager();
  }

  private static State.TrafficLight getSmsAdapterState(StateContext stateContext, boolean smsAdapterSmsPermission) {
    if (stateContext.isSmsAdapterMissing() || !smsAdapterSmsPermission || stateContext.isOperationsCenterPhoneNumbersBlocked()) {
      return RED;
    } else if (stateContext.isSmsAdapterVersionOutdated()) {
      return YELLOW;
    } else {
      return GREEN;
    }
  }

  private static String getSmsAdapterValue(StateContext stateContext, boolean smsAdapterSmsPermission) {
    if (stateContext.isSmsAdapterMissing()) {
      return stateContext.getString(R.string.state_fragment_sms_adapter_not_installed);
    } else if (!smsAdapterSmsPermission) {
      return stateContext.getString(R.string.state_fragment_sms_adapter_no_sms_permissions);
    } else if (stateContext.isOperationsCenterPhoneNumbersBlocked()) {
      return stateContext.getString(R.string.state_fragment_phone_numbers_blocked);
    } else if (stateContext.isSmsAdapterVersionOutdated()) {
      return stateContext.getString(R.string.state_fragment_s2msp_requires_update);
    } else {
      return "S2MSP V" + stateContext.getSmsAdapterInstallation().getAppVersion().orElse("?.?");
    }
  }

  @Override
  public void onClickAction(Context context) {
    if (stateContext.isSmsAdapterMissing()) {
      openDownloadDialog(context, R.string.permission_sms_text, R.string.permission_sms_title, stateContext.getSmsAdapterInstallation());
      return;
    }
    if (smsAdapterSmsPermission) {
      if (stateContext.isOperationsCenterPhoneNumbersBlocked()) {
        stateContext.registerPhoneNumberOnSmsAdapter();
        return;
      }
      if (stateContext.isSmsAdapterVersionOutdated()) {
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
      SpannableString htmlMessage = new SpannableString(Html.fromHtml(context.getString(message) + "<br><br>" + context.getString(R.string.install_from_fdroid), Html.FROM_HTML_MODE_COMPACT));
      AlertDialog alertDialog = new AlertDialog.Builder(context)
          // set dialog message
          .setTitle(title)
          .setMessage(htmlMessage)
          .setCancelable(true)
          .setPositiveButton(R.string.general_install, (dialog, which) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.github.frimtec.android.securesmsproxy"))))
          .setNeutralButton(R.string.add_repo, (dialog, which) -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://apt.izzysoft.de/fdroid/repo?fingerprint=3BF0D6ABFEAE2F401707B6D966BE743BF0EEE49C2561B9BA39073711F628937A"))))
          .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
          }).create();
      alertDialog.show();
      enableLinks(alertDialog);
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      // let the browser handle the stuff
      SpannableString htmlMessage = new SpannableString(Html.fromHtml(context.getString(message), Html.FROM_HTML_MODE_COMPACT));
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
          .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
          }).create();
      alertDialog.show();
      enableLinks(alertDialog);
    } else {
      if (!context.getPackageManager().canRequestPackageInstalls()) {
        SpannableString htmlMessage = new SpannableString(Html.fromHtml(context.getString(message) + "<br><br>" + context.getString(R.string.permission_sms_text_unknown_source_request), Html.FROM_HTML_MODE_COMPACT));
        AlertDialog alertDialog = new AlertDialog.Builder(context)
            // set dialog message
            .setTitle(title)
            .setMessage(htmlMessage)
            .setCancelable(true)
            .setPositiveButton(stateContext.getString(R.string.s2msp_settings), (dialog, which) ->
                stateContext.getContext().startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:com.github.frimtec.android.pikettassist"))))
            .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
            }).create();
        alertDialog.show();
        enableLinks(alertDialog);
      } else {
        SpannableString htmlMessage = new SpannableString(Html.fromHtml(stateContext.getString(R.string.s2msp_download_request), Html.FROM_HTML_MODE_COMPACT));
        AlertDialog alertDialog = new AlertDialog.Builder(context)
            // set dialog message
            .setTitle(title)
            .setMessage(htmlMessage)
            .setCancelable(true)
            .setPositiveButton(R.string.general_download, (dialog, which) -> {
              DownloadManager.Request request = new DownloadManager.Request(installation.getDownloadLink());
              request.setTitle("S2MSP version " + installation.getApiVersion());
              request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
              request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, String.format("s2msp-app-%s.apk", installation.getApiVersion()));
              DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
              if (manager != null) {
                manager.enqueue(request);
                Toast.makeText(context, R.string.s2msp_download_started, Toast.LENGTH_LONG).show();
              }
            })
            .setNegativeButton(R.string.general_cancel, (dialog, which) -> {
            }).create();
        alertDialog.show();
        enableLinks(alertDialog);
      }
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
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW, Menu.NONE, R.string.list_item_menu_view)
        .setEnabled(present);
    menu.add(Menu.NONE, SEND_TEST_SMS, Menu.NONE, R.string.list_item_menu_send_test_sms)
        .setEnabled(present && !TextUtils.isEmpty(ApplicationState.getSmsAdapterSecret()));
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

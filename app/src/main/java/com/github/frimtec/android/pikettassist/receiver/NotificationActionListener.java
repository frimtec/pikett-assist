package com.github.frimtec.android.pikettassist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.AlarmService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_UPDATE_NOW;
import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.UPDATE_NOTIFICATION_ID;

public class NotificationActionListener extends BroadcastReceiver {

  private static final String TAG = "NotificationActionListener";

  public static final String EXTRA_VERSION_NAME = "version_name";
  public static final String EXTRA_DOWNLOAD_URL = "download_url";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case ACTION_CLOSE_ALARM:
          new AlarmService(context).closeAlarm();
          break;
        case ACTION_UPDATE_NOW:
          NotificationHelper.cancelNotification(context, UPDATE_NOTIFICATION_ID);
          Bundle intentExtras = intent.getExtras();
          if (intentExtras != null) {
            String versionName = intentExtras.getString(EXTRA_VERSION_NAME);
            try {
              String url = intentExtras.getString(EXTRA_DOWNLOAD_URL);
              if (url != null) {
                Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                openBrowserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(openBrowserIntent);
              }
            } catch (Exception e) {
              Log.e(TAG, "Cannot parse download URI for version " + versionName, e);
              return;
            }
          }
          break;
        default:
          Log.e(TAG, "Unknown action: " + action);
      }
    }
  }
}

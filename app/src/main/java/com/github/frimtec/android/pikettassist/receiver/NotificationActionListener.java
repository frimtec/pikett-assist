package com.github.frimtec.android.pikettassist.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.service.AlarmService;
import com.github.frimtec.android.pikettassist.service.GitHubService;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_UPDATE_NOW;
import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.UPDATE_NOTIFICATION_ID;

public class NotificationActionListener extends BroadcastReceiver {

  private static final String TAG = "NotificationActionListener";

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
          Log.i(TAG, "Update now!");

          GitHubService.getInstance(context).loadLatestRelease(context, (context1, release) -> {
            String url = release.getApkUrl();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Download PAssist version " + release.getName());
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, String.format("passist-app-%s.apk", release.getName()));
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = manager.enqueue(request);
            Log.i(TAG, "Start download: " + downloadId);
          });
          break;
        default:
          Log.e(TAG, "Unknown action: " + action);
      }
    }
  }
}

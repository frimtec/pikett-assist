package com.github.frimtec.android.pikettassist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsListener extends BroadcastReceiver {

  private SharedPreferences preferences;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
      SmsMessage[] msgs = null;
      String msg_from;
      if (bundle != null) {
        try {
          Object[] pdus = (Object[]) bundle.get("pdus");
          msgs = new SmsMessage[pdus.length];
          for (int i = 0; i < msgs.length; i++) {
            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            msg_from = msgs[i].getOriginatingAddress();
            String msgBody = msgs[i].getMessageBody();
            Log.d("SMS received", msg_from + ":" + msgBody);

            SmsManager smgr = SmsManager.getDefault();
            smgr.sendTextMessage(msg_from,null,"OK",null,null);
          }
        } catch (Exception e) {
          Log.d("Exception caught", e.getMessage());
        }
      }
    }
  }
}
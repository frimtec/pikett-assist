package com.github.frimtec.android.pikettassist.helper;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.Sms;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SmsHelper {

  private static final String TAG = "SmsHelper";

  private static final String CONFIRMATION_MESSAGE = "OK";

  private SmsHelper() {
  }

  public static List<Sms> getSmsFromIntent(Intent intent) {
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      return Arrays.stream(pdus)
          .map(pdu -> {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            Sms sms = new Sms(message.getOriginatingAddress(), message.getMessageBody());
            Log.d(TAG, "SMS recived: " + sms);
            return sms;
          }).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


  public static void confimSms(String number) {
    SmsManager smgr = SmsManager.getDefault();
    smgr.sendTextMessage(number, null, CONFIRMATION_MESSAGE, null, null);
  }

}

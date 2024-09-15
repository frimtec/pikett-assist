package com.github.frimtec.android.pikettassist.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.github.frimtec.android.pikettassist.domain.AlertConfirmMethod;
import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

class AcknowledgmentServiceTest {

  @Test
  void acknowledgeForMethodNoAcknowledgement() {
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    Context context = mock(Context.class);
    SmsService smsService = mock(SmsService.class);
    //noinspection unchecked
    Consumer<Context> notifyHandlerSkipAcknowledge = mock(Consumer.class);
    AcknowledgmentService acknowledgmentService = new AcknowledgmentService(
        context,
        smsService,
        applicationPreferences,
        notifyHandlerSkipAcknowledge
    );
    when(applicationPreferences.getAlertConfirmMethod(context)).thenReturn(AlertConfirmMethod.NO_ACKNOWLEDGE);

    acknowledgmentService.acknowledge(new Sms("111", "text"));

    verifyNoInteractions(smsService);
    verifyNoInteractions(notifyHandlerSkipAcknowledge);
  }

  @Test
  void acknowledgeForMethodSmsStaticText() {
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    Context context = mock(Context.class);
    SmsService smsService = mock(SmsService.class);
    //noinspection unchecked
    Consumer<Context> notifyHandlerSkipAcknowledge = mock(Consumer.class);
    AcknowledgmentService acknowledgmentService = new AcknowledgmentService(
        context,
        smsService,
        applicationPreferences,
        notifyHandlerSkipAcknowledge
    );
    when(applicationPreferences.getAlertConfirmMethod(context)).thenReturn(AlertConfirmMethod.SMS_STATIC_TEXT);
    when(applicationPreferences.getSmsConfirmText(context)).thenReturn("OK");

    acknowledgmentService.acknowledge(new Sms("111", "text", 1));

    verify(smsService).sendSms("OK", "111", 1);
    verifyNoInteractions(notifyHandlerSkipAcknowledge);
  }

  @Test
  void acknowledgeForMethodSmsDynamicText() {
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    Context context = mock(Context.class);
    SmsService smsService = mock(SmsService.class);
    //noinspection unchecked
    Consumer<Context> notifyHandlerSkipAcknowledge = mock(Consumer.class);
    AcknowledgmentService acknowledgmentService = new AcknowledgmentService(
        context,
        smsService,
        applicationPreferences,
        notifyHandlerSkipAcknowledge
    );
    when(applicationPreferences.getAlertConfirmMethod(context)).thenReturn(AlertConfirmMethod.SMS_DYNAMIC_TEXT);
    when(applicationPreferences.getSmsConfirmPattern(context)).thenReturn("<(.*?)>");

    acknowledgmentService.acknowledge(new Sms("111", "reply with <123> to acknowledge", 1));

    verify(smsService).sendSms("123", "111", 1);
    verifyNoInteractions(notifyHandlerSkipAcknowledge);
  }

  @Test
  void acknowledgeForMethodSmsDynamicTextWithNoMatch() {
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    Context context = mock(Context.class);
    SmsService smsService = mock(SmsService.class);
    //noinspection unchecked
    Consumer<Context> notifyHandlerSkipAcknowledge = mock(Consumer.class);
    AcknowledgmentService acknowledgmentService = new AcknowledgmentService(
        context,
        smsService,
        applicationPreferences,
        notifyHandlerSkipAcknowledge
    );
    when(applicationPreferences.getAlertConfirmMethod(context)).thenReturn(AlertConfirmMethod.SMS_DYNAMIC_TEXT);
    when(applicationPreferences.getSmsConfirmPattern(context)).thenReturn("<(.*?)>");

    acknowledgmentService.acknowledge(new Sms("111", "reply with (123) to acknowledge", 1));

    verifyNoInteractions(smsService);
    verify(notifyHandlerSkipAcknowledge).accept(context);
  }
}
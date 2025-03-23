package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.domain.AlertConfirmMethod.INTERNET_FACT24_ENS;
import static com.github.frimtec.android.pikettassist.domain.AlertConfirmMethod.NO_ACKNOWLEDGE;
import static com.github.frimtec.android.pikettassist.domain.AlertConfirmMethod.SMS_DYNAMIC_TEXT;
import static com.github.frimtec.android.pikettassist.domain.AlertConfirmMethod.SMS_STATIC_TEXT;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlertConfirmMethod;
import com.github.frimtec.android.pikettassist.service.confirmation.ConfirmStrategy;
import com.github.frimtec.android.pikettassist.service.confirmation.DynamicSmsConfirmStrategy;
import com.github.frimtec.android.pikettassist.service.confirmation.NoConfirmStrategy;
import com.github.frimtec.android.pikettassist.service.confirmation.StaticSmsConfirmStrategy;
import com.github.frimtec.android.pikettassist.service.confirmation.fact24ens.Fact24EnsConfirmStrategy;
import com.github.frimtec.android.pikettassist.service.system.SmsService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class AcknowledgmentService {

  private static final String TAG = AcknowledgmentService.class.getSimpleName();

  private final Context context;
  private final ApplicationPreferences applicationPreferences;
  private final Consumer<Context> notifyHandlerSkipAcknowledge;
  private final Executor uiExecutor;

  private final Map<AlertConfirmMethod, ConfirmStrategy> confirmationStrategies;

  public AcknowledgmentService(Context context, SmsService smsService) {
    this(
        context,
        smsService,
        ApplicationPreferences.instance(),
        ContextCompat.getMainExecutor(context),
        ctx -> Toast.makeText(
            ctx,
            ctx.getString(R.string.toast_acknowledge_skipped),
            Toast.LENGTH_LONG
        ).show()
    );
  }

  AcknowledgmentService(
      Context context,
      SmsService smsService,
      ApplicationPreferences applicationPreferences,
      Executor uiExecutor,
      Consumer<Context> notifyHandlerSkipAcknowledge
  ) {
    this.context = context;
    this.applicationPreferences = applicationPreferences;
    this.notifyHandlerSkipAcknowledge = notifyHandlerSkipAcknowledge;
    this.uiExecutor = uiExecutor;
    this.confirmationStrategies = Map.of(
        NO_ACKNOWLEDGE, new NoConfirmStrategy(),
        SMS_STATIC_TEXT, new StaticSmsConfirmStrategy(
            smsService,
            () -> this.applicationPreferences.getSmsConfirmText(context)
        ),
        SMS_DYNAMIC_TEXT, new DynamicSmsConfirmStrategy(
            smsService,
            () -> this.applicationPreferences.getSmsConfirmPattern(context)
        ),
        INTERNET_FACT24_ENS, new Fact24EnsConfirmStrategy()
    );
  }

  public void acknowledge(List<Sms> receivedSms) {
    var confirmationStrategy = getConfirmationStrategy(
        this.applicationPreferences.getAlertConfirmMethod(context)
    );
    CompletableFuture.supplyAsync(() -> confirmationStrategy.confirm(receivedSms))
        .thenAcceptAsync(
            success -> {
              if (success == Boolean.FALSE) {
                Log.w(TAG, "Acknowledgement failed");
                notifyHandlerSkipAcknowledge.accept(context);
              }
            },
            uiExecutor
        );
  }

  private ConfirmStrategy getConfirmationStrategy(AlertConfirmMethod alertConfirmMethod) {
    return Objects.requireNonNull(
        confirmationStrategies.getOrDefault(
            alertConfirmMethod,
            receivedAlarmSms -> {
              Log.w(TAG, "No confirmation strategy found for method: " + alertConfirmMethod);
              return false;
            }
        )
    );
  }
}

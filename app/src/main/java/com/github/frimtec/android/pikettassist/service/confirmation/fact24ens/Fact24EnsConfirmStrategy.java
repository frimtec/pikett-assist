package com.github.frimtec.android.pikettassist.service.confirmation.fact24ens;

import android.util.Log;

import com.github.frimtec.android.pikettassist.service.confirmation.ConfirmStrategy;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class Fact24EnsConfirmStrategy implements ConfirmStrategy {

  private static final String TAG = Fact24EnsConfirmStrategy.class.getSimpleName();

  private static final String API_BASE_URL = "https://ensplus.fact24.com";
  private static final String FEEDBACK_STATUS_TIME_CONFIRMED = "TIME_CONFIRMED";
  private static final int STANDARD_CONFIRM_TIME_MINUTES = 5;

  private static final Fact24EnsConfirmService SERVICE = buildFact24EnsService(API_BASE_URL);

  static Fact24EnsConfirmService buildFact24EnsService(String apiBaseUrl) {
    return new Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Fact24EnsConfirmService.class);
  }

  private final Pattern TOKEN_EXTRACT_PATTERN = Pattern.compile(
      "https://ensplus\\.fact24\\.com/ens/#/c\\?t=([-\\da-z]+)"
  );

  private final Fact24EnsConfirmService service;

  public Fact24EnsConfirmStrategy() {
    this(SERVICE);
  }

  Fact24EnsConfirmStrategy(Fact24EnsConfirmService service) {
    this.service = service;
  }

  @Override
  public boolean confirm(Sms receivedAlarmSms) {
    Matcher matcher = this.TOKEN_EXTRACT_PATTERN.matcher(receivedAlarmSms.getText());
    if (matcher.find()) {
      String token = matcher.group(1);
      try {
        Call<AlarmUseCase> request = this.service.getAlarmUseCase(token);
        Response<AlarmUseCase> response = request.execute();
        if (!response.isSuccessful()) {
          Log.w(TAG, "getAlarmUseCase failed: " + response.code());
          return false;
        }
        AlarmUseCase useCase = response.body();
        if (useCase == null) {
          Log.e(TAG, "getAlarmUseCase failed: useCase is null");
          return false;
        }
        response = this.service.putFeedback(
            useCase.requestId(),
            new Feedback(
                FEEDBACK_STATUS_TIME_CONFIRMED,
                STANDARD_CONFIRM_TIME_MINUTES
            )
        ).execute();
        if (!response.isSuccessful()) {
          Log.w(TAG, "putFeedback failed: " + response.code());
          return false;
        }
        return true;
      } catch (IOException e) {
        Log.e(TAG, "Confirmation failed due to IOException", e);
        return false;
      }
    } else {
      Log.w(TAG, "No token found in SMS");
      return false;
    }
  }
}

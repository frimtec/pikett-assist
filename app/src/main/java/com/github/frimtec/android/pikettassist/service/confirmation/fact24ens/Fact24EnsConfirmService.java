package com.github.frimtec.android.pikettassist.service.confirmation.fact24ens;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

interface Fact24EnsConfirmService {

  @GET("/ens/api/alarm/alarm-use-case/{token}")
  Call<AlarmUseCase> getAlarmUseCase(@Path("token") String token);

  @PUT("/ens/api/alarm/feedback/{requestId}")
  Call<AlarmUseCase> putFeedback(@Path("requestId") String requestId, @Body Feedback feedback);
}
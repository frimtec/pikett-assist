package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.BiConsumer;

public class GitHubService {

  private final static String TAG = "GitHubService";

  private static final String LATEST_VERSION_URL = "https://api.github.com/repos/frimtec/pikett-assist/releases/latest";
  private static final String APK_ASSET_NAME = "app-release.apk";

  private static GitHubService instance;
  private final RequestQueue requestQueue;

  public static class Release {

    private final long id;
    private final String name;
    private final String apkUrl;

    private Release(long id, String name, String apkUrl) {
      this.id = id;
      this.name = name;
      this.apkUrl = apkUrl;
    }

    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getApkUrl() {
      return apkUrl;
    }
  }

  private GitHubService(Context context) {
    this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
  }

  public static synchronized GitHubService getInstance(Context context) {
    if (instance == null) {
      instance = new GitHubService(context);
    }
    return instance;
  }

  public void loadLatestRelease(Context context, BiConsumer<Context, Release> releaseHandler) {
    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
        (Request.Method.GET, LATEST_VERSION_URL, null,
            response -> releaseHandler.accept(context, extractLatestRelease(response)),
            error -> Log.e(TAG, "Latest version request failed: " + error.getLocalizedMessage(), error.getCause()));
    this.requestQueue.add(jsonObjectRequest);
  }

  private Release extractLatestRelease(JSONObject response) {
    return new Release(
        response.optLong("id", -1),
        response.optString("name", "0.0.0"),
        getApkDownloadUrl(response.optJSONArray("assets"))
    );
  }

  private String getApkDownloadUrl(JSONArray assets) {
    if (assets != null) {
      for (int i = 0; i < assets.length(); i++) {
        JSONObject asset = assets.optJSONObject(i);
        if (APK_ASSET_NAME.equals(asset.optString("name"))) {
          return asset.optString("browser_download_url");
        }
      }
    }
    return "N/A";
  }
}

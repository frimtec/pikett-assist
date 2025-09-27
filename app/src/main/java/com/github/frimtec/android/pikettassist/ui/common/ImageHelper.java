package com.github.frimtec.android.pikettassist.ui.common;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.Optional;

public class ImageHelper {

  private static final String TAG = "ImageHelper";

  public static Optional<Bitmap> uriToBitmap(ContentResolver contentResolver, @Nullable Uri imageUri) {
    if (imageUri != null) {
      try (InputStream inputStream = contentResolver.openInputStream(imageUri)) {
        if (inputStream != null) {
          return Optional.of(BitmapFactory.decodeStream(inputStream));
        } else {
          Log.w(TAG, "Cannot load image: " + imageUri);
        }
      } catch (Exception e) {
        Log.w(TAG, "Cannot load image: " + imageUri, e);
      }
    }
    return Optional.empty();
  }
}

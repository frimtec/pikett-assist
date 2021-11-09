package com.github.frimtec.android.pikettassist.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class GsonHelper {

  public static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(new TypeToken<Instant>() {
      }.getType(), new InstantConverter())
      .create();

  private static class InstantConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(FORMATTER.format(src));
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return FORMATTER.parse(json.getAsString(), Instant::from);
    }
  }
}

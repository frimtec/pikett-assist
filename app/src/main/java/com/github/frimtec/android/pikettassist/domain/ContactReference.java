package com.github.frimtec.android.pikettassist.domain;

public record ContactReference(long id, String lookupKey) {

  public static final ContactReference NO_SELECTION = ContactReference.fromSerializedString("-1;NOT_SET");

  public ContactReference(long id, String lookupKey) {
    this.id = id;
    this.lookupKey = lookupKey != null ? lookupKey : "";
  }

  public static ContactReference fromSerializedString(String serializedReference) {
    int separatorIndex = serializedReference.indexOf(";");
    if (separatorIndex > 0) {
      String lookupKey = serializedReference.substring(separatorIndex + 1);
      return new ContactReference(Long.parseLong(serializedReference.substring(0, separatorIndex)), lookupKey.length() > 0 ? lookupKey : null);
    }
    return new ContactReference(Long.parseLong(serializedReference), null);
  }

  public boolean isComplete() {
    return !lookupKey.isEmpty();
  }

  public String getSerializedString() {
    return id + ";" + lookupKey;
  }
}

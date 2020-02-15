package com.github.frimtec.android.pikettassist.domain;

public class ContactReference {

  public static final ContactReference NO_SELECTION = ContactReference.fromSerializedString("-1;NOT_SET");

  private final long id;
  private final String lookupKey;

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

  public long getId() {
    return id;
  }

  public String getLookupKey() {
    return lookupKey;
  }

  public String getSerializedString() {
    return id + ";" + lookupKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ContactReference reference = (ContactReference) o;

    if (id != reference.id) {
      return false;
    }
    return lookupKey.equals(reference.lookupKey);
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + lookupKey.hashCode();
    return result;
  }
}

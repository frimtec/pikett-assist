package com.github.frimtec.android.pikettassist.domain;

import androidx.annotation.Nullable;

public class ContactPerson extends BaseContact {

  private static final long INVALID_CONTACT_ID = -1;

  private final String nickname;
  private final long contactId;
  private final String fullName;

  public ContactPerson(
      String nickname,
      long contactId,
      String fullName,
      @Nullable String photoThumbnailUri
  ) {
    super(photoThumbnailUri);
    this.nickname = nickname;
    this.contactId = contactId;
    this.fullName = fullName;
  }

  public ContactPerson(String nickname) {
    super(null);
    this.nickname = nickname;
    this.contactId = INVALID_CONTACT_ID;
    this.fullName = nickname;
  }

  public String getNickname() {
    return nickname;
  }

  public long getContactId() {
    return contactId;
  }

  public String getFullName() {
    return fullName;
  }

  public boolean isValid() {
    return contactId != INVALID_CONTACT_ID;
  }
}

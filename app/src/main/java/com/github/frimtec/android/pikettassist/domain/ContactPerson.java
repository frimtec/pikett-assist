package com.github.frimtec.android.pikettassist.domain;

public class ContactPerson extends BaseContact {

  private static final long INVALID_CONTACT_ID = -1;

  private final String nickname;
  private final long contactId;
  private final String fullName;

  public ContactPerson(
      String nickname,
      long contactId,
      String fullName,
      Photo photo,
      boolean live
  ) {
    super(photo, live);
    this.nickname = nickname;
    this.contactId = contactId;
    this.fullName = fullName;
  }

  public ContactPerson(String nickname, boolean live) {
    super(new Photo(), live);
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

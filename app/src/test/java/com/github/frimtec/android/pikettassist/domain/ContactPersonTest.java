package com.github.frimtec.android.pikettassist.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContactPersonTest {

  @Test
  void getNickname() {
    final String nickname = "nickname";
    ContactPerson contactPerson = new ContactPerson(nickname, 12L, "fullName");
    assertThat(contactPerson.getNickname()).isEqualTo(nickname);
  }

  @Test
  void getContactId() {
    long id = 12L;
    ContactPerson contactPerson = new ContactPerson("nickname", id, "fullName");
    assertThat(contactPerson.getContactId()).isEqualTo(id);
  }

  @Test
  void getFullName() {
    String fullName = "fullName";
    ContactPerson contactPerson = new ContactPerson("nickname", 12L, fullName);
    assertThat(contactPerson.getFullName()).isEqualTo(fullName);
  }

  @Test
  void isValidForInvalidContactPerson() {
    ContactPerson contactPerson = new ContactPerson("nickname");
    assertThat(contactPerson.isValid()).isFalse();
  }

  @Test
  void isValidForContactPerson() {
    ContactPerson contactPerson = new ContactPerson("nickname", 12L, "fullName");
    assertThat(contactPerson.isValid()).isTrue();
  }
}
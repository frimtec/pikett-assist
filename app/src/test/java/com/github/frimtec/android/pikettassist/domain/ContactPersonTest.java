package com.github.frimtec.android.pikettassist.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContactPersonTest {

  @Test
  void getId() {
    long id = 12L;
    ContactPerson contactPerson = new ContactPerson(id, "fullName");
    assertThat(contactPerson.getId()).isEqualTo(id);
  }

  @Test
  void getFullName() {
    String fullName = "fullName";
    ContactPerson contactPerson = new ContactPerson(12L, fullName);
    assertThat(contactPerson.getFullName()).isEqualTo(fullName);
  }

  @Test
  void isValidForInvalidContactPerson() {
    ContactPerson contactPerson = new ContactPerson("any");
    assertThat(contactPerson.isValid()).isFalse();
  }

  @Test
  void isValidForContactPerson() {
    ContactPerson contactPerson = new ContactPerson(12L, "fullName");
    assertThat(contactPerson.isValid()).isTrue();
  }
}
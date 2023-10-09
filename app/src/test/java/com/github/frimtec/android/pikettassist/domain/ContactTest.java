package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContactTest {

  @Test
  void getter() {
    ContactReference reference = new ContactReference(12, "key");
    Contact contact = new Contact(reference, true, "test");
    assertThat(contact.reference()).isEqualTo(reference);
    assertThat(contact.valid()).isTrue();
    assertThat(contact.name()).isEqualTo("test");
  }
}
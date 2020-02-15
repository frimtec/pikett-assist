package com.github.frimtec.android.pikettassist.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContactTest {

  @Test
  void getter() {
    ContactReference reference = new ContactReference(12, "key");
    Contact contact = new Contact(reference, true, "test");
    assertThat(contact.getReference()).isEqualTo(reference);
    assertThat(contact.isValid()).isTrue();
    assertThat(contact.getName()).isEqualTo("test");
  }
}
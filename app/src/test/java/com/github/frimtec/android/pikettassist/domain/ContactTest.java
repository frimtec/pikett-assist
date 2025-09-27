package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.Assertions.assertThat;

import android.net.Uri;

import org.junit.jupiter.api.Test;

class ContactTest {

  @Test
  void getter() {
    ContactReference reference = new ContactReference(12, "key");
    Contact contact = new Contact(reference, true, "test", "http://image");
    assertThat(contact.reference()).isEqualTo(reference);
    assertThat(contact.valid()).isTrue();
    assertThat(contact.name()).isEqualTo("test");
    assertThat(contact.photoThumbnailUri()).isEqualTo(Uri.parse("http://image"));
  }
}